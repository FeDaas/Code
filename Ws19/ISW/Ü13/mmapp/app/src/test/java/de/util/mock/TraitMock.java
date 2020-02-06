package de.util.mock;

import de.util.Traits;
import de.util.annotations.Trait;

public class TraitMock {
    private static final Traits TRAIT_PROPERTY = new Traits(TraitMock.class);

    @Trait
    private final String name;
    @Trait
    private final int i;
    @Trait
    private String s;

    public TraitMock(String name, int i, String s) {
        this.name = name;
        this.i = i;
        this.s = s;
    }

    @Trait
    public int getNameLength() {
        return name.length();
    }

    @Trait
    private int getDiff() {
        return name.length() - s.length();
    }

    @Trait
    protected double createFancy() {
        return name.length() / 8.0 * s.length() - i;
    }

    @Trait
    public void notAValidTraitMethod() {

    }

    @Trait
    public int notAValidTraitMethod(int a, int b) {
        return a * a + b;
    }

    @Trait
    public void setS(String s) {
        this.s = s;
    }

    @Override
    public String toString() {
        return String.format("TM(%s, %s, %s)", name, i, s);
    }

    @Override
    public boolean equals(Object obj) {
        return TRAIT_PROPERTY.testEqualityBetween(this, obj);
    }

    @Override
    public int hashCode() {
        return TRAIT_PROPERTY.createHashCodeFor(this);
    }

    public static class SpecializedTraitMock extends TraitMock {

        private static final Traits TP = new Traits(SpecializedTraitMock.class);
        @Trait
        private String attr;

        public SpecializedTraitMock(String name, int i, String s) {
            super(name, i, s);
            attr = "";
        }

        public void setAttribute(String attr) {
            this.attr = attr;
        }

        @Override
        public boolean equals(Object obj) {
            return TP.testEqualityBetween(this, obj);
        }

        @Override
        public int hashCode() {
            return TP.createHashCodeFor(this);
        }
    }
}
