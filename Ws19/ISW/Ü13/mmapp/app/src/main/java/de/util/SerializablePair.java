package de.util;

import java.io.Serializable;
import java.util.Objects;

import de.util.annotations.Trait;

public class SerializablePair<F extends Serializable, S extends Serializable>
        implements Serializable {

    private static final Traits TRAITS = new Traits(Pair.class);

    @Trait public final F first;
    @Trait public final S second;

    private SerializablePair(final F first, final S second) {
        this.first = Objects.requireNonNull(first);
        this.second = Objects.requireNonNull(second);
    }

    @Override
    public boolean equals(Object obj) {
        return TRAITS.testEqualityBetween(this, obj);
    }

    @Override
    public int hashCode() {
        return TRAITS.createHashCodeFor(this);
    }

    public Pair<F, S> toPair() {
        return Pair.paired(first, second);
    }

    public static <X extends Serializable, Y extends Serializable> SerializablePair<X, Y> from(final Pair<X, Y> pair) {
        SerializablePair<X, Y> result = null;
        if(pair != null) {
            result = new SerializablePair<>(pair.first, pair.second);
        }
        return result;
    }
}
