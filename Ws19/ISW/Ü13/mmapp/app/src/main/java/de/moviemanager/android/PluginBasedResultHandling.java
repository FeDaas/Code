package de.moviemanager.android;

import android.app.Activity;
import android.content.Intent;
import android.util.SparseArray;

import static android.app.Activity.RESULT_OK;

public interface PluginBasedResultHandling extends ResultHandling {
    ResultHandlingPlugin getPlugin();

    @Override
    default void startActivityForResult(final Intent data,
                                       final ResultHandler handler) {
        getPlugin().startActivityForResult(data, RESULT_OK, handler);
    }

    @Override
    default void startActivityForResult(final Intent data,
                                       final int resultCode,
                                       final ResultHandler handler) {
        getPlugin().startActivityForResult(data, resultCode, handler);
    }

    @Override
    default void startActivityForResult(final Intent data,
                                       final SparseArray<ResultHandler> handlers) {
        getPlugin().startActivityForResult(data, handlers);
    }

    @Override
    default Intent createIntent(Class<? extends Activity> target) {
        return getPlugin().createIntent(target);
    }
}
