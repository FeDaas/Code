package de.moviemanager.ui.detail;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.widget.ImageButton;

import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import de.moviemanager.R;
import de.moviemanager.android.ResultHandling;
import de.moviemanager.core.storage.pipeline.MoviePipeline;
import de.moviemanager.core.storage.pipeline.PerformerPipeline;
import de.moviemanager.data.Movie;
import de.moviemanager.data.MovieTransformations;
import de.moviemanager.data.Performer;
import de.moviemanager.data.proxy.PersistentProxy;
import de.moviemanager.data.proxy.PortrayableProxy;
import de.moviemanager.data.proxy.TemporaryProxy;
import de.moviemanager.ui.adapter.SimpleRecyclerViewAdapter;
import de.moviemanager.ui.detail.modifiable.FreeTextAttribute;
import de.moviemanager.ui.detail.modifiable.ModifiableAttribute;
import de.moviemanager.ui.detail.modifiable.StringListAttribute;
import de.moviemanager.ui.detail.modifiable.TextInputAttribute;
import de.moviemanager.ui.detail.modifications.Modification;
import de.moviemanager.ui.dialog.PerformerSafeRemovalDialog;
import de.moviemanager.ui.dialog.PortrayableListDialog.ListContext;
import de.moviemanager.ui.dialog.ReleaseDialog;
import de.moviemanager.ui.masterlist.MarginItemDecoration;
import de.moviemanager.ui.wiki.WikiStorage;
import de.moviemanager.ui.wiki.fetch.WikiFetchActivity;
import de.moviemanager.ui.wiki.query.WikiQueryMode;
import de.moviemanager.util.ScrollViewUtils;
import de.util.DateUtils;
import de.util.Pair;
import de.util.operationflow.ReversibleOperations.ReversibleTransformation;
import de.util.operationflow.ReversibleTransaction;
import de.wiki.data.Film;

import static de.moviemanager.core.storage.temporary.IntentPayloadStorage.popTemporaryProxyFromResult;
import static de.moviemanager.core.storage.temporary.IntentPayloadStorage.popTemporaryProxyFromSource;
import static de.moviemanager.core.storage.temporary.IntentPayloadStorage.pushTemporaryProxy;
import static de.moviemanager.ui.detail.PerformerDetailEditActivity.SOURCE_MOVIE_ID;
import static de.moviemanager.util.DrawableUtils.crop;
import static de.moviemanager.util.Listeners.createOnTextChangedListener;
import static de.moviemanager.util.RecyclerViewUtils.addSwipeSupport;
import static de.util.Pair.MAP_KEY_SECOND;
import static java.lang.Integer.parseInt;
import static java.util.stream.Collectors.toList;

public class MovieDetailEditActivity extends PortrayableDetailEditActivity<Movie> {
    public static final String SOURCE_PERFORMER_ID = "source_performer_id";
    private TextInputAttribute<Movie> titleAttr;

    private ImageButton linkPerformerButton;
    private RecyclerView linkedPerformersList;
    private LinkedPortrayableList<Performer> linkedPerformers;

    private ImageButton addRelease;
    private RecyclerView releaseList;
    private List<Map<String, ?>> releases;
    private SimpleRecyclerViewAdapter releaseAdapter;

    private String temporaryCreatorKey;
    private FreeTextAttribute<Movie> runtimeAttr;
    private FreeTextAttribute<Movie> descriptionAttr;
    private StringListAttribute<Movie> languagesAttr;
    private StringListAttribute<Movie> productionLocationsAttr;

    public MovieDetailEditActivity() {
        super(R.layout.activity_movie_detail_edit);
    }

    @Override
    protected void bindViews() {
        scrollView = findViewById(R.id.attributes_container_scroll);
        buildTitleAttr();
        buildImageAttr();
        buildWatchDateAttr();
        buildDueDateAttr();
        buildRuntimeAttr();
        buildRatingAttr();
        linkPerformerButton = findViewById(R.id.link_performer_button);
        linkedPerformersList = findViewById(R.id.linked_performers);
        buildDescriptionAttr();
        buildLanguagesAttr();
        addRelease = findViewById(R.id.add_release_button);
        releaseList = findViewById(R.id.releases);
        buildProductionLocationsAttr();
        buildFilmingLocationsAttr();

        attributes.forEach(ModifiableAttribute::bindViews);

        setupLinkedPerformersList();
        setupReleaseList();
    }

