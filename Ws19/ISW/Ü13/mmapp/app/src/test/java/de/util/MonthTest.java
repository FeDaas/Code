package de.util;

import org.junit.jupiter.api.Test;

import static de.util.StringUtils.capitalizeString;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MonthTest {
    @Test
    void testAllMonths() {
        // setup
        Month[] months = Month.values();

        // test
        for(int i = 0; i < 12; ++i) {
            assertEquals(months[i], Month.of(i + 1));
            assertEquals(months[i].toString(), capitalizeString(months[i].name()));
        }
    }

    @Test
    void testMaxDaysWithoutLeap() {
        // setup
        Month[] months = Month.values();
        int[] days = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};

        // precondition
        assertEquals(12, days.length);

        // test
        for(int i = 0; i < months.length; ++i) {
            assertEquals(days[i], months[i].getMaxDaysWithoutLeap());
        }
    }

    @Test
    void testAsStringValues() {
        // setup
        final Month[] months = Month.values();
        final String[] monthNames = Month.asStrings();

        // test
        for(int i = 0; i < monthNames.length; ++i) {
            assertEquals(months[i].toString(), monthNames[i]);
        }
    }
}
