package de.moviemanager.android;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.util.SparseArray;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static android.app.Activity.RESULT_OK;

public class ResultHandlingPlugin implements ResultHandling {
    private final BiConsumer<Intent, Integer> startActivityForResult;
    private final Function<Class<? extends Activity>, Intent> createIntentTo;

    private final SparseArray<SparseArray<ResultHandler>> resultHolders;
    private final AtomicInteger currentRequestCode;

    public ResultHandlingPlugin(final Fragment fragment) {
        this(fragment::startActivityForResult, to -> new Intent(fragment.getActivity(), to));
    }

    ResultHandlingPlugin(final Activity activity) {
        this(activity::startActivityForResult, to -> new Intent(activity, to));
    }

    ResultHandlingPlugin(final BiConsumer<Intent, Integer> startActivityForResult,
                         final Function<Class<? extends Activity>, Intent> createIntentTo) {
        this.startActivityForResult = startActivityForResult;
        this.createIntentTo = createIntentTo;

        resultHolders = new SparseArray<>();
        currentRequestCode = new AtomicInteger();
    }


    @Override
    public void startActivityForResult(final Intent data,
                                       final ResultHandler handler) {
        startActivityForResult(data, RESULT_OK, handler);
    }

    @Override
    public void startActivityForResult(final Intent data,
                                       final int resultCode,
                                       final ResultHandler handler) {
        final SparseArray<ResultHandler> handlers = new SparseArray<>();
        handlers.put(resultCode, handler);
        startActivityForResult(data, handlers);
    }

    @Override
    public void startActivityForResult(final Intent data,
                                       final SparseArray<ResultHandler> handlers) {
        int requestCode = this.currentRequestCode.incrementAndGet();
        resultHolders.put(requestCode, handlers);
        startActivityForResult.accept(data, requestCode);
    }

    @Override
    public Intent createIntent(Class<? extends Activity> target) {
        return createIntentTo.apply(target);
    }

    public final void onActivityResult(final int requestCode,
                                       final int resultCode,
                                       @Nullable final Intent data) {
        final ResultHandler handler = getHandler(requestCode, resultCode);

        if (handler != null) {
            try {
                handler.onResult(data);
            } catch (Exception e) {
                Log.e(getClass().getSimpleName(), "ResultHandler failed!", e);
            }
        }
    }

    public boolean canProcess(final int requestCode, final int resultCode) {
        final ResultHandler handler = getHandler(requestCode, resultCode);
        return handler != null;
    }

    private ResultHandler getHandler(final int requestCode, final int resultCode) {
        ResultHandler handler = null;

        final SparseArray<ResultHandler> handlers = resultHolders.get(requestCode);
        if (handlers != null) {
            handler = handlers.get(resultCode);
        }

        return handler;
    }
}
