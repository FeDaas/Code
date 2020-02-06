package de.util;

import java.util.Arrays;
import java.util.List;

import static java.lang.Character.toUpperCase;
import static java.util.stream.Collectors.joining;

public enum  StringUtils {
    ;

    /**
     * @param leftString first string of comparison
     * @param rightString second string of comparison
     * @return value between 0.0 and 1.0 representing the normedMinimumEditDistance,
     * where 0.0 means not similar at all  and 1.0 means identically
     */
    public static double normedMinimumEditDistance(String leftString, String rightString) {
        if (leftString.length() < rightString.length()) {
            final String tmp = leftString;
            leftString = rightString;
            rightString = tmp;
        }

        int longerLength = leftString.length();
        if (longerLength == 0)
            return 1.0;
        return (longerLength - editDistance(leftString, rightString)) / (double) longerLength;

    }

    /**
     * Example implementation of the Levenshtein Edit Distance
     * See http://rosettacode.org/wiki/Levenshtein_distance#Java
     *
     * @param s1
     * @param s2
     * @return
     */
    public static int editDistance(String s1, String s2) {
        s1 = s1.toLowerCase();
        s2 = s2.toLowerCase();

        int[] costs = new int[s2.length() + 1];

        for (int i = 0; i <= s1.length(); i++) {
            int lastValue = i;
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0)
                    costs[j] = j;
                else {
                    if (j > 0) {
                        int newValue = costs[j - 1];
                        if (s1.charAt(i - 1) != s2.charAt(j - 1))
                            newValue = Math.min(Math.min(newValue, lastValue), costs[j]) + 1;
                        costs[j - 1] = lastValue;
                        lastValue = newValue;
                    }
                }
            }
            if (i > 0)
                costs[s2.length()] = lastValue;
        }
        return costs[s2.length()];
    }

    public static String capitalizeString(final String s) {
        if(s.isEmpty())
            return s;
        if(s.length() == 1)
            return s.toUpperCase();

        return toUpperCase(s.charAt(0)) + s.substring(1).toLowerCase();
    }

    public static int alphabeticalComparison(final String a, final String b) {
        Integer result = null;

        for(int i = 0; i < Math.min(a.length(), b.length()); ++i) {
            int diff = a.charAt(i) - b.charAt(i);
            if(diff != 0) {
                result = diff;
                break;
            }
        }

        if(result == null) {
            result = a.length() - b.length();
        }

        return result;
    }

    public static String join(final CharSequence delimiter, final String... args) {
        return join(delimiter, Arrays.asList(args));
    }

    public static String join(final CharSequence delimiter, final List<? extends CharSequence> args) {
        String result;
        if(args.isEmpty()) {
            result =  "";
        } else if(args.size() == 1) {
            result = "" + args.get(0);
        } else {
            result = args.stream().collect(joining(delimiter));
        }

        return result;
    }
}
