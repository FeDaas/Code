package de.wiki.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.util.Pair;
import de.util.Traits;
import de.util.annotations.Trait;

import static de.util.Pair.paired;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static java.util.regex.Pattern.compile;
import static java.util.stream.Collectors.toList;

/**
 * <p>
 *     Data class which stores the core attributes of an film.
 *     The data for this class is provided by the {@link de.wiki.loader.FilmLoader}.
 *     Class contains all data string based for a more specific usage further parsing is
 *     required.
 * </p>
 *
 *
 * <br><b>Author</b>: <a href="mailto:schustrchr@gmail.com">Ctoffer</a><br>
 */
public class Film {
    private static final String COUNTRY_ATTRIBUTE = "country";
    private static final String RUNNING_TIME_ATTRIBUTE = "running time";
    private static final String STARRING_ATTRIBUTE = "starring";
    private static final String LANGUAGE_ATTRIBUTE = "language";
    private static final String RELEASE_DATE_ATTRIBUTE = "release date";

    public static final List<String> TABULAR_ATTRIBUTES = unmodifiableList(asList(COUNTRY_ATTRIBUTE,
            RUNNING_TIME_ATTRIBUTE,
            STARRING_ATTRIBUTE,
            LANGUAGE_ATTRIBUTE,
            RELEASE_DATE_ATTRIBUTE));

    private static final Map<String, BiConsumer<Film, List<Pair<String, String>>>> ATTRIBUTE_HANDLER;

    static {
        ATTRIBUTE_HANDLER = new HashMap<>();
        ATTRIBUTE_HANDLER.put(COUNTRY_ATTRIBUTE, Film::handleCountries);
        ATTRIBUTE_HANDLER.put(RUNNING_TIME_ATTRIBUTE, Film::handleRunningTime);
        ATTRIBUTE_HANDLER.put(RELEASE_DATE_ATTRIBUTE, Film::handleReleaseDates);
        ATTRIBUTE_HANDLER.put(LANGUAGE_ATTRIBUTE, Film::handleLanguages);
        ATTRIBUTE_HANDLER.put(STARRING_ATTRIBUTE, Film::handleStarring);
    }

    private static final String RELEASE_TEXT_DATE = "\\d+\\s\\w+\\s\\d{4}";
    private static final String RELEASE_NUMERIC_DATE = "\\(\\d{4}-\\d+-\\d+\\)\\s";
    private static final String RELEASE_LOCATION = "\\w+(\\s\\w+)*";
    private static final String RELEASE_FORMAT = "(%s)\\s(%s)?\\((%s)\\)";
    private static final Pattern RELEASE_PATTERN = compile(format(RELEASE_FORMAT,
            RELEASE_TEXT_DATE, RELEASE_NUMERIC_DATE, RELEASE_LOCATION));
    private static final String TO_STRING_FORMAT = "Film{title=\"%s\"" +
            ", running time=\"%s\", #releases=%s}";

    private static final Traits TRAITS = new Traits(Film.class);

    @Trait private String title;
    @Trait private String imageURL;
    @Trait private String description;
    @Trait private List<String> countries;
    @Trait private String runningTime;
    @Trait private List<String> languages;
    @Trait private List<Pair<String, String>> releaseDates;
    @Trait private List<Pair<String, String>> starring;

    /**
     * Constructs a new film and processed the given attributes to parse them in a finer granularity.
     * Attributes which can't be handled will be ignored, private fields which are not filled
     * with content from an attribute are initialized empty.
     *
     * @param title title of this film
     * @param imageURL the url of the image of this actor
     * @param description the description for this film
     * @param attributes possible attributes for this film
     */
    public Film(String title, String imageURL, String description, Map<String, List<Pair<String, String>>> attributes) {
        this.title = title;
        this.imageURL = imageURL;
        this.description = description;

        this.runningTime = "";

        countries = new ArrayList<>();
        languages = new ArrayList<>();
        releaseDates = new ArrayList<>();
        starring = new ArrayList<>();

        for(Map.Entry<String, List<Pair<String, String>>> e : attributes.entrySet()) {
            BiConsumer<Film, List<Pair<String, String>>> method = ATTRIBUTE_HANDLER.getOrDefault(
                    e.getKey(), (d, p) -> {});
            method.accept(this, e.getValue());
        }
    }

    private void handleCountries(List<Pair<String, String>> countries) {
        Pattern pattern = compile("(.*)\\[\\d+\\]");
        this.countries = countries.stream()
                .map(Pair::getSecond)
                .map(pattern::matcher)
                .filter(Matcher::matches)
                .map(m -> m.group(1))
                .collect(toList());
    }

    private void handleRunningTime(List<Pair<String, String>> runningTimes) {
        Pattern pattern = compile("(\\d+\\s+minutes).*");
        this.runningTime = runningTimes.stream()
                .map(Pair::getSecond)
                .map(pattern::matcher)
                .filter(Matcher::matches)
                .map(m -> m.group(1))
                .findAny()
                .orElse("-1 minutes");
    }

    private void handleReleaseDates(List<Pair<String, String>> releaseDates) {
        this.releaseDates = releaseDates.stream()
                .map(Pair::getSecond)
                .map(RELEASE_PATTERN::matcher)
                .filter(Matcher::matches)
                .map(m -> paired(m.group(1), m.group(3)))
                .collect(toList());
    }

    private void handleLanguages(List<Pair<String, String>> languages) {
        this.languages = languages.stream().map(Pair::getSecond).collect(toList());
    }

    private void handleStarring(List<Pair<String, String>> starring) {
        this.starring = starring;
    }

    /**
     * Getter of the title
     *
     * @return title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Getter of the URL of the image
     *
     * @return URL of the image
     */
    public String getImageURL() {
        return imageURL;
    }

    /**
     * Getter of the description
     *
     * @return description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Getter of a read-only list of countries
     *
     * @return unmodifiable list of countries
     */
    public List<String> getCountries() {
        return unmodifiableList(countries);
    }

    /**
     * Getter of the running time
     *
     * @return running time
     */
    public String getRunningTime() {
        return runningTime;
    }

    /**
     * Getter of read-only list of actor meta data
     *
     * @return unmodifiable list of actor meta data
     */
    public List<Pair<String, String>> getStarring() {
        return unmodifiableList(starring);
    }

    /**
     * Getter of read-only list of langauges
     *
     * @return unmodifiable list of languages
     */
    public List<String> getLanguages() {
        return unmodifiableList(languages);
    }

    /**
     * Getter of read-only list of releases
     *
     * @return unmodifiable list of releases
     */
    public List<Pair<String, String>> getReleaseDates() {
        return unmodifiableList(releaseDates);
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
        return format(TO_STRING_FORMAT, title, runningTime, releaseDates.size());
    }
}
