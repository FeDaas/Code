package de.moviemanager.ui.wiki.fetch;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import java.util.function.Consumer;

import de.moviemanager.R;
import de.moviemanager.android.ResultHandlingFragment;
import de.moviemanager.ui.wiki.WikiStorage;
import de.moviemanager.util.AsyncUtils;
import de.moviemanager.util.autobind.AutoBind;
import de.moviemanager.util.autobind.Bind;
import de.wiki.data.Film;

import static de.moviemanager.util.AndroidStringUtils.join;
import static de.moviemanager.util.ScrollViewUtils.enableDeepScroll;
import static de.util.CollectionUtils.map;

public class WikiFilmFragment extends ResultHandlingFragment {
    private static final String FILM_OBJECT = "film_object";

    @Bind(R.id.title) private TextView title;
    @Bind(R.id.image) private ImageView image;
    @Bind(R.id.runtime) private TextView runtime;
    @Bind(R.id.description) private TextView description;
    @Bind(R.id.languages) private TextView languages;
    @Bind(R.id.releases) private TextView releases;
    @Bind(R.id.production_locations) private TextView productionLocations;

    private Film currentFilm;
    private Consumer<Bitmap> onImageLoaded;

    public static WikiFilmFragment newInstance(Film film, Consumer<Bitmap> onImageLoaded) {
        WikiFilmFragment result = new WikiFilmFragment();
        Bundle arguments = new Bundle();
        arguments.putString(FILM_OBJECT, WikiStorage.wrapFilm(film));
        result.setArguments(arguments);
        result.setOnImageLoaded(onImageLoaded);
        return result;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_wiki_sync_film, container, false);
    }

    public void setOnImageLoaded(Consumer<Bitmap> onImageLoaded) {
        this.onImageLoaded = onImageLoaded;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        AutoBind.bindAll(this, view);
        enableDeepScroll(description);
        enableDeepScroll(languages);
        enableDeepScroll(releases);
        enableDeepScroll(productionLocations);
        currentFilm = Optional.ofNullable(getArguments())
                .map(bundle -> bundle.getString(FILM_OBJECT))
                .map(WikiStorage::unwrapFilm)
                .orElse(null);
        populateUI();
    }

    private void populateUI() {
        if(currentFilm != null) {
            title.setText(currentFilm.getTitle());
            try {
                String absUrl = currentFilm.getImageURL();
                AsyncUtils.imageUrlLoader(image, onImageLoaded).execute(new URL(absUrl));
            } catch (MalformedURLException e) {
                Log.e("WFF", "Failed to load image");
            }
            runtime.setText(currentFilm.getRunningTime());
            description.setText(currentFilm.getDescription());
            languages.setText(join("\n", currentFilm.getLanguages()));
            releases.setText(join("\n",
                    map(pair -> pair.first + " | " + pair.second, currentFilm.getReleaseDates()))
            );
            productionLocations.setText(join("\n", currentFilm.getCountries()));
        }
    }
}
