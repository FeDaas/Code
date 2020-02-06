package de.wiki.loader;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.List;

import de.wiki.data.Actor;

import static de.wiki.data.Actor.TABULAR_ATTRIBUTES;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

/**
 * <p>
 *     Singleton of the actor loader which specializes the data extraction for actors.
 *     This class specifies the extraction for a actor.
 * </p>
 * <p>
 *     This class is not indented for multithreading.
 * </p>
 *
 * <br><b>Author</b>: <a href="mailto:schustrchr@gmail.com">Ctoffer</a><br>
 */
public final class ActorLoader extends BaseLoader<Actor, String> {
    private static final ActorLoader INSTANCE = new ActorLoader();
    private static final String NAME_SELECTOR = "div[class=fn]";
    private static final String TABLE_SELECTOR = "table[class=infobox biography vcard]";

    private ActorLoader() {
        super(TABULAR_ATTRIBUTES, Actor::new);
    }

    /**
     * Returns the only ActorLoader instance available.
     * @return the ActorLoader
     */
    public static ActorLoader getInstance() {
        return INSTANCE;
    }

    @Override
    protected String getNameSelector() {
        return NAME_SELECTOR;
    }

    @Override
    protected String getTableSelector() {
        return TABLE_SELECTOR;
    }

    @Override
    protected List<String> handleAttributeRow(Element row) {
        Element td = row.selectFirst("td");
        Element hList = td.selectFirst("div[class='hlist hlist-separated']");
        Elements nonListData = td.select("td > div, td > span");

        if (hList != null)
            return hList.select("li")
                    .stream()
                    .map(Element::text)
                    .map(String::toLowerCase)
                    .collect(toList());
        else if (nonListData != null && nonListData.size() > 1)
            return nonListData.stream()
                    .map(Element::text)
                    .collect(toList());
        else
            return singletonList(td.ownText());
    }


}
