package de.util;

public enum  PrimitiveUtils {
    ;

    public static boolean fromObject(final Boolean b) {
        if(b == null) {
            return false;
        } else {
            return b;
        }
    }

    public static int fromObject(final Integer i) {
        if(i == null) {
            return 0;
        } else {
            return i;
        }
    }
}
