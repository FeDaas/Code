package de.moviemanager.ui.wiki.query;

import android.app.IntentService;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import de.moviemanager.ui.wiki.WikiStorage;
import de.util.Pair;
import de.wiki.MediaWiki;

public class WikiQueryService extends IntentService {
    public static final int DEFAULT_REQUEST_ID = -1;

    public static final String PARAMETER_ID = "parameter_id";
    public static final String PARAMETER_QUERY = "parameter_query";
    public static final String PARAMETER_TYPE = "parameter_type";

    public static final int RESULT_EMPTY = 0;
    public static final int RESULT_NON_EMPTY = 1;

    public static final String RESULT_ID = "result_id";
    public static final String RESULT_STATE = "result_state";
    public static final String RESULT_LIST = "result_list";
    public static final String RESULT_CALLBACK = "de.moviemanager.ui.wiki.query";

    public WikiQueryService() {
        super(WikiQueryService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        final List<JsonObject> result = new ArrayList<>();
        int requestId = DEFAULT_REQUEST_ID;
        int resultCode = RESULT_EMPTY;

        if (intent != null) {
            Pair<Integer, Integer> idCodePair = handleRequest(intent, result);
            requestId = idCodePair.first;
            resultCode = idCodePair.second;
        }

        publishResult(requestId, resultCode, result);
    }

    private static Pair<Integer, Integer> handleRequest(@NonNull Intent request,
                                                        final List<JsonObject> resultList) {
        int resultCode = RESULT_NON_EMPTY;
        int id = request.getIntExtra(PARAMETER_ID, DEFAULT_REQUEST_ID);
        final String query = request.getStringExtra(PARAMETER_QUERY);
        final WikiQueryMode type = WikiQueryMode.fromExtra(request, PARAMETER_TYPE);

        if (type == WikiQueryMode.ACTOR) {
            resultList.addAll(queryActors(query));
        } else if (type == WikiQueryMode.FILM) {
            resultList.addAll(queryFilms(query));
        } else {
            resultCode = RESULT_EMPTY;
        }

        if(resultList.isEmpty()) {
            resultCode = RESULT_EMPTY;
        }

        return Pair.paired(id, resultCode);
    }

    private static List<JsonObject> queryActors(final String query) {
        return query(query, MediaWiki::getActorWikiPagesByName);
    }

    private static List<JsonObject> query(final String query,
                                          final Function<String, Optional<List<JsonObject>>> retriever) {
        List<JsonObject> result;
        if (query == null || query.isEmpty()) {
            result = new ArrayList<>();
        } else {
            result = retriever.apply(query).orElseGet(ArrayList::new);
        }
        return result;
    }

    private static List<JsonObject> queryFilms(final String query) {
        return query(query, MediaWiki::getFilmWikiPagesByName);
    }

    private void publishResult(int resultId, int resultCode, List<JsonObject> result) {
        final Intent intent = new Intent(RESULT_CALLBACK);
        intent.putExtra(RESULT_ID, resultId);
        intent.putExtra(RESULT_STATE, resultCode);
        intent.putExtra(RESULT_LIST, WikiStorage.wrapQueryResult(result));
        sendBroadcast(intent);
    }
}
