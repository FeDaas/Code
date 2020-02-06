package de.moviemanager.ui.detail;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.widget.ImageButton;

import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import de.moviemanager.R;
import de.moviemanager.android.ResultHandling;
import de.moviemanager.core.storage.pipeline.MoviePipeline;
import de.moviemanager.core.storage.pipeline.PerformerPipeline;
import de.moviemanager.core.storage.temporary.IntentPayloadStorage;
import de.moviemanager.data.Movie;
import de.moviemanager.data.Performer;
import de.moviemanager.data.PerformerTransformations;
import de.moviemanager.data.proxy.PersistentProxy;
import de.moviemanager.data.proxy.PortrayableProxy;
import de.moviemanager.data.proxy.TemporaryProxy;
import de.moviemanager.ui.detail.modifiable.DateAttribute;
import de.moviemanager.ui.detail.modifiable.FreeTextAttribute;
import de.moviemanager.ui.detail.modifiable.ModifiableAttribute;
import de.moviemanager.ui.detail.modifiable.StringListAttribute;
import de.moviemanager.ui.detail.modifiable.TextInputAttribute;
import de.moviemanager.ui.detail.modifications.Modification;
import de.moviemanager.ui.dialog.PortrayableListDialog.ListContext;
import de.moviemanager.ui.dialog.SimpleDialog;
import de.moviemanager.ui.wiki.WikiStorage;
import de.moviemanager.ui.wiki.fetch.WikiFetchActivity;
import de.moviemanager.ui.wiki.query.WikiQueryMode;
import de.moviemanager.util.AndroidStringUtils;
import de.moviemanager.util.ScrollViewUtils;
import de.util.Pair;
import de.util.operationflow.ReversibleOperations.ReversibleTransformation;
import de.util.operationflow.ReversibleTransaction;
import de.wiki.data.Actor;

import static de.moviemanager.core.storage.temporary.IntentPayloadStorage.popTemporaryProxyFromSource;
import static de.moviemanager.ui.detail.MovieDetailEditActivity.SOURCE_PERFORMER_ID;
import static de.moviemanager.util.DrawableUtils.crop;
import static de.moviemanager.util.Listeners.createOnTextChangedListener;
import static de.util.DateUtils.textToDate;
import static java.util.stream.Collectors.toList;

public class PerformerDetailEditActivity extends PortrayableDetailEditActivity<Performer> {
    public static final String SOURCE_MOVIE_ID = "source_movie_id";

    private RecyclerView linkedMoviesList;
    private ImageButton linkMoviesButton;
    private LinkedPortrayableList<Movie> linkedMovies;
    private TextInputAttribute<Performer> nameAttr;

    private String temporaryCreatorKey;
    private DateAttribute<Performer> dateOfBirthAttr;
    private TextInputAttribute<Performer> birthNameAttr;
    private FreeTextAttribute<Performer> biographyAttr;
    private StringListAttribute<Performer> occupationsAttr;

    public PerformerDetailEditActivity() {
        super(R.layout.activity_performer_detail_edit);
    }

    @Override
    protected void bindViews() {
        scrollView = findViewById(R.id.attributes_container_scroll);
        buildNameAttr();
        buildImageAttr();
        buildDateOfBirthAttr();
        buildRatingAttr();
        buildBirthNameAttr();
        buildBiographyAttr();
        linkedMoviesList = findViewById(R.id.linked_movies_list);
        linkMoviesButton = findViewById(R.id.link_movie_button);
        buildOccupationsAttr();

        attributes.forEach(ModifiableAttribute::bindViews);

        setupLinkedMoviesList();
    }

    private void buildNameAttr() {
        nameAttr = buildTextInputAttribute(
                R.id.edit_performer_name,
                Performer::getName,
                PerformerTransformations::setName,
                createOnTextChangedListener(s -> checkEnableStateOfMenuItems()),
                (edit, newState) -> checkCommitItemConstraints()
        );
    }

    private void buildImageAttr() {
        imageAttr = buildImageAttribute(
                R.id.edit_image,
                R.id.reset_image_button
        );
    }

    private void buildDateOfBirthAttr() {
        dateOfBirthAttr = buildDateAttribute(
                R.id.date_of_birth,
                Performer::getDateOfBirth,
                PerformerTransformations::setDateOfBirth
        );
    }

    private void buildRatingAttr() {
        buildRatingAttribute(
                R.id.edit_performer_rating,
                Performer::getRating,
                PerformerTransformations::setRating
        );
    }

    private void buildBirthNameAttr() {
        birthNameAttr = buildTextInputAttribute(
                R.id.edit_birth_name,
                Performer::getBirthName,
                PerformerTransformations::setBirthName
        );
    }

    private void buildBiographyAttr() {
        biographyAttr = buildFreeTextAttribute(
                R.id.edit_performer_biography,
                Performer::getBiography,
                PerformerTransformations::setBiography
        );
    }

