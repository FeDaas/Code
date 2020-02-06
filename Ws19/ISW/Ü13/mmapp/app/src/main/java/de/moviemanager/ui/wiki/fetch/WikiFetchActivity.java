package de.moviemanager.ui.wiki.fetch;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.ColorRes;
import androidx.annotation.Nullable;

import com.google.gson.JsonObject;

import java.util.Optional;

import de.moviemanager.R;
import de.moviemanager.ui.wiki.NetworkActivity;
import de.moviemanager.ui.wiki.NetworkInfoFragment;
import de.moviemanager.ui.wiki.WikiStorage;
import de.moviemanager.ui.wiki.query.WikiQueryMode;
import de.moviemanager.util.autobind.AutoBind;
import de.moviemanager.util.autobind.Bind;
import de.wiki.WikiException;
import de.wiki.data.Actor;
import de.wiki.data.Film;

import static com.google.gson.JsonParser.parseString;
import static de.moviemanager.ui.wiki.fetch.WikiFetchService.PARAMETER_WIKI_PAGE_JSON;
import static de.moviemanager.ui.wiki.query.WikiQueryMode.ACTOR;
import static de.moviemanager.ui.wiki.query.WikiQueryMode.FILM;
import static de.moviemanager.ui.wiki.query.WikiQueryMode.UNDEFINED;
import static de.moviemanager.ui.wiki.query.WikiQueryService.PARAMETER_ID;
import static de.moviemanager.ui.wiki.query.WikiQueryService.PARAMETER_TYPE;
import static de.moviemanager.ui.wiki.query.WikiQueryService.RESULT_NON_EMPTY;
import static de.moviemanager.ui.wiki.query.WikiQueryService.RESULT_STATE;
import static java.util.Optional.ofNullable;

public class WikiFetchActivity extends NetworkActivity<JsonObject> {
    public static final String EXTRA_PAGE_STRING = "page_string";
    public static final String EXTRA_QUERY_MODE = "query_mode";

    public static final String RESULT_MODE = "result_mode";
    public static final String RESULT_KEY = "result_key";

    @Bind(R.id.refresh_button) private ImageView refreshButton;
    @Bind(R.id.forward_button) private Button forwardButton;
    @Bind(R.id.forward_arrow) private ImageView forwardArrow;
    @Bind(R.id.backward_button) private Button backwardButton;
    @Bind(R.id.backward_arrow) private ImageView backwardArrow;

    private WikiQueryMode mode = UNDEFINED;
    private JsonObject page;
    private Actor currentActor;
    private Film currentFilm;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wiki_sync_fetch);
        AutoBind.bindAll(this);

        setupFromExtras();
        refreshButton.setOnClickListener(v -> refetch());
        setBackwardListener(this::onBackPressed);
        setForwardListener(this::finishWithSuccess);
        refetch();
    }

    private void setupFromExtras() {
        final Optional<Intent> extraOptional = ofNullable(getIntent());

        if (extraOptional.isPresent()) {
            Intent extras = extraOptional.get();
            mode = WikiQueryMode.fromExtra(extras, EXTRA_QUERY_MODE);
            page = parseString(extras.getStringExtra(EXTRA_PAGE_STRING)).getAsJsonObject();
        } else {
            throw new WikiException("Fetch Activity was called without parameters!");
        }
    }

    private void setBackwardListener(Runnable action) {
        backwardArrow.setOnClickListener(view -> action.run());
        backwardButton.setOnClickListener(view -> action.run());
    }

    private void setForwardListener(Runnable action) {
        forwardButton.setOnClickListener(view -> action.run());
        forwardArrow.setOnClickListener(view -> action.run());
    }

    private void refetch() {
        sendRequestIfInternet(page);
    }

    private void finishWithSuccess() {
        Intent result = new Intent();
        if(mode == ACTOR) {
            result.putExtra(RESULT_KEY, WikiStorage.wrapActor(currentActor));
        } else {
            result.putExtra(RESULT_KEY, WikiStorage.wrapFilm(currentFilm));
        }
        result.putExtra(RESULT_MODE, mode.ordinal());
        setResult(Activity.RESULT_OK, result);
        finish();
    }

    @Override
    protected int getFragmentContainerId() {
        return R.id.fragment_container;
    }

    private void disableForward() {
        setForwardEnabled(false);
    }

    private void setForwardEnabled(boolean enabled) {
        @ColorRes int colorId = enabled ? R.color.icon_enabled_fill : R.color.icon_disabled_fill;
        forwardButton.setEnabled(enabled);
        forwardButton.setTextColor(getColor(colorId));
        forwardArrow.setEnabled(enabled);
    }

    @Override
    protected String callbackIdentifier() {
        return WikiFetchService.RESULT_CALLBACK;
    }

    @Override
    protected void sendRequestToService(int requestId, JsonObject requestData) {
        disableForward();
        Intent requestList = new Intent(this, WikiFetchService.class);
        requestList.putExtra(PARAMETER_ID, requestId);
        requestList.putExtra(PARAMETER_TYPE, mode.ordinal());
        requestList.putExtra(PARAMETER_WIKI_PAGE_JSON, requestData.toString());
        startService(requestList);
    }

    @Override
    protected void latestServiceResponse(Bundle extras) {
        int resultCode = extras.getInt(RESULT_STATE);
        if (resultCode == RESULT_NON_EMPTY) {
            if(mode == ACTOR) {
                enableForward();
                currentActor = WikiStorage.unwrapActor(extras.getString(RESULT_KEY));
                showFragment(WikiActorFragment.newInstance(currentActor, WikiStorage::storeImage));
            } else if(mode == FILM) {
                enableForward();
                currentFilm = WikiStorage.unwrapFilm(extras.getString(RESULT_KEY));
                showFragment(WikiFilmFragment.newInstance(currentFilm, WikiStorage::storeImage));
            }
        } else {
            showFragment(NetworkInfoFragment.noResultFragment(this));
        }
    }

    private void enableForward() {
        setForwardEnabled(true);
    }

}
