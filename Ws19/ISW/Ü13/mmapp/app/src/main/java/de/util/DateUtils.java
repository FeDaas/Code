package de.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static java.util.Locale.US;

public enum  DateUtils {
    ;

    public static Date normDateTimeToMidnight(final Date d) {
        Date result;
        if(d == null) {
            result = null;
        } else {
            final Calendar calendar = Calendar.getInstance();
            calendar.setTime(d);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            result = calendar.getTime();
        }

        return result;
    }

    public static Date normDate(Date date) {
        if(date == null)
            return null;
        long normedMillis = normMillis(date.getTime());
        return new Date(normedMillis);
    }

    static long normMillis(long millis) {
        return 1000 * (millis / 1000);
    }

    public static Date now() {
        return new Date(normMillis(System.currentTimeMillis()));
    }

    public static Date nowAtMidnight() {
        return normDateTimeToMidnight(now());
    }

    public static long differenceInDays(final Date date1, final Date date2) {
        long millis1 = date1.getTime();
        long millis2 = date2.getTime();
        long diff = millis2 - millis1;
        return TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
    }

    public static int differenceInYears(final Date date1, final Date date2) {
        final Calendar calendar1 = Calendar.getInstance();
        calendar1.setTime(date1);
        final Calendar calendar2 = Calendar.getInstance();
        calendar2.setTime(date2);

        return Math.abs(calendar1.get(Calendar.YEAR) - calendar2.get(Calendar.YEAR));
    }

    public static String dateToText(final Date date) {
        final SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy", US);
        formatter.setLenient(false);
        return dateToText(formatter, date);
    }

    public static String dateToText(final SimpleDateFormat formatter, final Date date) {
        String result;
        if(formatter == null || date == null) {
            result =  "";
        } else {
            result = formatter.format(date);
        }
        return result;
    }

    public static Date textToDate(final String format, final String str) {
        return textToDate(new SimpleDateFormat(format, US), str);
    }

    public static Date textToDate(final SimpleDateFormat formatter, final String str) {
        Date result;

        if("today".equals(str)) {
            result = now();
        }

        else if(str == null || formatter == null || str.isEmpty()) {
            result = null;
        } else {
            try {
                result = formatter.parse(str);
            } catch (ParseException e) {
                result = null;
            }
        }

        return result;
    }

    public static boolean isLeapYear(int year) {
        final Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        return cal.getActualMaximum(Calendar.DAY_OF_YEAR) > 365;
    }
}
