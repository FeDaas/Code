package de.moviemanager.ui.detail;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ScrollView;

import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.DialogFragment;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.DoubleFunction;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;

import de.moviemanager.R;
import de.moviemanager.core.storage.RuntimeStorageAccess;
import de.moviemanager.core.storage.temporary.IntentPayloadStorage;
import de.moviemanager.data.Portrayable;
import de.moviemanager.data.proxy.TemporaryProxy;
import de.moviemanager.ui.detail.modifiable.BaseBuilder;
import de.moviemanager.ui.detail.modifiable.DateAttribute;
import de.moviemanager.ui.detail.modifiable.FreeTextAttribute;
import de.moviemanager.ui.detail.modifiable.ImageAttribute;
import de.moviemanager.ui.detail.modifiable.ModifiableAppCompatActivity;
import de.moviemanager.ui.detail.modifiable.ModifiableAttribute;
import de.moviemanager.ui.detail.modifiable.RatingAttribute;
import de.moviemanager.ui.detail.modifiable.StringListAttribute;
import de.moviemanager.ui.detail.modifiable.TextInputAttribute;
import de.moviemanager.ui.dialog.SimpleDialog;
import de.moviemanager.ui.view.DateSelectionView;
import de.moviemanager.ui.wiki.query.WikiQueryActivity;
import de.moviemanager.ui.wiki.query.WikiQueryMode;
import de.moviemanager.util.AndroidUtils;
import de.moviemanager.util.DrawableUtils;
import de.util.operationflow.ReversibleOperations.ReversibleTransformation;
import de.util.operationflow.ReversibleTransaction;

import static de.moviemanager.data.ImagePyramid.ImageSize.LARGE;
import static de.moviemanager.ui.detail.PortrayableDetailActivity.INITIAL_PORTRAYABLE;
import static de.moviemanager.util.Listeners.createOnTextChangedListener;

