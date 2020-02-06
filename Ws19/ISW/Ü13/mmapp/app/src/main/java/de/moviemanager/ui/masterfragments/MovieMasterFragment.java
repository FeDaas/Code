package de.moviemanager.ui.masterfragments;

import android.content.Intent;

import java.util.function.Consumer;

import de.moviemanager.R;
import de.moviemanager.core.storage.pipeline.MoviePipeline;
import de.moviemanager.core.storage.pipeline.Pipelines;
import de.moviemanager.data.Movie;
import de.moviemanager.ui.adapter.PortrayableRVAdapter;
import de.moviemanager.ui.detail.MovieDetailActivity;
import de.moviemanager.ui.detail.MovieDetailEditActivity;
import de.moviemanager.ui.dialog.PerformerSafeRemovalDialog;
import de.moviemanager.ui.masterfragments.onetimetask.OneTimeTaskExecutorRudiment;
import de.moviemanager.ui.masterlist.categorizer.Alphabetical;
import de.moviemanager.ui.masterlist.categorizer.Due;
import de.moviemanager.ui.masterlist.categorizer.OrderGroup;
import de.moviemanager.ui.masterlist.categorizer.Rated;
import de.moviemanager.util.RatingUtils;

import static de.moviemanager.ui.detail.PortrayableDetailEditActivity.RESULT_PIPELINE;
import static de.moviemanager.util.RatingUtils.calculateOverallRating;
import static de.moviemanager.util.RatingUtils.ratingToString;

public class MovieMasterFragment extends PortrayableMasterFragment<Movie> {
    private PortrayableRVAdapter<Movie> adapter;

    @Override
    protected void createOrders() {
        final String rating = getString(R.string.movie_criterion_rating);
        final String overallRating = getString(R.string.movie_criterion_overall_rating);
        final String dueDate = "Due Date";

        orders = new OrderGroup<>(0);
        orders.addOrder(getString(R.string.movie_criterion_title),
                new Alphabetical<>(false,
                        m -> ratingToString(calculateOverallRating(m), true)),
                nameContainsInput
        );
        orders.addOrder(rating,
                new Rated<>(rating, Movie::getRating),
                nameContainsInput
        );
        orders.addOrder(overallRating,
                new Rated<>(overallRating, RatingUtils::calculateOverallRating),
                nameContainsInput
        );
        orders.addOrder(dueDate,
                new Due(),
                nameContainsInput);
    }

    @Override
    protected PortrayableRVAdapter<Movie> createAdapter() {
        String constraint = filter.getText().toString();

        adapter = PortrayableRVAdapter.<Movie>builder()
                .setTaskExecutor((OneTimeTaskExecutorRudiment) getActivity())
                .setHost(this)
                .setOrders(orders)
                .setModelData(originalData)
                .setConstraint(constraint)
                .setDetailActivityGetter(() -> MovieDetailActivity.class)
                .setDetailEditActivityGetter(() -> MovieDetailEditActivity.class)
                .setRemoveFromStorage(STORAGE::removeMovie)
                .setAfterUpdate(result -> afterUpdate())
                .setAfterRequestedCreation(this::afterRequestedCreation)
                .setCanDelete(this::canDelete)
                .build();

        return adapter;
    }

    private void afterRequestedCreation(final Intent result) {
        final String key = result.getStringExtra(RESULT_PIPELINE);
        MoviePipeline.commit(key);
        Pipelines.discardAllPipelines();
        originalData.clear();
        originalData.addAll(STORAGE.getMovies());
        afterUpdate();
    }

    private void afterUpdate() {
        adapter.reselectOrder();
    }

    private void canDelete(Movie model, Consumer<Movie> delete) {
        PerformerSafeRemovalDialog.showIfNecessary(getActivity(),
                STORAGE.getLinkedPerformersOfMovie(model),
                li -> delete.accept(model),
                () -> {},
                () -> delete.accept(model)
        );
    }

    @Override
    public PortrayableRVAdapter<Movie> getAdapter() {
        return adapter;
    }
}
