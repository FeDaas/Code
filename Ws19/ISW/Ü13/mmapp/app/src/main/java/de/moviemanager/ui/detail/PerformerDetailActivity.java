package de.moviemanager.ui.detail;

import android.app.Activity;
import android.content.Intent;
import android.util.SparseArray;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import de.moviemanager.R;
import de.moviemanager.android.ResultHandler;
import de.moviemanager.android.ResultHandlingActivity;
import de.moviemanager.core.storage.pipeline.PerformerPipeline;
import de.moviemanager.data.ImagePyramid;
import de.moviemanager.data.Movie;
import de.moviemanager.data.Performer;
import de.moviemanager.data.proxy.PersistentProxy;
import de.moviemanager.ui.adapter.LinkedDataAdapter;
import de.moviemanager.ui.masterlist.MarginItemDecoration;
import de.moviemanager.ui.view.CustomRatingBar;
import de.moviemanager.ui.view.DateSelectionView;
import de.moviemanager.util.AndroidStringUtils;
import de.moviemanager.util.autobind.AutoBind;
import de.moviemanager.util.autobind.Bind;
import de.util.Identifiable;
import de.util.StringUtils;

import static de.moviemanager.ui.detail.PortrayableDetailEditActivity.RESULT_PIPELINE;
import static de.moviemanager.util.RecyclerViewUtils.setLinearLayoutTo;
import static de.moviemanager.util.ScrollViewUtils.enableDeepScroll;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

public class PerformerDetailActivity extends PortrayableDetailActivity<Performer> {
    @Bind(R.id.image) private ImageView showImage;
    @Bind(R.id.date_of_birth) private DateSelectionView showDateOfBirth;
    @Bind(R.id.rating) private CustomRatingBar showRating;
    @Bind(R.id.birth_name) private TextView showBirthName;
    @Bind(R.id.biography) private TextView showBiography;

    @Bind(R.id.linked_movies) private RecyclerView linkedMoviesList;
    private List<Movie> linkedMovies;
    private LinkedDataAdapter linkedMoviesAdapter;

    @Bind(R.id.occupations) private TextView showOccupations;

    public PerformerDetailActivity() {
        super(R.layout.activity_performer_detail, PerformerDetailEditActivity.class);
    }

    @Override
    protected void bindViews() {
        AutoBind.bindAll(this);
    }

    @Override
    protected void setupLists() {
        setupLinkedMoviesList();
    }

    private void setupLinkedMoviesList() {
        setLinearLayoutTo(this, linkedMoviesList);

        linkedMovies = new ArrayList<>(getSortedLinkedMovies());
        linkedMoviesAdapter = new LinkedDataAdapter(this,
                toProxyList(linkedMovies),
                R.layout.listitem_portrayable_detail
        );
        linkedMoviesAdapter.setOnItemClickListener(movie -> MovieDetailActivity.showAndNotify(this,
                (Movie) ((PersistentProxy) movie).getSource(),
                this::updateAfterLinkedDetails)
        );
        linkedMoviesList.setAdapter(linkedMoviesAdapter);
        linkedMoviesList.addItemDecoration(new MarginItemDecoration(getResources().getDimension(R.dimen.default_padding)));
    }

    private List<Movie> getSortedLinkedMovies() {
        return STORAGE.getLinkedMoviesOfPerformer(model)
                .stream()
                .sorted(comparing(Movie::name, StringUtils::alphabeticalComparison))
                .collect(toList());
    }

    @Override
    protected void setListeners() {
        enableDeepScroll(showBiography);
        enableDeepScroll(showOccupations);
    }

    @Override
    protected void updateAfterLinkedDetails(final Intent result) {
        final Optional<Performer> currentModelOpt = STORAGE.getPerformerById(model.id());
        if(currentModelOpt.isPresent()) {
            model = currentModelOpt.get();
            updateUIWithModelData();
        } else {
            finishAfterDeletion(model);
        }
    }

    @Override
    protected void updateUIWithModelData() {
        actionBar.setTitle(model.name());
        showImage.setImageDrawable(STORAGE.getImage(this, model, ImagePyramid.ImageSize.LARGE).first);
        showDateOfBirth.setDate(model.getDateOfBirth());
        showRating.setRating(model.getRating());
        showBirthName.setText(model.getBirthName());
        showBiography.setText(model.getBiography());

        linkedMovies.clear();
        linkedMovies.addAll(getSortedLinkedMovies());
        linkedMoviesAdapter.update(toProxyList(linkedMovies));

        showOccupations.setText(AndroidStringUtils.join("\n", model.getOccupations()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail_menu, menu);
        return true;
    }

    @Override
    protected void updateAfterEdit(Intent result) {
        final String key = result.getStringExtra(RESULT_PIPELINE);
        PerformerPipeline.commit(key);

        final Optional<Performer> optional = PerformerPipeline.getResultOf(key)
                .filter(PersistentProxy::isPersistent)
                .map(PersistentProxy.class::cast)
                .map(PersistentProxy::getSource)
                .map(Performer.class::cast);

        final Optional<Performer> reloaded = optional
                .map(Identifiable::id)
                .flatMap(STORAGE::getPerformerById);

        if(reloaded.isPresent()) {
            model = reloaded.get();
            updated = true;
            updateUIWithModelData();
        } else {
            finishAfterDeletion(model);
        }
    }

    @Override
    void finishAfterDeletion(final Performer old) {
        String msg = String.format(getString(R.string.info_performer_deletion), old.name());
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        super.finishAfterDeletion(old);
    }

    public static void showAndNotifyIfOk(final ResultHandlingActivity source,
                                         final Performer perf,
                                         final ResultHandler handler) {
        final Intent intent = new Intent(source, PerformerDetailActivity.class);
        intent.putExtra(INITIAL_PORTRAYABLE, perf);

        source.startActivityForResult(intent, handler);
    }

    public static void showAndNotify(final ResultHandlingActivity source,
                                     final Performer performer,
                                     final ResultHandler handler) {
        final Intent intent = new Intent(source, PerformerDetailActivity.class);
        intent.putExtra(INITIAL_PORTRAYABLE, performer);

        final SparseArray<ResultHandler> handlers = new SparseArray<>();
        handlers.put(Activity.RESULT_OK, handler);
        handlers.put(Activity.RESULT_CANCELED, handler);
        source.startActivityForResult(intent, handlers);
    }
}
