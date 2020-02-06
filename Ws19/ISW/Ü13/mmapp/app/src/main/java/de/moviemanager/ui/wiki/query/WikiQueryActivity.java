package de.moviemanager.ui.wiki.query;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.ColorRes;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;

import com.google.gson.JsonObject;

import java.util.List;
import java.util.Optional;

import de.moviemanager.R;
import de.moviemanager.ui.wiki.NetworkActivity;
import de.moviemanager.ui.wiki.NetworkInfoFragment;
import de.moviemanager.ui.wiki.fetch.WikiFetchActivity;
import de.moviemanager.util.autobind.AutoBind;
import de.moviemanager.util.autobind.Bind;
import de.wiki.WikiException;

import static de.moviemanager.ui.wiki.WikiStorage.unwrapQueryResult;
import static de.moviemanager.ui.wiki.query.WikiQueryMode.UNDEFINED;
import static de.moviemanager.ui.wiki.query.WikiQueryService.PARAMETER_ID;
import static de.moviemanager.ui.wiki.query.WikiQueryService.PARAMETER_QUERY;
import static de.moviemanager.ui.wiki.query.WikiQueryService.PARAMETER_TYPE;
import static de.moviemanager.ui.wiki.query.WikiQueryService.RESULT_LIST;
import static de.moviemanager.ui.wiki.query.WikiQueryService.RESULT_NON_EMPTY;
import static de.moviemanager.ui.wiki.query.WikiQueryService.RESULT_STATE;
import static de.moviemanager.util.Listeners.submitQueryListener;
import static java.util.Optional.ofNullable;

public class WikiQueryActivity extends NetworkActivity<String> {
    public static final String EXTRA_INITIAL_QUERY = "initial_query";
    public static final String EXTRA_QUERY_MODE = "query_mode";

    @Bind(R.id.search) private SearchView search;
    @Bind(R.id.refresh_button) private ImageView refreshButton;
    @Bind(R.id.forward_button) private Button forwardButton;
    @Bind(R.id.forward_arrow) private ImageView forwardArrow;

    private WikiQueryMode mode = UNDEFINED;
    private JsonObject selected;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wiki_sync_query);
        AutoBind.bindAll(this);

        search.setOnQueryTextListener(submitQueryListener(this, this::onQueryConfirmed));
        showFragment(NetworkInfoFragment.loadingFragment(this));
        refreshButton.setOnClickListener(view -> onQueryConfirmed("" + search.getQuery()));

        setupFromExtras();
        setForwardListener(this::openFetchActivity);
    }

    private void setupFromExtras() {
        final Optional<Intent> extraOptional = ofNullable(getIntent());

        if (extraOptional.isPresent()) {
            Intent extras = extraOptional.get();
            mode = WikiQueryMode.fromExtra(extras, EXTRA_QUERY_MODE);
            search.setQuery(extras.getStringExtra(EXTRA_INITIAL_QUERY), true);
        } else {
            throw new WikiException("Query Activity was called without parameters!");
        }
    }

    private void setForwardListener(Runnable action) {
        forwardButton.setOnClickListener(view -> action.run());
        forwardArrow.setOnClickListener(view -> action.run());
    }

    private void openFetchActivity() {
        Intent intent = new Intent(this, WikiFetchActivity.class);
        Bundle extra = new Bundle();
        extra.putInt(WikiFetchActivity.EXTRA_QUERY_MODE, mode.ordinal());
        extra.putString(WikiFetchActivity.EXTRA_PAGE_STRING, selected.toString());
        intent.putExtras(extra);
        startActivityForResult(intent, this::onFetchSuccess);
    }

    private void onFetchSuccess(Intent result) {
        setResult(Activity.RESULT_OK, result);
        finish();
    }


    @Override
    protected int getFragmentContainerId() {
        return R.id.fragment_container;
    }

    private boolean onQueryConfirmed(final String query) {
        disableForward();
        sendRequestIfInternet(query);
        return true;
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
        return WikiQueryService.RESULT_CALLBACK;
    }

    @Override
    protected void sendRequestToService(int requestId, String requestData) {
        Intent requestList = new Intent(this, WikiQueryService.class);
        requestList.putExtra(PARAMETER_ID, requestId);
        requestList.putExtra(PARAMETER_TYPE, mode.ordinal());
        requestList.putExtra(PARAMETER_QUERY, requestData);
        startService(requestList);
    }

    @Override
    protected void latestServiceResponse(Bundle extras) {
        int resultCode = extras.getInt(RESULT_STATE);
        if (resultCode == RESULT_NON_EMPTY) {
            List<JsonObject> resultList = unwrapQueryResult(extras.getString(RESULT_LIST));
            showFragment(WikiQueryResultFragment.newInstance(resultList, this::onSelected));
        } else {
            showFragment(NetworkInfoFragment.noResultFragment(this));
        }
    }

    private void onSelected(JsonObject selected) {
        this.selected = selected;
        setForwardEnabled(selected != null);
    }
}
