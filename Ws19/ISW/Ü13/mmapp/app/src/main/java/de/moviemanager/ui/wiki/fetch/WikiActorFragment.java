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
import de.wiki.data.Actor;

import static de.moviemanager.util.AndroidStringUtils.join;
import static de.moviemanager.util.ScrollViewUtils.enableDeepScroll;

public class WikiActorFragment extends ResultHandlingFragment {
    private static final String ACTOR_OBJECT = "actor_object";

    @Bind(R.id.title) private TextView title;
    @Bind(R.id.image) private ImageView image;
    @Bind(R.id.birth_name) private TextView birthName;
    @Bind(R.id.date_of_birth) private TextView dateOfBirth;
    @Bind(R.id.biography) private TextView biography;
    @Bind(R.id.occupations) private TextView occupations;

    private Actor currentActor;
    private Consumer<Bitmap> onImageLoaded;

    public static WikiActorFragment newInstance(Actor actor, Consumer<Bitmap> onImageLoaded) {
        WikiActorFragment result = new WikiActorFragment();
        Bundle arguments = new Bundle();
        arguments.putString(ACTOR_OBJECT, WikiStorage.wrapActor(actor));
        result.setArguments(arguments);
        result.setOnImageLoaded(onImageLoaded);
        return result;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_wiki_sync_actor, container, false);
    }

    public void setOnImageLoaded(Consumer<Bitmap> onImageLoaded) {
        this.onImageLoaded = onImageLoaded;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        AutoBind.bindAll(this, view);
        enableDeepScroll(biography);
        enableDeepScroll(occupations);
        currentActor = Optional.ofNullable(getArguments())
                .map(bundle -> bundle.getString(ACTOR_OBJECT))
                .map(WikiStorage::unwrapActor)
                .orElse(null);
        populateUI();
    }

    private void populateUI() {
        if(currentActor != null) {
            title.setText(currentActor.getName());
            try {
                String absUrl = currentActor.getImageURL();
                AsyncUtils.imageUrlLoader(image, onImageLoaded).execute(new URL(absUrl));
            } catch (MalformedURLException e) {
                Log.e("WFF", "Failed to load image");
            }
            birthName.setText(currentActor.getBirthName());
            dateOfBirth.setText(currentActor.getDateOfBirth());
            biography.setText(currentActor.getBiography());
            occupations.setText(join("\n", currentActor.getOccupations()));
        }
    }
}
