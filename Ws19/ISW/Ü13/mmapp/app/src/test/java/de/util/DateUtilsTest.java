package de.util;

import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import static de.util.DateUtils.dateToText;
import static de.util.DateUtils.differenceInDays;
import static de.util.DateUtils.differenceInYears;
import static de.util.DateUtils.normDate;
import static de.util.DateUtils.normDateTimeToMidnight;
import static de.util.DateUtils.now;
import static de.util.DateUtils.nowAtMidnight;
import static de.util.DateUtils.textToDate;
import static de.util.Month.JULY;
import static java.util.Calendar.DAY_OF_MONTH;
import static java.util.Calendar.DECEMBER;
import static java.util.Calendar.HOUR_OF_DAY;
import static java.util.Calendar.JANUARY;
import static java.util.Calendar.MILLISECOND;
import static java.util.Calendar.MINUTE;
import static java.util.Calendar.MONTH;
import static java.util.Calendar.SECOND;
import static java.util.Calendar.YEAR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DateUtilsTest {
    @Test
    void testNormDateTimeToMidnight() {
        // setup
        final Date date = new Date(0L);
        final Date d = normDateTimeToMidnight(date);
        final Calendar expectedCalendar = Calendar.getInstance();
        expectedCalendar.setTime(date);
        expectedCalendar.set(HOUR_OF_DAY, 0);
        expectedCalendar.set(MINUTE, 0);
        expectedCalendar.set(SECOND, 0);
        expectedCalendar.set(MILLISECOND, 0);
        final Calendar actualCalendar = Calendar.getInstance();
        actualCalendar.setTime(d);

        // test
        assertEquals(expectedCalendar, actualCalendar);
    }

    @Test
    void testNormDateTimeToMidnightWithNull() {
        assertNull(normDateTimeToMidnight(null));
    }

    @Test
    void testNormDate() {
        long millis = System.currentTimeMillis();
        if(millis % 10 == 0) {
            millis += 1;
        }
        final Date date = new Date(millis);
        long normedDate = normDate(date).getTime();

        assertEquals(0, normedDate % 1000);
        assertNotEquals(0, millis % 1000);
    }

    @Test
    void testNormDateWithNull() {
        assertNull(normDate(null));
    }

    @Test
    void testNormMillis() {
        // setup
        long millis = System.currentTimeMillis();
        if(millis % 10 == 0) {
            millis += 1;
        }

        // precondition
        assertNotEquals(0, millis % 1000);

        // test
        long normedMillis = DateUtils.normMillis(millis);
        assertEquals(0, normedMillis % 1000);

    }

    @Test
    void testNow() {
        // setup
        final Calendar expectedCalendar = Calendar.getInstance();
        final Calendar actualCalendar = Calendar.getInstance();
        actualCalendar.setTime(now());
        expectedCalendar.setTime(new Date());
        expectedCalendar.set(MILLISECOND, 0);
        // add offset because of calendar setter bug
        if(TimeZone.getDefault().inDaylightTime( new Date() )) {
            expectedCalendar.add(HOUR_OF_DAY, -1);
        }

        assertEquals(expectedCalendar, actualCalendar);
    }

    @Test
    void testNowAtMidnight() {
        // setup
        final Calendar expectedCalendar = Calendar.getInstance();
        expectedCalendar.setTimeInMillis(System.currentTimeMillis());
        expectedCalendar.set(HOUR_OF_DAY, 0);
        expectedCalendar.set(MINUTE, 0);
        expectedCalendar.set(SECOND, 0);
        expectedCalendar.set(MILLISECOND, 0);
        final Calendar actualCalendar = Calendar.getInstance();
        actualCalendar.setTime(nowAtMidnight());

        // test
        assertEquals(expectedCalendar, actualCalendar);
    }

    @Test
    void testDifferenceInDays() {
        assertEquals(0, differenceInDays(
                createDate(1, JANUARY, 2019),
                createDate(1, JANUARY, 2019))
        );
        assertEquals(30, differenceInDays(
                createDate(1, JANUARY, 2019),
                createDate(31, JANUARY, 2019))
        );
        assertEquals(364, differenceInDays(
                createDate(1, JANUARY, 2019),
                createDate(31, DECEMBER, 2019))
        );
    }

    @Test
    void testDifferenceInYears() {
        assertEquals(0, differenceInYears(
                createDate(1, JANUARY, 2019),
                createDate(1, JANUARY, 2019))
        );
        assertEquals(1, differenceInYears(
                createDate(1, JANUARY, 2018),
                createDate(31, JANUARY, 2019))
        );
        assertEquals(1000, differenceInYears(
                createDate(1, JANUARY, 1019),
                createDate(31, DECEMBER, 2019))
        );
    }

    @Test
    void testDateToTextWithDefaultFormatter() {
        // setup
        int day = 4;
        int month = JULY.ordinal();
        int year = 1996;

        // test
        assertEquals("", dateToText(null));
        assertEquals("04.07.1996", dateToText(createDate(day, month, year)));
    }

    private Date createDate(int day, int month, int year) {
        final Calendar calendar = Calendar.getInstance();
        calendar.set(YEAR, year);
        calendar.set(MONTH, month);
        calendar.set(DAY_OF_MONTH, day);
        calendar.set(HOUR_OF_DAY, 0);
        calendar.set(MINUTE, 0);
        calendar.set(SECOND, 0);
        calendar.set(MILLISECOND, 0);
        return calendar.getTime();
    }

    @Test
    void testDateToTextWithNoFormatter() {
        // setup
        int day = 4;
        int month = JULY.ordinal();
        int year = 1996;

        // test
        assertEquals("", dateToText(null, null));
        assertEquals("", dateToText(null, createDate(day, month, year)));
    }

    @Test
    void testDateToTextWithCustomFormatter() {
        // setup
        int day = 4;
        int month = JULY.ordinal();
        int year = 1996;
        final SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");

        // test
        assertEquals("", dateToText(formatter, null));
        assertEquals("04-07-1996", dateToText(formatter, createDate(day, month, year)));
    }

    @Test
    void testTextToDateWithNoFormatter() {
        // test
        assertEquals(now(), textToDate(ObjectUtils.<SimpleDateFormat>typeSafeNull(), "today"));
        assertNull(textToDate(ObjectUtils.<SimpleDateFormat>typeSafeNull(), null));
        assertNull(textToDate(ObjectUtils.<SimpleDateFormat>typeSafeNull(), ""));
        assertNull(textToDate(ObjectUtils.<SimpleDateFormat>typeSafeNull(), "04-07-1996"));
        assertNull(textToDate(ObjectUtils.<SimpleDateFormat>typeSafeNull(), "abc"));
    }

    @Test
    void testTextToDateWithSimpleFormatter() {
        // setup
        int day = 4;
        int month = JULY.ordinal();
        int year = 1996;
        final SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        final Date expectedDate = createDate(day, month, year);

        // test
        assertEquals(now(), textToDate(formatter, "today"));
        assertNull(textToDate(formatter, null));
        assertNull(textToDate(formatter, ""));
        assertEquals(expectedDate, textToDate(formatter, "04-07-1996"));
        assertNull(textToDate(formatter, "abc"));
    }

    @Test
    void testIsLeapYear() {
        // test
        assertTrue(DateUtils.isLeapYear(1600));
        assertTrue(DateUtils.isLeapYear(1704));
        assertFalse(DateUtils.isLeapYear(1700));
        assertFalse(DateUtils.isLeapYear(1800));
        assertFalse(DateUtils.isLeapYear(1900));
        assertFalse(DateUtils.isLeapYear(1999));
        assertTrue(DateUtils.isLeapYear(2000));
    }
}
