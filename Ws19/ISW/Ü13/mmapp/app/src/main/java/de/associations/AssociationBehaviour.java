package de.associations;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.ToIntFunction;

import de.associations.shortcuts.AssociatedObjectsCounter;
import de.util.Traits;
import de.util.annotations.Trait;

class AssociationBehaviour<S, T> {
    private static final Traits TRAITS = new Traits(AssociationBehaviour.class);
    private static final int UNLIMITED = -1;

    @Trait
    private final Class<S> sourceClass;
    @Trait
    private final Class<T> targetClass;
    private final AssociatedObjectsCounter<S> associatedObjectsCounter;

    private String minimumText;
    private String maximumText;

    private Function<S, Integer> minimumGetter;
    private Function<S, Integer> maximumGetter;

    public AssociationBehaviour(final Class<S> sourceClass, final Class<T> targetClass,
                                final AssociatedObjectsCounter<S> counter) {
        this.sourceClass = sourceClass;
        this.targetClass = targetClass;

        this.associatedObjectsCounter = counter;

        setMinimumToZero();
        setMaximumToUnlimited();
    }

    public void setMinimumToZero() {
        this.minimumGetter = s -> 0;
        minimumText = "0";
    }

    public void setMinimumToOne() {
        this.minimumGetter = s -> 1;
        minimumText = "1";
    }

    public void setMinimum(int val) {
        this.minimumGetter = s -> positiveOrZero(val);
        minimumText = "" + positiveOrZero(val);
    }

    public void setMinimum(String name, final ToIntFunction<S> minimumGetter) {
        this.minimumGetter = s -> positiveOrZero(minimumGetter.applyAsInt(s));
        minimumText = name;
    }

    private int positiveOrZero(int val) {
        return val < 0 ? 0 : val;
    }

    public void setMaximumToOne() {
        this.maximumGetter = s -> 1;
        maximumText = "1";
    }

    public void setMaximum(int val) {
        int max = positiveOrUnlimited(val);
        this.maximumGetter = s -> max;
        maximumText = val < 0 ? "*" : "" + val;
    }

    private void setMaximum(String name, ToIntFunction<S> maximumGetter) {
        this.maximumGetter = s -> positiveOrUnlimited(maximumGetter.applyAsInt(s));
        maximumText = name;
    }

    private int positiveOrUnlimited(int val) {
        return val < 0 ? UNLIMITED : val;
    }

    private void setMaximumToUnlimited() {
        this.maximumGetter = s -> UNLIMITED;
        maximumText = "*";
    }

    public void setBoundaries(String boundaries) {
        final String[] splitted = boundaries.split("\\.\\.");
        checkLength(splitted);
        parseMinimum(splitted[0]);
        parseMaximum(splitted[1]);
    }

    private void checkLength(String[] splitted) {
        if (splitted.length != 2)
            throw new AssociationException("Expected expression of form '(num|func)..(num|func|*)'");
    }

    private void parseMinimum(String segm) {
        final Optional<Integer> minOpt = tryIntegerParse(segm);
        if (minOpt.isPresent())
            setMinimum(minOpt.get());
        else
            setMinimum(segm, tryNameParse(segm));
    }

    private void parseMaximum(String segm) {
        final Optional<Integer> maxOpt = tryIntegerParse(segm);
        if (maxOpt.isPresent()) {
            setMaximum(maxOpt.get());
        } else if ("*".equals(segm)) {
            setMaximumToUnlimited();
        } else {
            setMaximum(segm, tryNameParse(segm));
        }
    }

    private Optional<Integer> tryIntegerParse(String segm) {
        try {
            return Optional.of(Integer.parseInt(segm));
        } catch (NumberFormatException nfe) {
            return Optional.empty();
        }
    }

    private ToIntFunction<S> tryNameParse(final String segment) {
        try {
            return createFunction(segment);
        } catch (IllegalArgumentException | NoSuchMethodException | SecurityException e) {
            final String format = "Can't access method '%s' of class '%s'";
            final String msg = String.format(format, segment, sourceClass.getName());
            throw new AssociationException(msg, e);
        }
    }

    private ToIntFunction<S> createFunction(final String segment)
            throws NoSuchMethodException {
        final Method method = sourceClass.getMethod(segment);
        return src -> {
            try {
                return (int) method.invoke(src);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                final String msg = "Can't call '" + segment + "' on object '" + src + "'";
                throw new AssociationException(msg, e);
            }
        };
    }

    public boolean canAppend(final S source) {
        return inBoundaries(source, 1);
    }

    public boolean appliesTo(final S source) {
        return inBoundaries(source, 0);
    }

    public boolean canRemove(final S source) {
        return inBoundaries(source, -1);
    }

    private boolean inBoundaries(final S source, int offset) {
        return checkBorders(source, associatedObjectsCounter.apply(source) + offset);
    }

    private boolean checkBorders(final S source, int curSize) {
        return checkLowerBorder(source, curSize) && checkUpperBorder(source, curSize);
    }

    private boolean checkLowerBorder(final S source, int curSize) {
        return minimumGetter.apply(source) <= curSize;
    }

    boolean checkUpperBorder(final S source, int curSize) {
        boolean result;
        int max = maximumGetter.apply(source);

        if (max == UNLIMITED) {
            result = true;
        } else {
            result = curSize <= max;
        }
        return result;
    }

    @Trait
    @Override
    public String toString() {
        return sourceClass.getSimpleName() +
                " --> [" +
                getBoundariesAsString() +
                "] " +
                targetClass.getSimpleName();
    }

    @Override
    public boolean equals(Object obj) {
        return TRAITS.testEqualityBetween(this, obj);
    }

    @Override
    public int hashCode() {
        return TRAITS.createHashCodeFor(this);
    }

    private String getBoundariesAsString() {
        return minimumText + ".." + maximumText;
    }
}
