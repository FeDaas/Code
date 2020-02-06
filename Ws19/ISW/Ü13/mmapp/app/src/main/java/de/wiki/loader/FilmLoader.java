package de.wiki.loader;

import org.jsoup.nodes.Element;

import java.util.List;

import de.util.Pair;
import de.wiki.data.Film;

import static de.util.Pair.paired;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

/**
 * <p>
 *     Singleton of the film loader which specializes the data extraction for films.
 *     This class specifies the extraction for a film.
 * </p>
 * <p>
 *     This class is not indented for multithreading.
 * </p>
 *
 * <br><b>Author</b>: <a href="mailto:schustrchr@gmail.com">Ctoffer</a><br>
 */
public final class FilmLoader extends BaseLoader<Film, Pair<String, String>> {
    private static final  FilmLoader INSTANCE = new FilmLoader();
    private static final String NAME_SELECTOR = "th[class=summary]";
    private static final String TABLE_SELECTOR = "table[class=infobox vevent]";

    private FilmLoader() {
        super(Film.TABULAR_ATTRIBUTES, Film::new);
    }

    /**
     * Returns the only FilmLoader instance available.
     * @return the FilmLoader
     */
    public static FilmLoader getInstance() {
        return INSTANCE;
    }

    @Override
    protected  String getNameSelector() {
        return NAME_SELECTOR;
    }

    @Override
    protected  String getTableSelector() {
        return TABLE_SELECTOR;
    }

    @Override
    protected List<Pair<String, String>> handleAttributeRow(Element row) {
        Element td = row.selectFirst("td");
        Element plainList = td.selectFirst("div[class=plainlist]");

        if (plainList != null)
            return plainList.select("li")
                    .stream()
                    .map(this::listEntryToPair)
                    .collect(toList());
        else
            return singletonList(paired("", td.ownText()));
    }
}
