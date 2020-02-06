package de.moviemanager.ui.search;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import de.moviemanager.R;
import de.moviemanager.android.ResultHandler;
import de.moviemanager.android.ResultHandling;
import de.moviemanager.android.ResultHandlingActivity;
import de.moviemanager.core.storage.RuntimeStorageAccess;
import de.moviemanager.data.Portrayable;
import de.moviemanager.ui.adapter.SearchListAdapter;

import static de.moviemanager.util.Listeners.liveQueryListener;
import static de.moviemanager.util.RecyclerViewUtils.setLinearLayoutTo;

public abstract class PortrayableSearchActivity<T extends Portrayable> extends ResultHandlingActivity {
    private static final String INITIAL_QUERY = "initial_query";
    private static final String TITLE_ID = "title_id";
    private static final String QUERY_HINT_ID = "query_hint_id";

    static final RuntimeStorageAccess STORAGE = RuntimeStorageAccess.getInstance();
    private final int layoutId;
    private String initialQuery;
    @StringRes private int queryHintId;
    @StringRes private int titleId;

    private SearchView searchBar;
    private RecyclerView resultList;
    private SearchListAdapter<T> adapter;
    private SearchInfo infoBox;

    private boolean updated;

    public static void openMovieSearch(ResultHandling source) {
        openMovieSearch("", source, result -> {});
    }

    public static void openMovieSearch(String initialQuery,
                                       ResultHandling source,
                                       ResultHandler handler) {
        Intent data = source.createIntent(MovieSearchActivity.class);
        data.putExtra(INITIAL_QUERY, initialQuery);
        data.putExtra(TITLE_ID, R.string.movie_search_title);
        data.putExtra(QUERY_HINT_ID, R.string.search_in_movies_hint);
        source.startActivityForResult(data, handler);
    }

    public static void openPerformerSearch(ResultHandling source) {
        openPerformerSearch("", source, result -> {});
    }


    public static void openPerformerSearch(String initialQuery,
                                           ResultHandling source,
                                           ResultHandler handler) {
        Intent data = source.createIntent(PerformerSearchActivity.class);
        data.putExtra(INITIAL_QUERY, initialQuery);
        data.putExtra(TITLE_ID, R.string.performer_search_title);
        data.putExtra(QUERY_HINT_ID, R.string.search_in_performers_hint);
        source.startActivityForResult(data, handler);
    }

    public PortrayableSearchActivity() {
        this.layoutId = R.layout.activity_search_list;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layoutId);
        STORAGE.updateRequiredPermissions(this);
        STORAGE.openMovieManagerStorage();

        getParametersFromIntent();
        bindViews();
        setupList();
        setupActionBar();
        setupSearchBar();
    }

    private void getParametersFromIntent() {
        final Intent intent = getIntent();
        if(intent != null) {
            initialQuery = intent.getStringExtra(INITIAL_QUERY);
            titleId = intent.getIntExtra(TITLE_ID, R.string.default_search_title);
            queryHintId = intent.getIntExtra(QUERY_HINT_ID, R.string.default_query_text);
        }
    }

    private void bindViews() {
        searchBar = findViewById(R.id.search_bar);
        resultList = findViewById(R.id.search_results);
        infoBox = new SearchInfo(this);
    }

    private void setupSearchBar() {
        searchBar.setIconifiedByDefault(false);
        searchBar.setQuery(initialQuery, true);
        searchBar.setQueryHint(getString(queryHintId));
    }

    private void setupList() {
        resultList.setVisibility(View.VISIBLE);
        setLinearLayoutTo(this, resultList);

        configureListAdapter();
        setListeners();
    }

    private void configureListAdapter() {
        adapter = new SearchListAdapter<>(this,
                getListFromStorage(),
                SearchListAdapter.UNLIMITED,
                false
        );
        adapter.setOnQueryProcessedListener(infoBox::show);
        resultList.setAdapter(adapter);
    }

    protected abstract List<T> getListFromStorage();

    private void setListeners() {
        adapter.setOnItemClickListener(this::showFrom);
        searchBar.setOnQueryTextListener(liveQueryListener(this, this::onQueryChanged));
    }

    private boolean onQueryChanged(final String query) {
        adapter.filter(query);
        return true;
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(titleId);
        }
    }

    void updateAfterEdit() {
        updateUIWithNewData();
        updated = true;
    }

    private void updateUIWithNewData() {
        adapter.refilterList(getListFromStorage(), searchBar.getQuery().toString());
    }

    protected abstract void showFrom(T elem);

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        final Intent backIntent = new Intent();
        setResult(updated ? RESULT_OK : RESULT_CANCELED, backIntent);
        super.onBackPressed();
    }
}