public abstract class PortrayableDetailEditActivity<T extends Portrayable>
        extends ModifiableAppCompatActivity {
    static final RuntimeStorageAccess STORAGE = RuntimeStorageAccess.getInstance();
    private static final Portrayable NO_PORTRAYABLE = null;

    public static final String RESULT_PROXY_KEY = "result_proxy_key";
    static final String PARENT_PIPELINE = "parent_pipeline";
    public static final String RESULT_PIPELINE = "result_pipeline";

    enum EditMode {
        CREATION, UPDATE
    }

    @LayoutRes private final int layout;

    ScrollView scrollView;

    MenuItem wikiItem;
    private MenuItem undoItem;
    MenuItem commitItem;

    ImageAttribute<T> imageAttr;
    final List<ModifiableAttribute<T, ?>> attributes;
    EditMode mode;

    String pipelineKey;
    String parentPipelineKey;
    T currentObject;

    ReversibleTransaction<T> transaction;

    public PortrayableDetailEditActivity(@LayoutRes int layout) {
        this.layout = layout;
        this.attributes = new ArrayList<>();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout);
        STORAGE.updateRequiredPermissions(this);
        STORAGE.openMovieManagerStorage();

        bindViews();
        initActionBar();
        decideMode();
        registerListeners();
        updateWarning();
        System.out.print(this);
        DateSelectionView edit_due_date = this.findViewById(R.id.edit_due_date);
        if(edit_due_date.getDate() != null)
        {
            edit_due_date.hideDateSettings();
        }
    }

    protected abstract void bindViews();

    TextInputAttribute<T> buildTextInputAttribute(@IdRes int id,
                                                  Function<T, String> getter,
                                                  Function<String, ReversibleTransformation<T>> setter) {
        return buildTextInputAttribute(
                id,
                getter,
                setter,
                createOnTextChangedListener(s -> {
                }),
                (e, s) -> {
                });
    }

    TextInputAttribute<T> buildTextInputAttribute(@IdRes int id,
                                                  Function<T, String> getter,
                                                  Function<String, ReversibleTransformation<T>> setter,
                                                  TextWatcher textChangeListener,
                                                  BiConsumer<EditText, String> inputValidation
    ) {
        final TextInputAttribute<T> attr = ModifiableAttribute.<T>createTextInputAttribute(this)
                .setTextChangeListener(textChangeListener)
                .setInputValidation(inputValidation)
                .setConstructor(TextInputAttribute::new)
                .setRoot(scrollView)
                .setViewId(id)
                .setSource(setter)
                .setContentExtractor(getter)
                .build();
        attributes.add(attr);
        return attr;
    }

    ImageAttribute<T> buildImageAttribute(@IdRes int imageId, @IdRes int removeId) {
        final ImageAttribute<T> attr = ModifiableAttribute.<T>createImageAttribute(this)
                .setRoot(scrollView)
                .setImageId(imageId)
                .setResetButtonId(removeId)
                .setSource(p -> DrawableUtils.setImageInStorage(p.first, p.second))
                .setContentExtractor(p -> STORAGE.getImage(this, p, LARGE))
                .build();
        attributes.add(attr);
        return attr;
    }

    DateAttribute<T> buildDateAttribute(@IdRes int id,
                                        Function<T, Date> getter,
                                        Function<Date, ReversibleTransformation<T>> setter) {
        return buildAttribute(DateAttribute::new, id, getter, setter);
    }

    private <X, A extends ModifiableAttribute<T, X>> A buildAttribute(
            BaseBuilder.TriFunction<ModifiableAppCompatActivity, ScrollView, Integer, A> constructor,
            @IdRes int id,
            Function<T, X> getter,
            Function<X, ReversibleTransformation<T>> setter) {
        final A attr = new BaseBuilder<T, X, A>(this)
                .setConstructor(constructor)
                .setRoot(scrollView)
                .setViewId(id)
                .setSource(setter)
                .setContentExtractor(getter)
                .build();
        attributes.add(attr);
        return attr;
    }

    RatingAttribute<T> buildRatingAttribute(
            @IdRes int id,
            ToDoubleFunction<T> getter,
            DoubleFunction<ReversibleTransformation<T>> setter) {
        return buildAttribute(RatingAttribute::new, id, getter::applyAsDouble, setter::apply);
    }

    StringListAttribute<T> buildStringListAttribute(
            @IdRes int id,
            Function<T, List<String>> getter,
            Function<List<String>, ReversibleTransformation<T>> setter) {
        return buildAttribute(StringListAttribute::new, id, getter, setter);
    }

    FreeTextAttribute<T> buildFreeTextAttribute(
            @IdRes int id,
            Function<T, String> getter,
            Function<String, ReversibleTransformation<T>> setter) {
        return buildAttribute(FreeTextAttribute::new, id, getter, setter);
    }

    private void initActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void decideMode() {
        final Intent metaData = getIntent();
        final Portrayable portrayable = metaData.getParcelableExtra(INITIAL_PORTRAYABLE);
        currentObject = getFromStorage(portrayable == null ? -1 : portrayable.id());
        pipelineKey = createPipeline();
        parentPipelineKey = metaData.getStringExtra(PARENT_PIPELINE);

        if (currentObject == NO_PORTRAYABLE) {
            mode = EditMode.CREATION;
            initForCreation();
        } else {
            mode = EditMode.UPDATE;
            initForUpdate();
        }
    }

    protected abstract T getFromStorage(final int id);

    protected abstract void initForCreation();

    protected abstract String createPipeline();

    protected abstract void initForUpdate();

    protected abstract void registerListeners();

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail_edit_menu, menu);
        wikiItem = menu.findItem(R.id.wiki_sync);
        undoItem = menu.findItem(R.id.undo);
        commitItem = menu.findItem(R.id.commit);
        undoItem.setEnabled(false);
        checkEnableStateOfMenuItems();
        return true;
    }

    protected abstract void checkEnableStateOfMenuItems();

    protected abstract void checkCommitItemConstraints();

    void updateCommitMenuItem(boolean enable) {
        commitItem.setEnabled(enable);
        updateWarning();
    }

    protected abstract void updateWarning();

    @Override
    protected void onModificationsChanged() {
        undoItem.setEnabled(hasModifications());
        checkCommitItemConstraints();
    }

    @Override
    public boolean dispatchTouchEvent(final MotionEvent event) {
        checkCommitItemConstraints();
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            final View v = getCurrentFocus();
            if (v instanceof EditText) {
                final Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                clearFocusIfNecessary(v, outRect, event);
            }
        }
        return super.dispatchTouchEvent(event);
    }

    private void clearFocusIfNecessary(final View focusedView,
                                       final Rect outRect,
                                       final MotionEvent event) {
        if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
            AndroidUtils.closeKeyboard(this, focusedView);
            checkCommitItemConstraints();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                return cancel();
            case R.id.wiki_sync:
                return syncWithWiki();
            case R.id.undo:
                return undoLastModification();
            case R.id.commit:
                return commitChanges();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean cancel() {
        hideKeyboard();
        onBackPressed();
        return true;
    }

    private boolean syncWithWiki() {
        Intent intent = new Intent(this, WikiQueryActivity.class);
        Bundle extras = new Bundle();
        extras.putString(WikiQueryActivity.EXTRA_INITIAL_QUERY, getCurrentName());
        extras.putInt(WikiQueryActivity.EXTRA_QUERY_MODE, getQueryMode().ordinal());
        intent.putExtras(extras);
        startActivityForResult(intent, this::handleResultOfWikiSync);
        return true;
    }

    protected abstract void handleResultOfWikiSync(final Intent result);

    protected abstract String getCurrentName();

    protected abstract WikiQueryMode getQueryMode();

    private boolean undoLastModification() {
        hideKeyboard();
        if (hasModifications())
            removeLastModification().undo();
        return true;
    }

    private boolean commitChanges() {
        hideKeyboard();
        if (hasModifications()) {
            checkCommitConditions(() -> {
                transaction = beginTransaction();
                addOperationsToTransaction();
                updatePipeline();

                final Intent resultIntent = new Intent();
                resultIntent.putExtra(RESULT_PIPELINE, pipelineKey);
                resultIntent.putExtra(RESULT_PROXY_KEY, IntentPayloadStorage.pushTemporaryProxy(currentProxy()));
                setResult(Activity.RESULT_OK, resultIntent);

                finish();
            });
        }

        return true;
    }

    private ReversibleTransaction<T> beginTransaction() {
        ReversibleTransaction<T> result;
        if (mode == EditMode.CREATION) {
            result = creationTransaction();
        } else {
            result = updateTransaction();
        }
        return result;
    }

    void checkCommitConditions(final Runnable commit) {
        commit.run();
    }

    protected abstract ReversibleTransaction<T> creationTransaction();

    protected abstract ReversibleTransaction<T> updateTransaction();

    protected abstract void addOperationsToTransaction();

    protected abstract void updatePipeline();

    protected abstract TemporaryProxy currentProxy();

    @Override
    public void onBackPressed() {
        if (hasModifications())
            SimpleDialog.warning(this)
                    .setMessage(R.string.discard_changes)
                    .setPositiveButtonText(R.string.yes)
                    .setPositiveButtonAction(dialog -> discardChanges())
                    .setNegativeButtonText(R.string.no)
                    .setNegativeButtonAction(DialogFragment::dismiss)
                    .show();
        else {
            discardChanges();
        }
    }

    private void discardChanges() {
        setResult(Activity.RESULT_CANCELED, new Intent());
        super.onBackPressed();
    }
}
