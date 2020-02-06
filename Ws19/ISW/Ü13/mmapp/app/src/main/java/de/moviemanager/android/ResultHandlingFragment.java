package de.moviemanager.android;

import android.content.Intent;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ResultHandlingFragment extends Fragment implements PluginBasedResultHandling {
    private final ResultHandlingPlugin resultHandlingPlugin;

    public ResultHandlingFragment() {
        resultHandlingPlugin = new ResultHandlingPlugin(this);
    }

    @Override
    public ResultHandlingPlugin getPlugin() {
        return resultHandlingPlugin;
    }

    @Override
    public final void onActivityResult(final int requestCode,
                                       final int resultCode,
                                       @Nullable final Intent data) {
        if(resultHandlingPlugin.canProcess(requestCode, resultCode)) {
            resultHandlingPlugin.onActivityResult(requestCode, resultCode, data);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
