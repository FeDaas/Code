package de.moviemanager.ui.masterfragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.widget.SearchView;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.ObjIntConsumer;

import de.moviemanager.R;
import de.moviemanager.android.ResultHandlingActivity;
import de.moviemanager.android.ResultHandlingFragment;
import de.moviemanager.core.storage.RuntimeStorageAccess;
import de.moviemanager.data.Movie;
import de.moviemanager.data.Performer;
import de.moviemanager.data.Portrayable;
import de.moviemanager.ui.MasterActivity;
import de.moviemanager.ui.adapter.SearchListAdapter;
import de.moviemanager.ui.detail.MovieDetailActivity;
import de.moviemanager.ui.detail.PerformerDetailActivity;
import de.moviemanager.ui.search.SearchInfo;
import de.moviemanager.ui.view.InterceptibleScrollView;
import de.moviemanager.ui.view.SearchResultBlock;
import de.moviemanager.util.AndroidUtils;
import de.moviemanager.util.Listeners;
import de.util.Pair;

import static de.moviemanager.ui.MasterActivity.FRAGMENT_NAME;
import static de.moviemanager.ui.adapter.SearchListAdapter.MAX_RESULTS;
import static de.moviemanager.ui.search.PortrayableSearchActivity.openMovieSearch;
import static de.moviemanager.ui.search.PortrayableSearchActivity.openPerformerSearch;
import static de.util.Pair.paired;
import static java.util.Arrays.asList;
import static java.util.Comparator.comparing;
import static java.util.Optional.ofNullable;
import static java.util.stream.IntStream.range;

public class SearchMasterFragment extends ResultHandlingFragment {
    private static final RuntimeStorageAccess STORAGE = RuntimeStorageAccess.getInstance();

    private Activity source;
    private @StringRes
    int nameId;
    private SearchView searchBar;

    private LinearLayout blockRoot;
    private SearchResultBlock movieBlock;
    private SearchListAdapter<Movie> movieAdapter;
    private SearchResultBlock performerBlock;
    private SearchListAdapter<Performer> performerAdapter;

    private SearchInfo info;
    private TotalCount counter;

