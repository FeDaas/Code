package de.moviemanager.ui.wiki.fetch;

import android.app.IntentService;
import android.content.Intent;

import androidx.annotation.Nullable;

import com.google.gson.JsonObject;

import java.util.Optional;
import java.util.function.Function;

import de.moviemanager.ui.wiki.WikiStorage;
import de.moviemanager.ui.wiki.query.WikiQueryMode;
import de.wiki.MediaWiki;

import static com.google.gson.JsonParser.parseString;
import static de.moviemanager.ui.wiki.query.WikiQueryService.DEFAULT_REQUEST_ID;
import static de.moviemanager.ui.wiki.query.WikiQueryService.PARAMETER_ID;
import static de.moviemanager.ui.wiki.query.WikiQueryService.PARAMETER_TYPE;
import static de.moviemanager.ui.wiki.query.WikiQueryService.RESULT_EMPTY;
import static de.moviemanager.ui.wiki.query.WikiQueryService.RESULT_ID;
import static de.moviemanager.ui.wiki.query.WikiQueryService.RESULT_NON_EMPTY;
import static de.moviemanager.ui.wiki.query.WikiQueryService.RESULT_STATE;

public class WikiFetchService extends IntentService {
    public static final String PARAMETER_WIKI_PAGE_JSON = "parameter_wiki_page_json";
    public static final String RESULT_KEY = "result_key";
    public static final String RESULT_CALLBACK = "de.moviemanager.ui.wiki.fetch";

    public WikiFetchService() {
        super(WikiFetchService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        int requestId = DEFAULT_REQUEST_ID;
        String resultKey = null;

        if (intent != null) {
            requestId = intent.getIntExtra(PARAMETER_ID, DEFAULT_REQUEST_ID);
            final WikiQueryMode type = WikiQueryMode.fromExtra(intent, PARAMETER_TYPE);
            final String pageJson = intent.getStringExtra(PARAMETER_WIKI_PAGE_JSON);
            final JsonObject page = parseString(pageJson).getAsJsonObject();
            resultKey = processPage(type, page);
        }

        publishResult(requestId, resultKey);
    }

    private String processPage(WikiQueryMode mode, JsonObject page) {
        String resultKey = null;

        if (mode == WikiQueryMode.ACTOR) {
            resultKey = loadActorFromPage(page);
        } else if(mode == WikiQueryMode.FILM) {
            resultKey = loadFilmFromPage(page);
        }

        return resultKey;
    }

    private static String loadActorFromPage(final JsonObject page) {
        return loadFromPage(MediaWiki::getActorDataFromWikiPage, WikiStorage::wrapActor, page);
    }

    private static String loadFilmFromPage(final JsonObject page) {
        return loadFromPage(MediaWiki::getFilmDataFromWikiPage, WikiStorage::wrapFilm, page);
    }

    private static <T> String loadFromPage(final Function<JsonObject, Optional<T>> load,
                                           final Function<T, String> tempSave,
                                           final JsonObject page) {
        String resultKey = null;
        Optional<T> optional = load.apply(page);

        if(optional.isPresent()) {
            resultKey = tempSave.apply(optional.get());
        }

        return resultKey;
    }

    private void publishResult(int resultId, String resultKey) {
        final Intent intent = new Intent(RESULT_CALLBACK);
        intent.putExtra(RESULT_ID, resultId);
        intent.putExtra(RESULT_STATE, resultKey != null ? RESULT_NON_EMPTY : RESULT_EMPTY);
        intent.putExtra(RESULT_KEY, resultKey);
        sendBroadcast(intent);
    }
}
