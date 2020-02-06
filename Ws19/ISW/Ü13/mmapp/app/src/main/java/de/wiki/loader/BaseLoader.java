package de.wiki.loader;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import de.util.Pair;

import static de.util.Pair.paired;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

/**
 * <p>
 *      Extracts meta data from a given Wiki-page. Some extractions need to be specialized by
 *      a subclass.
 * </p>
 *
 * <br><b>Author</b>: <a href="mailto:schustrchr@gmail.com">Ctoffer</a><br>
 *
 * @param <P> type of the product created by this loader
 * @param <T> type of the attribute metadata
 */
public abstract class BaseLoader<P, T> {
    private final List<String> tabularAttributes;
    private final ProductConstructor<P, T> constructor;

    BaseLoader(List<String> tabularAttributes, ProductConstructor<P, T> constructor) {
        this.tabularAttributes = tabularAttributes;
        this.constructor = constructor;
    }

    /**
     * Loads meta data from the given document and creates a product using this metadata.
     *
     * @param doc document of a Wiki HTML
     * @return empty if an exception is thrown during loading<br>
     *         otherwise contains product
     */
    public Optional<P> loadDataFromWikiHTML(Document doc) {
        try {
            return Optional.of(tryLoadingDataFromWikiHTML(doc));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private P tryLoadingDataFromWikiHTML(Document doc) {
        Element table = doc.selectFirst(getTableSelector());

        String title = table.selectFirst(getNameSelector()).text();
        Element image = table.selectFirst("a[class=image]").selectFirst("img");
        String relativePath = image.hasAttr("srcset")
                ? image.attr("srcset")
                : image.attr("src");
        String imageURL = "https:" + relativePath.split(" ")[0];
        String description = getIntroduction(doc);
        Map<String, List<T>> attributes = extractAttributesFromTable(table);

        return constructor.create(title, imageURL, description, attributes);
    }

    protected abstract String getTableSelector();

    protected abstract String getNameSelector();

    private String getIntroduction(Document doc) {
        int index = doc.selectFirst("div[class~=toc]").elementSiblingIndex();
        Predicate<String> notEmpty = s -> !s.isEmpty();
        return doc.select("p")
                .stream()
                .filter(p -> p.elementSiblingIndex() < index)
                .map(this::removeCites)
                .map(Element::text)
                .filter(notEmpty)
                .limit(1)
                .collect(joining("\n"));
    }

    private Element removeCites(Element e) {
        e.select("sup[id^=cite_ref-]").remove();
        return e;
    }

    private Map<String, List<T>> extractAttributesFromTable(Element table) {
        List<Element> rows = table.select("th[scope=row]")
                .stream()
                .map(Element::parent)
                .collect(toList());

        return extractAttributesFromTableRows(rows);
    }

    private Map<String, List<T>> extractAttributesFromTableRows(List<Element> rows) {
        return  rows.stream()
                .map(this::toHeaderContentPair)
                .filter(this::headerOfInterest)
                .map(this::normContent)
                .collect(toMap(Pair::getFirst, Pair::getSecond));
    }

    private Pair<String, Element> toHeaderContentPair(Element row) {
        Element th = row.selectFirst("th");
        String text = th.text().toLowerCase();
        return paired(text, row);
    }

    private boolean headerOfInterest(Pair<String, Element> p) {
        return tabularAttributes.contains(p.first);
    }

    private Pair<String, List<T>> normContent(Pair<String, Element> p) {
        return paired(p.getFirst(), handleAttributeRow(p.getSecond()));
    }

    protected abstract List<T> handleAttributeRow(Element row);

    Pair<String, String> listEntryToPair(Element li) {
        Elements anchor = li.select("a[href^='/wiki/']");
        if (anchor != null && !anchor.text().isEmpty() && li.ownText().isEmpty()) {
            return paired(anchor.attr("href"), anchor.text());
        } else
            return paired("", li.text());
    }
}

interface ProductConstructor <P, T> {
    P create(String name, String imageURL, String introduction, Map<String, List<T>> attrs);
}