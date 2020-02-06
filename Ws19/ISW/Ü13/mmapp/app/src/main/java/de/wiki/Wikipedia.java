package de.wiki;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import de.util.Traits;
import de.util.annotations.Trait;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.joining;

/**
 * Light weighted wrapper class for accessing Wikipedia-related online data.
 * This class was introduced to have a central point of access, in such way that a child
 * of this class could simulate a non existing connection.
 *
 * <br><b>Author</b>: <a href="mailto:schustrchr@gmail.com">Ctoffer</a><br>
 */
public class Wikipedia {
    private static final int DEFAULT_TIMEOUT = 5000;

    private static final Traits TRAITS = new Traits(Wikipedia.class);

    @Trait private final String name;
    @Trait private final int timeout;

    /**
     * Creates a new Wikipedia instance with the default timeout
     * of 5 seconds.
     *
     * @param name base url part of the Wiki like 'en.wikipedia.org'
     */
    public Wikipedia(String name) {
        this(name, DEFAULT_TIMEOUT);
    }

    /**
     * Creates a new Wikipedia instance with a custom timeout
     *
     * @param name base url part of the Wiki like 'en.wikipedia.org'
     * @param timeout the maximum time after which a response must be retrieved
     */
    private Wikipedia(String name, int timeout) {
        this.name = name;
        this.timeout = timeout;
    }

    /**
     * Getter of the name
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the name as URL
     *
     * @return "https://" + name
     */
    public String getHomeURL() {
        return "https://" + name;
    }

    /**
     * Returns the absolute url of a relative path. The relative path is expected to be relative
     * to the {@link Wikipedia#getHomeURL()}
     *
     * @param relativePath relative path of a page or resource
     * @return absolute path of the page or resource
     */
    public String asAbsolutePath(String relativePath) {
        return getHomeURL() + relativePath;
    }

    /**
     * Retrieves a html page as a document
     *
     * @param url URL of the html page
     * @return the parsed html document
     * @throws IOException if the URL is not a valid one or the connection time outed
     */
    public Document getHTMLDocument(String url) throws IOException {
        return Jsoup.parse(new URL(url), timeout);
    }

    /**
     * Retrieves a text base file from the given url.
     *
     * @param url URL of the file
     * @return a line break separated String of the complete file
     * @throws IOException if the url is invalid or read failed
     */
    public String getTextFile(String url) throws IOException {
        try (InputStream input = new URL(url).openStream();
             InputStreamReader reader = new InputStreamReader(input, UTF_8.name());
             BufferedReader bReader = new BufferedReader(reader)
        ) {
            return bReader.lines().collect(joining("\n"));
        }
    }

    @Override
    public boolean equals(Object obj) {
        return TRAITS.testEqualityBetween(this, obj);
    }

    @Override
    public int hashCode() {
        return TRAITS.createHashCodeFor(this);
    }

    @Override
    public String toString() {
        return format("Wikipedia{name=\"%s\", timeout='%s ms'}", name, timeout);
    }
}
