package de.moviemanager.util;

import android.os.Build;

import java.security.SecureRandom;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import de.util.StringUtils;

import static java.util.stream.Collectors.toList;

public enum  AndroidStringUtils {
    ;

    public static final int IDENTIFIER_LENGTH = 64;

    public static String join(final CharSequence delimiter,
                              final String... args) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            return String.join(delimiter, args);
        else
            return StringUtils.join(delimiter, args);
    }

    public static String join(final CharSequence delimiter,
                              final List<? extends CharSequence> args) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            return String.join(delimiter, args);
        else
            return StringUtils.join(delimiter, args);
    }

    public static <X> String join(final CharSequence delimiter,
                                  final Function<X, ? extends CharSequence> mapper,
                                  final List<X> raw) {
        List<? extends CharSequence> args = raw.stream().map(mapper).collect(toList());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            return String.join(delimiter, args);
        else
            return StringUtils.join(delimiter, args);
    }

    public static String generateIdentifier(final Predicate<String> condition) {
        String key = generateIdentifier();
        while (condition.test(key)) {
            key = generateIdentifier();
        }
        return key;
    }

    private static String generateIdentifier() {
        final byte[] code = new byte[IDENTIFIER_LENGTH];
        final char[] chars = new char[code.length];

        new SecureRandom().nextBytes(code);
        for(int i = 0; i < chars.length; ++i) {
            chars[i] = (char) code[i];
        }

        return new String(chars);
    }

    public static <T, S extends CharSequence> BiPredicate<T, S> buildQueryPredicate(
            final BiPredicate<S, S> predicate,
            final Function<T, S> mapper,
            final UnaryOperator<S> transformation
    ) {
        return (obj, sequence) -> {
            S objSequence = mapper.apply(obj);
            objSequence = transformation.apply(objSequence);
            sequence = transformation.apply(sequence);

            return predicate.test(objSequence, sequence);
        };
    }
}
