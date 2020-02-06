package de.moviemanager.android;

import android.content.Intent;
import android.content.res.Configuration;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.os.ConfigurationCompat;

import java.util.Locale;

public abstract class ResultHandlingActivity extends AppCompatActivity implements PluginBasedResultHandling {
    private final ResultHandlingPlugin resultHandlingPlugin;

    public ResultHandlingActivity() {
        resultHandlingPlugin = new ResultHandlingPlugin(this);
    }

    @Override
    public ResultHandlingPlugin getPlugin() {
        return resultHandlingPlugin;
    }

    @Override
    protected final void onActivityResult(final int requestCode,
                                          final int resultCode,
                                          @Nullable final Intent data) {
        if (resultHandlingPlugin.canProcess(requestCode, resultCode)) {
            resultHandlingPlugin.onActivityResult(requestCode, resultCode, data);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    protected final Locale getCurrentLocale() {
        final Configuration config = getResources().getConfiguration();
        return ConfigurationCompat.getLocales(config).get(0);
    }
}