    private void buildTitleAttr() {
        titleAttr = buildTextInputAttribute(
                R.id.edit_movie_title,
                Movie::getTitle,
                MovieTransformations::setTitle,
                createOnTextChangedListener(s -> checkEnableStateOfMenuItems()),
                (edit, newState) -> checkCommitItemConstraints()
        );
    }

    private void buildImageAttr() {
        imageAttr = buildImageAttribute(
                R.id.edit_image,
                R.id.reset_image_button);
    }

    private void buildWatchDateAttr() {
        buildDateAttribute(
                R.id.edit_watch_date,
                Movie::getWatchDate,
                MovieTransformations::setWatchDate
        );
    }

    private void buildDueDateAttr() {
        buildDateAttribute(
                R.id.edit_due_date,
                Movie::getDueDate,
                MovieTransformations::setDueDate
        );
    }

    private void buildRuntimeAttr() {
        runtimeAttr = buildFreeTextAttribute(
                R.id.edit_runtime,
                m -> Integer.toString(m.getRuntime()),
                s -> {
                    if (s == null || s.trim().isEmpty())
                        return MovieTransformations.setRuntime(0);
                    return MovieTransformations.setRuntime(parseInt(s));
                }
        );
    }

    private void buildRatingAttr() {
        buildRatingAttribute(
                R.id.edit_rating,
                Movie::getRating,
                MovieTransformations::setRating);
    }

    private void buildDescriptionAttr() {
        descriptionAttr = buildFreeTextAttribute(
                R.id.edit_description,
                Movie::getDescription,
                MovieTransformations::setDescription);
    }

    private void buildLanguagesAttr() {
        languagesAttr = buildStringListAttribute(
                R.id.edit_languages,
                Movie::getLanguages,
                MovieTransformations::setLanguages);
    }

    private void buildProductionLocationsAttr() {
        productionLocationsAttr = buildStringListAttribute(
                R.id.edit_production_locations,
                Movie::getProductionLocations,
                MovieTransformations::setProductionLocations);
    }

    private void buildFilmingLocationsAttr() {
        buildStringListAttribute(
                R.id.edit_filming_locations,
                Movie::getFilmingLocations,
                MovieTransformations::setFilmingLocations);
    }

    private void setupLinkedPerformersList() {
        linkedPerformers = new LinkedPortrayableList<>(this, linkedPerformersList);
        linkedPerformers.setOnModificationUndo(() -> {
            hideKeyboard();
            ScrollViewUtils.scrollToViewIfNeeded(scrollView, linkedPerformersList);
            checkCommitItemConstraints();
        });
        linkedPerformers.setAddModification(this::addModification);
        linkedPerformers.setOnLinkedDataChanged(this::checkCommitItemConstraints);

    }

    private void setupReleaseList() {
        releases = new ArrayList<>();

        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        releaseList.setLayoutManager(linearLayoutManager);

        final List<Function<Object, String>> mappers = new ArrayList<>();
        mappers.add(Object::toString);
        mappers.add(o -> DateUtils.dateToText((Date) o));

        releaseAdapter = new SimpleRecyclerViewAdapter(this,
                releases,
                R.layout.listitem_release_edit,
                new String[]{Pair.MAP_KEY_FIRST, MAP_KEY_SECOND},
                new int[]{R.id.show_release_location, R.id.show_release_date},
                mappers);
        releaseList.setAdapter(releaseAdapter);
        releaseAdapter.setOnItemClick(view -> {
            int i = ((RecyclerView.ViewHolder) view.getTag()).getAdapterPosition();
            final Map<String, ?> map = releases.get(i);
            showReleaseModificationDialog(i, map);
        });
        addSwipeSupport(this, releaseList, R.drawable.ic_delete_enabled, this::deleteRelease);
        releaseList.addItemDecoration(new MarginItemDecoration(this, R.dimen.half_padding));
    }

    private void showReleaseModificationDialog(int i, final Map<String, ?> map) {
        final FragmentManager fragmentManager = getSupportFragmentManager();
        final ReleaseDialog dialog = ReleaseDialog.create(mapToRelease(map));

        dialog.setConfirmationListener(release -> {
            addModification(new Modification<>(releases.get(i), savedMap -> {
                releases.set(i, savedMap);
                releaseAdapter.notifyItemChanged(i);
                ScrollViewUtils.scrollToViewIfNeeded(scrollView, releaseList);
            }));
            releases.set(i, release.toMap());
            releaseAdapter.notifyItemChanged(i);
        });

        dialog.show(fragmentManager, "modify_release_dialog");
    }

