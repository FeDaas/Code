package de.wiki.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.util.Month;
import de.util.Traits;
import de.util.annotations.Trait;

import static java.lang.Integer.parseInt;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static java.util.regex.Pattern.compile;

/**
 * <p>
 *     Data class which stores the core attributes of an actor.
 *     The data for this class is provided by the {@link de.wiki.loader.ActorLoader}.
 *     Class contains all data string based for a more specific usage further parsing is
 *     required.
 * </p>
 *
 *
 * <br><b>Author</b>: <a href="mailto:schustrchr@gmail.com">Ctoffer</a><br>
 */
public class Actor {
    private static final String BORN = "born";
    private static final String OCCUPATION = "occupation";

    public static final List<String> TABULAR_ATTRIBUTES = unmodifiableList(asList(BORN, OCCUPATION));
    private static final Map<String, BiConsumer<Actor, List<String>>> ATTRIBUTE_HANDLER;

    static {
        ATTRIBUTE_HANDLER = new HashMap<>();
        ATTRIBUTE_HANDLER.put(BORN, Actor::handleBorn);
        ATTRIBUTE_HANDLER.put(OCCUPATION, Actor::handleOccupations);
    }

    private static BiConsumer<Actor, List<String>> getAttributeHandler(String key) {
        return ATTRIBUTE_HANDLER.getOrDefault(key, (d, p) -> {});
    }

    private static final Pattern DATE_PATTERN = compile("\\((\\d{4})-(\\d{2})-(\\d{2})\\)");
    private static final String TO_STRING_FORMAT = "Actor{name=\"%s\", " +
            "birthday=\"%s\", occupations=%s}";

    private static final Traits TRAITS = new Traits(Actor.class);

    @Trait private String name;
    @Trait private String imageURL;
    @Trait private String birthName;
    @Trait private String dateOfBirth;
    @Trait private String biography;
    @Trait private List<String> occupations;

    /**
     * Constructs a new actor and processed the given attributes to parse them in a finer granularity.
     * Attributes which can't be handled will be ignored, private fields which are not filled
     * with content from an attribute are initialized empty.
     *
     * @param name name of this actor
     * @param imageURL the url of the image of this actor
     * @param biography the biography for this actor
     * @param attributes possible attributes for this actor
     */
    public Actor(String name, String imageURL, String biography, Map<String, List<String>> attributes) {
        this.name = name;
        this.imageURL = imageURL;
        this.biography = biography;
        this.dateOfBirth = "";
        this.birthName = "";
        this.occupations = new ArrayList<>();

        for(Map.Entry<String, List<String>> e : attributes.entrySet()) {
            BiConsumer<Actor, List<String>> method = getAttributeHandler(e.getKey());
            method.accept(this, e.getValue());
        }
    }

    private void handleBorn(List<String> born) {
        this.birthName = born.get(0);
        this.dateOfBirth = convertDate(DATE_PATTERN.matcher(born.get(1)));
    }

    private static String convertDate(Matcher regex) {
        regex.find();
        String year = regex.group(1);
        String month = regex.group(2);
        String day = regex.group(3);

        return day + " " + Month.of(parseInt(month)) + " " + year;
    }

    private void handleOccupations(List<String> occupations) {
        this.occupations = occupations;
    }

    /**
     * Getter of the name
     *
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Getter of the name at birth
     *
     * @return name at birth
     */
    public String getBirthName() {
        return birthName;
    }

    /**
     * Getter of a read-only list of occupations
     *
     * @return unmodifiable list of occupations
     */
    public List<String> getOccupations() {
        return unmodifiableList(occupations);
    }

    /**
     * Getter of the URL of the image
     *
     * @return url of the image
     */
    public String getImageURL() {
        return imageURL;
    }

    /**
     * Getter of the birth date
     *
     * @return date of birth
     */
    public String getDateOfBirth() {
        return dateOfBirth;
    }

    /**
     * Getter of the biography
     *
     * @return biography
     */
    public String getBiography() {
        return biography;
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
        return format(TO_STRING_FORMAT, name, dateOfBirth, occupations);
    }
}