    private void buildOccupationsAttr() {
        occupationsAttr = buildStringListAttribute(
                R.id.edit_occupations,
                Performer::getOccupations,
                PerformerTransformations::setOccupations
        );
    }

    private void setupLinkedMoviesList() {
        linkedMovies = new LinkedPortrayableList<>(this, linkedMoviesList);
        linkedMovies.setOnModificationUndo(() -> {
            hideKeyboard();
            ScrollViewUtils.scrollToViewIfNeeded(scrollView, linkedMoviesList);
        });
        linkedMovies.setAddModification(this::addModification);
        linkedMovies.setOnLinkedDataChanged(this::checkCommitItemConstraints);
    }

    @Override
    protected void initForCreation() {
        final Intent metaData = getIntent();
        final TemporaryProxy sourceMovie = popTemporaryProxyFromSource(metaData, SOURCE_MOVIE_ID);

        if (sourceMovie != null) {
            linkedMovies.link(sourceMovie);
        } else {
            linkedMovies.showSelectionDialog(Movie.class,
                    STORAGE.getMovies(),
                    parentPipelineKey == null,
                    this::addNewMovie
            );
        }
        imageAttr.setCustomImageFlag(false);
    }

    private void addNewMovie(final ResultHandling source, final ListContext context) {
        final Intent data = source.createIntent(MovieDetailEditActivity.class);

        data.putExtra(PARENT_PIPELINE, pipelineKey);
        data.putExtra(SOURCE_PERFORMER_ID, IntentPayloadStorage.pushTemporaryProxy(currentProxy()));

        startActivityForResult(data, result -> returnFromMovieCreation(result, context));
    }

    private void returnFromMovieCreation(final Intent result, final ListContext context) {
        PortrayableProxy proxy = IntentPayloadStorage.popTemporaryProxyFromResult(result);
        context.addElement(proxy);
    }

    @Override
    protected Performer getFromStorage(int id) {
        Performer result = null;
        if (id >= 0) {
            result = STORAGE.getPerformerById(id).orElse(null);
        }
        return result;
    }

    @Override
    protected void initForUpdate() {
        setInitialStateFrom(currentObject);
    }

    @Override
    protected String createPipeline() {
        return PerformerPipeline.beginPipeline();
    }

    private void setInitialStateFrom(Performer performer) {
        linkedMovies.setInitialState(STORAGE.getLinkedMoviesOfPerformer(performer));

        attributes.forEach(attr -> attr.setContentModel(performer));
    }


    @Override
    protected void registerListeners() {
        attributes.forEach(ModifiableAttribute::bindListeners);
        linkMoviesButton.setOnClickListener(view -> linkedMovies.showSelectionDialog(Movie.class,
                getNotLinkedMovies(),
                parentPipelineKey == null,
                this::addNewMovie));
    }


    private List<Movie> getNotLinkedMovies() {
        return STORAGE.getMovies()
                .stream()
                .filter(m -> linkedMovies.containsNot(new PersistentProxy(m)))
                .filter(linkedMovies::containsNot)
                .collect(toList());
    }

    @Override
    protected void checkEnableStateOfMenuItems() {
        checkCommitItemConstraints();
        changeWikiEnable();
    }

    @Override
    protected void checkCommitItemConstraints() {
        if (commitItem != null) {
            boolean isModified = hasModifications();
            boolean hasName = !nameAttr.hasEmptyText();
            boolean enoughMovies = mode != EditMode.CREATION
                    || linkedMovies.containsAtLeastOneElement();

            boolean enabled = isModified && hasName && enoughMovies;
            updateCommitMenuItem(enabled);
        }
    }

    @Override
    protected String getCurrentName() {
        return nameAttr.getContent();
    }

    @Override
    protected WikiQueryMode getQueryMode() {
        return WikiQueryMode.ACTOR;
    }

    @Override
    protected void updateWarning() {
        boolean emptyTitle = nameAttr.getContent().trim().isEmpty();
        boolean emptyMovies = linkedMovies.size() == 0;
        boolean errorEnabled = !emptyTitle && !emptyMovies;

        if (errorEnabled) {
            nameAttr.setError(null);
        } else {
            final List<String> warnings = new ArrayList<>();
            if (emptyTitle) {
                warnings.add(getString(R.string.warning_performer_name));
            }
            if (emptyMovies) {
                String emptyMovieWarning = getString(R.string.warning_performer_minimum_movies);

                if(mode != EditMode.CREATION) {
                    emptyMovieWarning += getString(R.string.warning_performer_self_delete);
                }

                warnings.add(emptyMovieWarning);
            }
            nameAttr.setError(AndroidStringUtils.join("\n", warnings));
        }
    }

    private void changeWikiEnable() {
        wikiItem.setEnabled(!nameAttr.hasEmptyText());
    }

