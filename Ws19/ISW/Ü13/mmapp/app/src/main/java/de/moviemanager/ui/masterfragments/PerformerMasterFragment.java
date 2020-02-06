package de.moviemanager.ui.masterfragments;

import android.content.Intent;

import de.moviemanager.R;
import de.moviemanager.core.storage.pipeline.PerformerPipeline;
import de.moviemanager.core.storage.pipeline.Pipelines;
import de.moviemanager.data.Performer;
import de.moviemanager.ui.adapter.PortrayableRVAdapter;
import de.moviemanager.ui.detail.PerformerDetailActivity;
import de.moviemanager.ui.detail.PerformerDetailEditActivity;
import de.moviemanager.ui.masterfragments.onetimetask.OneTimeTaskExecutorRudiment;
import de.moviemanager.ui.masterlist.categorizer.Alphabetical;
import de.moviemanager.ui.masterlist.categorizer.Numeric;
import de.moviemanager.ui.masterlist.categorizer.OrderGroup;
import de.moviemanager.ui.masterlist.categorizer.Rated;

import static de.moviemanager.ui.detail.PortrayableDetailActivity.INITIAL_PORTRAYABLE;
import static de.moviemanager.ui.detail.PortrayableDetailEditActivity.RESULT_PIPELINE;
import static de.moviemanager.util.RatingUtils.ratingToString;


public class PerformerMasterFragment extends PortrayableMasterFragment<Performer> {
    private PortrayableRVAdapter<Performer> adapter;

    @Override
    protected void createOrders() {
        final String rating = getString(R.string.performer_criterion_rating);
        final String age = getString(R.string.performer_criterion_age);

        orders = new OrderGroup<>(0);
        orders.addOrder(getString(R.string.performer_criterion_name),
                new Alphabetical<>(false, p -> ratingToString(p.rating())),
                nameContainsInput
        );
        orders.addOrder(rating,
                new Rated<>(rating, Performer::getRating),
                nameContainsInput
        );
        orders.addOrder(age,
                new Numeric<>(age, 10, Performer::age),
                nameContainsInput
        );
    }

    @Override
    protected PortrayableRVAdapter<Performer> createAdapter() {
        String constraint = filter.getText().toString();
        adapter = PortrayableRVAdapter.<Performer>builder()
                .setTaskExecutor((OneTimeTaskExecutorRudiment) getActivity())
                .setHost(this)
                .setOrders(orders)
                .setModelData(originalData)
                .setConstraint(constraint)
                .setDetailActivityGetter(() -> PerformerDetailActivity.class)
                .setDetailEditActivityGetter(() -> PerformerDetailEditActivity.class)
                .setRemoveFromStorage(STORAGE::removePerformer)
                .setAfterUpdate(this::afterUpdate)
                .setAfterRequestedCreation(this::afterRequestedCreation)
                .build();

        return adapter;
    }

    private void afterRequestedCreation(final Intent result) {
        final String key = result.getStringExtra(RESULT_PIPELINE);
        PerformerPipeline.commit(key);
        Pipelines.discardAllPipelines();
        originalData.clear();
        originalData.addAll(STORAGE.getPerformers());
        afterUpdate(result);
    }

    private void afterUpdate(final Intent result) {
        if (result.getParcelableExtra(INITIAL_PORTRAYABLE) == null) {
            originalData.clear();
            originalData.addAll(STORAGE.getPerformers());
        }
        adapter.reselectOrder();
    }

    @Override
    protected PortrayableRVAdapter<Performer> getAdapter() {
        return adapter;
    }
}
