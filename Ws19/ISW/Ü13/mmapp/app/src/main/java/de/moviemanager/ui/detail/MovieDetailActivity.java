package de.moviemanager.ui.detail;


import android.app.Activity;
import android.content.Intent;
import android.util.SparseArray;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import de.moviemanager.R;
import de.moviemanager.android.ResultHandler;
import de.moviemanager.android.ResultHandlingActivity;
import de.moviemanager.core.storage.pipeline.MoviePipeline;
import de.moviemanager.data.Movie;
import de.moviemanager.data.Performer;
import de.moviemanager.data.proxy.PersistentProxy;
import de.moviemanager.ui.adapter.LinkedDataAdapter;
import de.moviemanager.ui.adapter.SimpleRecyclerViewAdapter;
import de.moviemanager.ui.masterlist.MarginItemDecoration;
import de.moviemanager.ui.view.CustomRatingBar;
import de.moviemanager.ui.view.DateSelectionView;
import de.moviemanager.util.autobind.AutoBind;
import de.moviemanager.util.autobind.Bind;
import de.util.DateUtils;
import de.util.Pair;
import de.util.StringUtils;

import static de.moviemanager.data.ImagePyramid.ImageSize.LARGE;
import static de.moviemanager.ui.detail.PortrayableDetailEditActivity.RESULT_PIPELINE;
import static de.moviemanager.util.AndroidStringUtils.join;
import static de.moviemanager.util.RatingUtils.calculateOverallRating;
import static de.moviemanager.util.RecyclerViewUtils.setLinearLayoutTo;
import static de.moviemanager.util.ScrollViewUtils.enableDeepScroll;
import static de.util.Pair.MAP_KEY_SECOND;
import static java.lang.String.format;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

public class MovieDetailActivity extends PortrayableDetailActivity<Movie> {
    @Bind(R.id.image) private ImageView showImage;
    @Bind(R.id.watch_date) private DateSelectionView showWatchDate;
    @Bind(R.id.due_date) private DateSelectionView showDueDate;
    @Bind(R.id.runtime) private TextView showRuntime;
    @Bind(R.id.rating) private CustomRatingBar showRating;

    @Bind(R.id.overall_rating_header) private TextView overallRatingTitle;
    @Bind(R.id.overall_rating) private CustomRatingBar showOverallRating;

    @Bind(R.id.description) private TextView showDescription;
    @Bind(R.id.linked_performers) private RecyclerView showLinkedPerformers;
    private List<Performer> linkedPerformers;
    private LinkedDataAdapter adapter;
    @Bind(R.id.languages) private TextView showLanguages;

    @Bind(R.id.releases) private RecyclerView releaseList;
    private List<Map<String, ?>> releases;
    private SimpleRecyclerViewAdapter releaseAdapter;

    @Bind(R.id.production_locations) private TextView showProductionLocations;
    @Bind(R.id.filming_locations) private TextView showFilmingLocations;

    public MovieDetailActivity() {
        super(R.layout.activity_movie_detail, MovieDetailEditActivity.class);
    }

    @Override
    protected void bindViews() {
        AutoBind.bindAll(this);
    }

    @Override
    protected void setupLists() {
        setupLinkedPerformersList();
        setupReleaseList();
    }

    private void setupLinkedPerformersList() {
        setLinearLayoutTo(this, showLinkedPerformers);

        linkedPerformers = new ArrayList<>(getSortedLinkedPerformers());
        adapter = new LinkedDataAdapter(this,
                toProxyList(linkedPerformers),
                R.layout.listitem_portrayable_detail
        );
        adapter.setOnItemClickListener(perf -> PerformerDetailActivity.showAndNotify(this,
                (Performer) ((PersistentProxy) perf).getSource(),
                this::updateAfterLinkedDetails)
        );
        showLinkedPerformers.setAdapter(adapter);
        showLinkedPerformers.addItemDecoration(new MarginItemDecoration(getResources().getDimension(R.dimen.default_padding)));
    }

    private List<Performer> getSortedLinkedPerformers() {
        return STORAGE.getLinkedPerformersOfMovie(model)
                .stream()
                .sorted(comparing(Performer::name, StringUtils::alphabeticalComparison))
                .collect(toList());
    }

    private void setupReleaseList() {
        setLinearLayoutTo(this, releaseList);

        releases = getSortedReleases();

        final List<Function<Object, String>> mappers = new ArrayList<>();
        mappers.add(Object::toString);
        mappers.add(o -> DateUtils.dateToText((Date) o));

        releaseAdapter = new SimpleRecyclerViewAdapter(this,
                releases,
                R.layout.listitem_release,
                new String[]{Pair.MAP_KEY_FIRST, MAP_KEY_SECOND},
                new int[]{R.id.show_release_location, R.id.show_release_date},
                mappers);
        releaseList.setAdapter(releaseAdapter);
        releaseList.addItemDecoration(new MarginItemDecoration(this, R.dimen.half_padding));
    }

