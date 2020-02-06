package de.util;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import de.util.annotations.Trait;

import static java.util.Objects.requireNonNull;

/**
 * Simple immutable container to create a tuple of two objects.
 * If this class needs to be serialized, please use {@link SerializablePair}, which ensures, that
 * all its members implement {@link Serializable}.
 * This class provides uses {@link Traits} to implement equals and hashCode
 * with respect to the first and second stored object.
 *
 * <br><b>Author</b>: <a href="mailto:schustrchr@gmail.com">Ctoffer</a><br>
 *
 * @param <F> type of the first object
 * @param <S> type of the second object
 */
public class Pair <F, S> {
    public static final String MAP_KEY_FIRST = "first";
    public static final String MAP_KEY_SECOND = "second";

    private static final Traits TRAITS = new Traits(Pair.class);

    @Trait public final F first;
    @Trait public final S second;

    /**
     * Constructs a new pair from the two given objects.
     *
     * @param first 1<sup>st</sup> object of this pair
     * @param second 2<sup>nd</sup> object of this pair
     * @throws NullPointerException if one of the given objects is null
     */
    public Pair(final F first, final S second) {
	    this.first = requireNonNull(first);
	    this.second = requireNonNull(second);
    }

    /**
     * Access method for first object created to support
     * functional interfaces. If such a access is not needed, simply
     * access the attribute directly.
     *
     * @return first
     */
    public F getFirst() {
	    return first;
    }

    /**
     * Access method for second object created to support
     * functional interfaces. If such a access is not needed, simply
     * access the attribute directly.
     *
     * @return first
     */
    public S getSecond() {
	    return second;
    }

    /**
     * Creates a new pair with swapped objects, so if the current pair
     * is (a, b) this method returns (b, a).
     *
     * @return new pair with swapped arguments
     */
    public Pair<S, F> swapArgs() {
	    return new Pair<>(getSecond(), getFirst());
    }

    public <X> Pair<X, S> mapFirst(final Function<F, X> mapper) {
        return new Pair<>(mapper.apply(first), second);
    }

    public <X> Pair<F, X> mapSecond(final Function<S, X> mapper) {
        return new Pair<>(first, mapper.apply(second));
    }

    /**
     * @see Traits#testEqualityBetween(Object, Object)
     *
     * @param obj
     * @return <i>true</i> if the given object if it equals <i>this</i>
     */
    @Override
    public boolean equals(Object obj) {
	    return TRAITS.testEqualityBetween(this, obj);
    }

    /**
     * @see Traits#createHashCodeFor(Object)
     * @return hashCode of this object
     */
    @Override
    public int hashCode() {
        return TRAITS.createHashCodeFor(this);
    }

    /**
     * Creates a string representation of this pair.
     * This method is equivalent to:<br>
     * <pre><code>
     *     String.format("(%s, %s)", first, second);
     * </code></pre>
     *
     * @return string representation of this pair
     */
    @Override
    public String toString() {
        return String.format("(%s, %s)", first, second);
    }

    public JSONObject toJsonObject() {
        return new JSONObject(toMap());
    }

    public Map<String, Object> toMap(){
        Map<String, Object> map = new HashMap<>();
        map.put(MAP_KEY_FIRST, first);
        map.put(MAP_KEY_SECOND, second);
        return map;
    }

    /**
     * Converts an Map.Entry to a pair of the contained key and value.
     *
     * @param e Entry of a Map that should be converted into a
     * @param <F> type of the first object
     * @param <S> type of the second object
     * @return a new pair containing (key, value) from the Entry
     */
    public static <F, S> Pair<F, S> paired(Entry<F, S> e) {
	    return paired(e.getKey(), e.getValue());
    }

    /**
     * Constructs a new pair from the two given objects.
     *
     * @param first first object of the resulting pair
     * @param second second object of the resulting pair
     * @param <F> type of the first object
     * @param <S> type of the second object
     * @return a new pair containing (first, second)
     */
    public static <F, S> Pair<F, S> paired(F first, S second) {
	    return new Pair<>(first, second);
    }
}