    private void deleteRelease(final RecyclerView.ViewHolder vh) {
        int pos = vh.getAdapterPosition();
        final Map<String, ?> release = releases.remove(pos);
        releaseAdapter.notifyItemRemoved(pos);
        addModification(new Modification<>(pos, oldPos -> {
            releases.add(oldPos, release);
            releaseAdapter.notifyItemInserted(oldPos);
            ScrollViewUtils.scrollToViewIfNeeded(scrollView, releaseList);
        }));
    }

    @Override
    protected Movie getFromStorage(int id) {
        Movie result = null;
        if (id >= 0) {
            result = STORAGE.getMovieById(id).orElse(null);
        }
        return result;
    }

    @Override
    protected void initForCreation() {
        final Intent metaData = getIntent();
        final TemporaryProxy sourcePerformer = popTemporaryProxyFromSource(metaData, SOURCE_PERFORMER_ID);
        if (sourcePerformer != null) {
            linkedPerformers.link(sourcePerformer);
        }

        imageAttr.setCustomImageFlag(false);
        releases.clear();
    }

    @Override
    protected String createPipeline() {
        return MoviePipeline.beginPipeline();
    }

    @Override
    protected void initForUpdate() {
        setInitialStateFrom(currentObject);
    }

    private void setInitialStateFrom(Movie movie) {
        final List<Performer> mergedList = new ArrayList<>(STORAGE.getLinkedPerformersOfMovie(movie));
        linkedPerformers.setInitialState(mergedList);

        releases.clear();
        releases.addAll(movie.getReleases()
                .stream()
                .map(Pair::toMap)
                .collect(toList()));
        releaseAdapter.notifyDataSetChanged();

        attributes.forEach(attr -> attr.setContentModel(movie));
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void registerListeners() {
        linkPerformerButton.setOnClickListener(v -> showSelectionDialog());
        addRelease.setOnClickListener(v -> showReleaseCreationDialog());
        attributes.forEach(ModifiableAttribute::bindListeners);
    }

    private void showReleaseCreationDialog() {
        final FragmentManager fragmentManager = getSupportFragmentManager();
        final ReleaseDialog dialog = ReleaseDialog.create();

        dialog.setConfirmationListener(release -> {
            int pos = releases.size();
            releases.add(pos, release.toMap());
            releaseAdapter.notifyDataSetChanged();

            addModification(new Modification<>(pos, oldIndex -> {
                releases.remove((int) oldIndex);
                releaseAdapter.notifyDataSetChanged();
                ScrollViewUtils.scrollToViewIfNeeded(scrollView, releaseList);
            }));
        });

        dialog.show(fragmentManager, "create_release_dialog");
    }

    private void showSelectionDialog() {
        linkedPerformers.showSelectionDialog(
                Performer.class,
                getNotLinkedPerformers(),
                parentPipelineKey == null,
                this::addNewPerformer);

    }

    private List<Performer> getNotLinkedPerformers() {
        return STORAGE.getPerformers()
                .stream()
                .filter(p -> linkedPerformers.containsNot(new PersistentProxy(p)))
                .filter(linkedPerformers::containsNot)
                .collect(toList());
    }

    private void addNewPerformer(final ResultHandling source, final ListContext context) {
        final Intent data = source.createIntent(PerformerDetailEditActivity.class);

        data.putExtra(PARENT_PIPELINE, pipelineKey);
        data.putExtra(SOURCE_MOVIE_ID, pushTemporaryProxy(currentProxy()));

        startActivityForResult(data, result -> returnFromPerformerCreation(result, context));
    }

    private void returnFromPerformerCreation(final Intent result, final ListContext context) {
        PortrayableProxy proxy = popTemporaryProxyFromResult(result);
        context.addElement(proxy);
    }

    @Override
    protected void checkEnableStateOfMenuItems() {
        checkCommitItemConstraints();
        changeWikiEnable();
    }

    @Override
    protected void checkCommitItemConstraints() {
        if (commitItem != null) {
            boolean hasName = !titleAttr.hasEmptyText();
            boolean enabled = hasModifications() && hasName;

            updateCommitMenuItem(enabled);
        }
    }

    @Override
    protected void updateWarning() {
        boolean emptyTitle = titleAttr.getContent().trim().isEmpty();
        boolean errorEnabled = !emptyTitle;

        if(errorEnabled) {
            titleAttr.setError(null);
        } else {
            titleAttr.setError(getString(R.string.warning_movie_title));
        }
    }

    private void changeWikiEnable() {
        wikiItem.setEnabled(!titleAttr.hasEmptyText());
    }

    @Override
    protected void handleResultOfWikiSync(Intent result) {
        Film film = WikiStorage.unwrapFilm(result.getStringExtra(WikiFetchActivity.RESULT_KEY));
        Bitmap bitmap = WikiStorage.retrieveImage();
        List<Modification<?>> modifications = new ArrayList<>();

        modifications.add(titleAttr.modifyValue(film.getTitle()));
        if(bitmap != null) {
            bitmap = crop(bitmap, 2, 3);
            modifications.add(imageAttr.modifyValue(Pair.paired(new BitmapDrawable(getResources(), bitmap), true)));
        }
        modifications.add(descriptionAttr.modifyValue(film.getDescription()));
        modifications.add(runtimeAttr.modifyValue(film.getRunningTime().split(" ")[0]));
        modifications.add(languagesAttr.modifyValue(film.getLanguages()));
        modifications.add(productionLocationsAttr.modifyValue(film.getCountries()));

        addModification(Modification.stack(modifications));
    }

    @Override
    protected void checkCommitConditions(Runnable commit) {
        List<Performer> unlinkedPerformer = linkedPerformers.getUnlinked()
                .stream()
                .filter(m -> !m.isTemporary())
                .map(PersistentProxy.class::cast)
                .map(PersistentProxy::getSource)
                .map(p -> (Performer) p)
                .collect(toList());

        if (mode == EditMode.UPDATE) {
            PerformerSafeRemovalDialog.showIfNecessary(this,
                    unlinkedPerformer,
                    li -> super.checkCommitConditions(commit),
                    () -> {
                    },
                    () -> super.checkCommitConditions(commit));
        } else
            super.checkCommitConditions(commit);
    }

    @Override
    protected String getCurrentName() {
        return titleAttr.getContent();
    }

    @Override
    protected WikiQueryMode getQueryMode() {
        return WikiQueryMode.FILM;
    }

    @Override
    protected ReversibleTransaction<Movie> creationTransaction() {
        return STORAGE.newMovie();
    }

    @Override
    protected ReversibleTransaction<Movie> updateTransaction() {
        return STORAGE.updateMovie(currentObject);
    }

    @Override
    protected void addOperationsToTransaction() {
        if (parentPipelineKey != null) {
            temporaryCreatorKey = MoviePipeline.saveTemporaryCreator(() -> {
                attributes.forEach(attr -> transaction.addOperation(attr.toTransformation()));
                transaction.addOperation(MovieTransformations.setReleases(getReleases()));
                return transaction;
            });
        } else {
            attributes.forEach(attr -> transaction.addOperation(attr.toTransformation()));
            transaction.addOperation(MovieTransformations.setReleases(getReleases()));
        }
    }

    @Override
    protected void updatePipeline() {
        if (parentPipelineKey == null) {
            MoviePipeline.setCore(pipelineKey, transaction);
            MoviePipeline.setPersistentLinkedPerformers(pipelineKey, setPersistentLinkedPerformers());

            final List<TemporaryProxy> temporaries = linkedPerformers.getTemporaries();
            temporaries.stream()
                    .filter(TemporaryProxy::hasCreatorKey)
                    .map(TemporaryProxy::getCreatorKey)
                    .map(PerformerPipeline::pullTemporaryFunction)
                    .forEach(function -> MoviePipeline.addTemporaryPerformer(pipelineKey, function));
        }
    }

    private ReversibleTransformation<Movie> setPersistentLinkedPerformers() {
        return linkedPerformers.toCommitOperation(STORAGE::link, STORAGE::unlink);
    }

    @Override
    protected TemporaryProxy currentProxy() {
        final TemporaryProxy proxy = new TemporaryProxy(Movie.class,
                titleAttr.getContent(),
                imageAttr.getContent().first
        );

        if(currentObject != null) {
            proxy.basedOn(currentObject);
        }

        if (temporaryCreatorKey != null) {
            proxy.setCreatorKey(temporaryCreatorKey);
        }

        return proxy;
    }

    private List<Pair<String, Date>> getReleases() {
        return releases
                .stream()
                .map(this::mapToRelease)
                .collect(toList());
    }

    private Pair<String, Date> mapToRelease(final Map<String, ?> map) {
        return Pair.paired((String) map.get(Pair.MAP_KEY_FIRST), (Date) map.get(MAP_KEY_SECOND));
    }
}