    public static SearchMasterFragment newInstance(@StringRes int nameId) {
        Bundle args = new Bundle();

        SearchMasterFragment fragment = new SearchMasterFragment();
        args.putInt(FRAGMENT_NAME, nameId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        STORAGE.updateRequiredPermissions(getActivity());
        STORAGE.openMovieManagerStorage();

        source = this.getActivity();
        return inflater.inflate(R.layout.fragment_master_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        retrieveArguments();
        bindViews(view);
        configureListAdapters();
        setupListeners();
    }

    private void retrieveArguments() {
        nameId = ofNullable(getArguments())
                .map(b -> b.getInt(FRAGMENT_NAME))
                .orElse(R.string.default_fragment_name);
    }

    private boolean onQueryChanged(final String query) {
        counter.runFilter(query, asList(
                q -> movieAdapter.filter(q),
                q -> performerAdapter.filter(q)
        ));
        return true;
    }

    private void bindViews(@NonNull View view) {
        searchBar = view.findViewById(R.id.search_bar);
        final InterceptibleScrollView scrollView = view.findViewById(R.id.search_scroll_view);
        scrollView.setOnDispatchListener(() -> AndroidUtils.closeKeyboard(source));
        blockRoot = view.findViewById(R.id.content_root);

        movieBlock = createBlock(R.string.movie_search_title,
                R.string.continue_search_in_movies
        );

        performerBlock = createBlock(R.string.performer_search_title,
                R.string.continue_search_in_performers
        );
        blockRoot.addView(movieBlock);
        blockRoot.addView(performerBlock);

        info = new SearchInfo(source);
        counter = new TotalCount(this::afterBlockUpdates);
    }

    private SearchResultBlock createBlock(@StringRes int titleId,
                                          @StringRes int continueTextId) {
        final SearchResultBlock block = new SearchResultBlock(source);
        block.setName(titleId);
        block.setContinueText(continueTextId);
        block.setVisibility(View.GONE);
        return block;
    }

    private void configureListAdapters() {
        movieAdapter = createAdapter(STORAGE.getMovies(),
                movieBlock,
                this::showMovieFrom
        );
        performerAdapter = createAdapter(STORAGE.getPerformers(),
                performerBlock,
                this::showPerformerFrom
        );
    }

    private <T extends Portrayable> SearchListAdapter<T> createAdapter(final List<T> data,
                                                                       final SearchResultBlock block,
                                                                       final Consumer<T> listClick) {
        final SearchListAdapter<T> result = new SearchListAdapter<>(source, data, MAX_RESULTS);
        result.setOnSizeChangeListener(showBlockIfNonEmpty(block));
        result.setOnQueryProcessedListener((query, size) -> counter.update(size));
        result.setOnItemClickListener(listClick);
        block.setAdapter(result);
        return result;
    }

    private IntConsumer showBlockIfNonEmpty(final SearchResultBlock block) {
        return size -> {
            int visibility = View.GONE;
            if (size > 0) {
                visibility = View.VISIBLE;
            }
            block.setVisibility(visibility);
        };
    }

    private void showMovieFrom(@NonNull Movie elem) {
        MovieDetailActivity.showAndNotifyIfOk((ResultHandlingActivity) getActivity(),
                elem,
                data -> refilterAfterEdit()
        );
    }

    private void showPerformerFrom(@NonNull Performer elem) {
        PerformerDetailActivity.showAndNotifyIfOk((ResultHandlingActivity) getActivity(),
                elem,
                data -> refilterAfterEdit()
        );
    }

    private void refilterAfterEdit() {
        final String query = searchBar.getQuery().toString();
        counter.runFilter(query, asList(
                q -> movieAdapter.refilterList(STORAGE.getMovies(), q),
                q -> performerAdapter.refilterList(STORAGE.getPerformers(), q)
        ));
    }

    private void setupListeners() {

        searchBar.setOnQueryTextListener(Listeners.liveQueryListener(source, this::onQueryChanged));

        movieBlock.setContinueListener(v -> openMovieSearch(
                searchBar.getQuery().toString(),
                this,
                result -> {
                })
        );
        performerBlock.setContinueListener(v -> openPerformerSearch(
                searchBar.getQuery().toString(),
                this,
                result -> {
                })
        );

        info.addOnClickActionTo(R.id.only_search_movies, () -> openMovieSearch(this));
        info.addOnClickActionTo(R.id.only_search_performers, () -> openPerformerSearch(this));
    }

    private void afterBlockUpdates(final String query, int totalSize) {
        info.show(query, totalSize);

        final List<View> blocks = asList(
                movieBlock,
                performerBlock
        );
        final List<SearchListAdapter<? extends Portrayable>> adapters = asList(
                movieAdapter,
                performerAdapter
        );

        blocks.forEach(blockRoot::removeView);
        range(0, adapters.size())
                .mapToObj(i -> paired(i, adapters.get(i)))
                .sorted(comparing(p -> p.getSecond().bestGuessValue(query)))
                .map(Pair::getFirst)
                .map(blocks::get)
                .forEach(blockRoot::addView);
    }

    @Override
    public void onResume() {
        super.onResume();
        final Activity activity = getActivity();
        if (activity != null) {
            MasterActivity master = (MasterActivity) activity;
            master.setTitle(nameId);
            master.setBottomNavigationTo(nameId);
        }
    }
}

class TotalCount {
    private String currentQuery;
    private AtomicInteger value;
    private final AtomicInteger pending;
    private final ObjIntConsumer<String> listener;

    TotalCount(final ObjIntConsumer<String> listener) {
        this.listener = listener;
        pending = new AtomicInteger();
        reset(null);
    }

    private void reset(final String currentQuery) {
        this.currentQuery = currentQuery;
        this.value = new AtomicInteger(0);
    }

    void runFilter(final String currentQuery, final List<Consumer<String>> actions) {
        reset(currentQuery);
        pending.set(actions.size());
        for (final Consumer<String> action : actions) {
            action.accept(currentQuery);
        }
    }

    void update(int size) {
        int stillPending = pending.decrementAndGet();
        value.addAndGet(size);

        if (stillPending == 0) {
            notifyListener();
        }
    }

    private void notifyListener() {
        if (listener != null) {
            listener.accept(currentQuery, value.get());
        }
    }
}
