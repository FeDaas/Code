package de.moviemanager.android;

import android.app.Activity;
import android.content.Intent;
import android.util.SparseArray;

public interface ResultHandling {
    void startActivityForResult(Intent data, ResultHandler handler);
    void startActivityForResult(Intent data, int resultCode, ResultHandler handler);
    void startActivityForResult(Intent data, SparseArray<ResultHandler> handlers);
    Intent createIntent(Class<? extends Activity> target);
}