    private List<Map<String, ?>> getSortedReleases() {
        return model.getReleases()
                .stream()
                .map(Pair::toMap)
                .sorted(Comparator.comparing(m -> (Date) m.get(MAP_KEY_SECOND)))
                .collect(toList());
    }

    @Override
    protected void updateAfterLinkedDetails(final Intent result) {
        final Optional<Movie> currentModelOpt = STORAGE.getMovieById(model.id());
        if (currentModelOpt.isPresent()) {
            model = currentModelOpt.get();
            updateUIWithModelData();
        } else {
            finishAfterDeletion(model);
        }
    }

    @Override
    protected void updateAfterEdit(Intent result) {
        final String key = result.getStringExtra(RESULT_PIPELINE);
        MoviePipeline.commit(key);

        final Optional<Movie> m = MoviePipeline.getResultOf(key)
                .filter(PersistentProxy::isPersistent)
                .map(PersistentProxy.class::cast)
                .map(PersistentProxy::getSource)
                .map(Movie.class::cast);

        if (m.isPresent()) {
            model = m.get();
            updated = true;
            updateUIWithModelData();
        }
    }

    @Override
    protected void updateUIWithModelData() {
        actionBar.setTitle(model.getTitle());
        showImage.setImageDrawable(STORAGE.getImage(this, model, LARGE).first);
        showWatchDate.setDate(model.getWatchDate());
        showDueDate.setDate(model.getDueDate());

        if (showDueDate.getDate() == null) {
            showDueDate.setVisibility(View.INVISIBLE);
            findViewById(R.id.due_banner).setVisibility(View.INVISIBLE);
            findViewById(R.id.due_date_divider).setVisibility(View.INVISIBLE);
            findViewById(R.id.due_date_header).setVisibility(View.INVISIBLE);
        } else {
            showDueDate.setVisibility(View.VISIBLE);
            findViewById(R.id.due_banner).setVisibility(View.VISIBLE);
            findViewById(R.id.due_date_divider).setVisibility(View.VISIBLE);
            findViewById(R.id.due_date_header).setVisibility(View.VISIBLE);

            if (DateUtils.normDateTimeToMidnight(showDueDate.getDate()).compareTo(DateUtils.nowAtMidnight()) <= 0) {
                ImageView dueBanner = (ImageView)findViewById(R.id.due_banner);
                dueBanner.setImageResource(R.drawable.ic_overdue_banner_big);
            } else {
                ImageView dueBanner = (ImageView)findViewById(R.id.due_banner);
                dueBanner.setImageResource(R.drawable.ic_due_banner_big);
            }
        }

        showRuntime.setText(format(getCurrentLocale(), "%d", model.getRuntime()));
        updateOverallRating();

        showDescription.setText(model.getDescription());
        linkedPerformers.clear();
        linkedPerformers.addAll(getSortedLinkedPerformers());
        adapter.update(toProxyList(linkedPerformers));

        showLanguages.setText(join("\n", model.getLanguages()));
        releases.clear();
        releases.addAll(getSortedReleases());
        releaseAdapter.notifyDataSetChanged();
        showProductionLocations.setText(join("\n", model.getProductionLocations()));
        showFilmingLocations.setText(join("\n", model.getFilmingLocations()));
    }

    private void updateOverallRating() {
        showRating.setRating(model.rating());
        double overallRating = calculateOverallRating(model);

        String overallRatingMessage = getString(R.string.not_rated);
        if (overallRating >= 0) {
            overallRatingMessage = format(Locale.US, "%2.1f", overallRating);
        }

        overallRatingTitle.setText(format(
                getString(R.string.movie_overallRating),
                overallRatingMessage)
        );
        showOverallRating.setRating(overallRating);
    }

    @Override
    protected void setListeners() {
        enableDeepScroll(showDescription);
        enableDeepScroll(showLanguages);
        enableDeepScroll(showProductionLocations);
        enableDeepScroll(showFilmingLocations);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.detail_menu, menu);
        return true;
    }

    public static void showAndNotifyIfOk(final ResultHandlingActivity source,
                                         final Movie movie,
                                         final ResultHandler handler) {
        final Intent intent = new Intent(source, MovieDetailActivity.class);
        intent.putExtra(INITIAL_PORTRAYABLE, movie);

        source.startActivityForResult(intent, handler);
    }

    public static void showAndNotify(final ResultHandlingActivity source,
                                     final Movie movie,
                                     final ResultHandler handler) {
        final Intent intent = new Intent(source, MovieDetailActivity.class);
        intent.putExtra(INITIAL_PORTRAYABLE, movie);

        final SparseArray<ResultHandler> handlers = new SparseArray<>();
        handlers.put(Activity.RESULT_OK, handler);
        handlers.put(Activity.RESULT_CANCELED, handler);
        source.startActivityForResult(intent, handlers);
    }
}