    @Override
    protected void handleResultOfWikiSync(Intent result) {
        Actor actor = WikiStorage.unwrapActor(result.getStringExtra(WikiFetchActivity.RESULT_KEY));
        Bitmap bitmap = WikiStorage.retrieveImage();
        List<Modification<?>> modifications = new ArrayList<>();

        modifications.add(nameAttr.modifyValue(actor.getName()));
        if(bitmap != null) {
            bitmap = crop(bitmap, 2, 3);
            modifications.add(imageAttr.modifyValue(Pair.paired(new BitmapDrawable(getResources(), bitmap), true)));
        }
        modifications.add(dateOfBirthAttr.modifyValue(textToDate("dd MMMM yyyy", actor.getDateOfBirth())));
        modifications.add(birthNameAttr.modifyValue(actor.getBirthName()));
        modifications.add(biographyAttr.modifyValue(actor.getBiography()));
        modifications.add(occupationsAttr.modifyValue(actor.getOccupations()));

        addModification(Modification.stack(modifications));
    }

    @Override
    protected void checkCommitConditions(Runnable commit) {
        if (selfDelete()) {
            SimpleDialog.warning(this)
                    .setMessage(R.string.warning_performer_will_be_deleted, nameAttr.getContent())
                    .setTitle(R.string.warning)
                    .setPositiveButtonText(R.string.confirm)
                    .setPositiveButtonAction(this::onChangesConfirmed)
                    .setNegativeButtonText(R.string.cancel)
                    .setNegativeButtonAction(DialogFragment::dismiss)
                    .show();
        } else {
            super.checkCommitConditions(commit);
        }
    }

    private boolean selfDelete() {
        return mode != EditMode.CREATION && !linkedMovies.containsAtLeastOneElement();
    }

    private void onChangesConfirmed(final DialogFragment dialog) {
        dialog.dismiss();
        PerformerPipeline.setUpdateCore(pipelineKey, STORAGE.removePerformer(currentObject));
        final Intent resultIntent = new Intent();
        resultIntent.putExtra(RESULT_PIPELINE, pipelineKey);
        resultIntent.putExtra(RESULT_PROXY_KEY, (String) null);
        setResult(Activity.RESULT_OK, resultIntent);

        finish();
    }

    @Override
    protected void addOperationsToTransaction() {
        if (parentPipelineKey != null) {
            temporaryCreatorKey = PerformerPipeline.saveTemporaryCreator(movie -> {
                final ReversibleTransaction<Performer> transaction = STORAGE.newPerformer(movie);
                attributes.forEach(attr -> transaction.addOperation(attr.toTransformation()));
                transaction.addOperation(setPersistentLinkedMovies());
                return transaction;
            });
        }
    }

    private ReversibleTransformation<Performer> setPersistentLinkedMovies() {
        return linkedMovies.toCommitOperation(
                (p, m) -> STORAGE.link(m, p),
                (p, m) -> STORAGE.unlink(m, p)
        );
    }

    @Override
    protected ReversibleTransaction<Performer> creationTransaction() {
        return null;
    }

    @Override
    protected ReversibleTransaction<Performer> updateTransaction() {
        return STORAGE.updatePerformer(currentObject);
    }

    @Override
    protected void updatePipeline() {
        if (parentPipelineKey == null) {
            setupCore();

            PerformerPipeline.setLinkedMovies(pipelineKey, linkedMovies.getPersistentOnes());
            final List<TemporaryProxy> temporaries = linkedMovies.getTemporaries();
            temporaries.stream()
                    .filter(TemporaryProxy::hasCreatorKey)
                    .map(TemporaryProxy::getCreatorKey)
                    .map(MoviePipeline::pullTemporaryFunction)
                    .forEach(function -> PerformerPipeline.addTemporaryMovie(pipelineKey, function));
        }
    }

    private void setupCore() {
        if (mode == EditMode.CREATION) {
            PerformerPipeline.setCreationCore(pipelineKey, movie -> {
                final ReversibleTransaction<Performer> transaction = STORAGE.newPerformer(movie);
                attributes.forEach(attr -> transaction.addOperation(attr.toTransformation()));
                return transaction;
            });
        } else {
            transaction = updateTransaction();
            attributes.forEach(attr -> transaction.addOperation(attr.toTransformation()));
            PerformerPipeline.setUpdateCore(pipelineKey, transaction);
        }

        PerformerPipeline.setPersistentLinkedMovies(pipelineKey, setPersistentLinkedMovies());
    }

    @Override
    protected TemporaryProxy currentProxy() {
        final TemporaryProxy proxy = new TemporaryProxy(Performer.class,
                nameAttr.getContent(),
                imageAttr.getContent().first);

        if(currentObject != null) {
            proxy.basedOn(currentObject);
        }

        if (temporaryCreatorKey != null)
            proxy.setCreatorKey(temporaryCreatorKey);

        return proxy;
    }
}
