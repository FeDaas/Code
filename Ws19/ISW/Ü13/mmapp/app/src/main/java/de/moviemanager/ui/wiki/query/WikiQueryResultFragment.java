package de.moviemanager.ui.wiki.query;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import de.moviemanager.R;
import de.moviemanager.android.ResultHandlingFragment;
import de.moviemanager.util.RecyclerViewUtils;
import de.moviemanager.util.autobind.AutoBind;
import de.moviemanager.util.autobind.Bind;
import de.util.CollectionUtils;

import static de.moviemanager.ui.adapter.selection.SelectionAdapters.singleSelection;
import static java.util.stream.Collectors.toList;

public class WikiQueryResultFragment extends ResultHandlingFragment {
    private static final String QUERY_RESULT_LIST = "query_result_list";

    static WikiQueryResultFragment newInstance(final List<JsonObject> objects,
                                               final Consumer<JsonObject> onSelected) {
        final WikiQueryResultFragment fragment = new WikiQueryResultFragment();
        final Bundle arguments = new Bundle();
        arguments.putStringArrayList(QUERY_RESULT_LIST, reduceJsonObjectsToString(objects));
        fragment.setArguments(arguments);
        fragment.onSelected = onSelected;
        return fragment;
    }

    private static ArrayList<String> reduceJsonObjectsToString(final List<JsonObject> objects) {
        return new ArrayList<>(CollectionUtils.map(JsonObject::toString, objects));
    }

    @Bind(R.id.show_query_results) private RecyclerView showResults;
    private List<JsonObject> result;
    private Consumer<JsonObject> onSelected;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        result = Optional.ofNullable(getArguments())
                .map(args -> args.getStringArrayList(QUERY_RESULT_LIST))
                .orElse(new ArrayList<>())
                .stream()
                .map(JsonParser::parseString)
                .map(JsonElement::getAsJsonObject)
                .collect(toList());

        return inflater.inflate(R.layout.fragment_wiki_sync_query_result, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        AutoBind.bindAll(this, view);
        setupResults();
    }

    private void setupResults() {
        RecyclerViewUtils.setLinearLayoutTo(getContext(), showResults);
        showResults.setAdapter(singleSelection(getContext(), this::currentSelected)
                .setData(result)
                .setElementLayoutId(R.layout.listitem_wiki_sync_query_result)
                .setContentBinder(this::bindListItem)
                .setFilterCriterion((element, constraint) -> true)
                .setOrderCriterion(Comparator.comparing(result::indexOf))
                .build());
    }

    private void currentSelected(JsonObject selectedResult) {
        if (onSelected != null) {
            onSelected.accept(selectedResult);
        }
    }

    private void bindListItem(ViewGroup parent, JsonObject element) {
        final TextView queryResultName = parent.findViewById(R.id.query_result_name);
        queryResultName.setText(getTitle(element));
    }

    private static String getTitle(final JsonObject element) {
        String result = "<EMPTY>";

        final JsonElement titleElement = element.get("title");
        if (titleElement != null) {
            result = titleElement.getAsString();
        }

        return result;
    }
}
