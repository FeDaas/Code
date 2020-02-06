package de.moviemanager.ui.detail;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;

import java.util.List;

import de.moviemanager.R;
import de.moviemanager.android.ResultHandlingActivity;
import de.moviemanager.core.storage.RuntimeStorageAccess;
import de.moviemanager.data.Portrayable;
import de.moviemanager.data.proxy.PersistentProxy;
import de.moviemanager.data.proxy.PortrayableProxy;

import static java.util.stream.Collectors.toList;

public abstract class PortrayableDetailActivity<T extends Portrayable> extends ResultHandlingActivity {
    public static final String INITIAL_PORTRAYABLE = "portrayable";
    static final RuntimeStorageAccess STORAGE = RuntimeStorageAccess.getInstance();

    private final int layoutId;
    ActionBar actionBar;
    private final Class<? extends Activity> editActivity;

    T model;
    boolean updated;

    public PortrayableDetailActivity(@LayoutRes int layoutId, final Class<? extends Activity> editActivity) {
        this.layoutId = layoutId;
        this.editActivity = editActivity;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layoutId);

        model = getIntent().getParcelableExtra(INITIAL_PORTRAYABLE);

        STORAGE.areRequiredPermissionsGrantedFor(this);
        STORAGE.openMovieManagerStorage();

        if (model == null) {
            finish();
        } else {
            updated = false;

            bindViews();
            setupLists();
            setListeners();
            setupActionBar();
            updateUIWithModelData();
        }
    }

    protected abstract void bindViews();

    protected abstract void setupLists();

    List<PortrayableProxy> toProxyList(final List<? extends Portrayable> portrayables) {
        return portrayables.stream()
                .map(PersistentProxy::new)
                .collect(toList());
    }

    protected abstract void setListeners();

    private void setupActionBar() {
        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        boolean result = true;

        switch (id) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.edit:
                final Intent intent = new Intent(this, editActivity);
                intent.putExtra(INITIAL_PORTRAYABLE, model);
                startActivityForResult(intent, this::updateAfterEdit);
                break;
            default:
                result = super.onOptionsItemSelected(item);
        }

        return result;
    }

    protected abstract void updateAfterEdit(Intent result);

    protected abstract void updateAfterLinkedDetails(final Intent result);

    void finishAfterDeletion(T old) {
        final Intent resultIntent = new Intent();
        resultIntent.putExtra(INITIAL_PORTRAYABLE, (T) null);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    protected abstract void updateUIWithModelData();

    @Override
    public void onBackPressed() {
        final Intent resultIntent = new Intent();
        resultIntent.putExtra(INITIAL_PORTRAYABLE, model);
        setResult(updated ? RESULT_OK : RESULT_CANCELED, resultIntent);
        super.onBackPressed();
    }

}

