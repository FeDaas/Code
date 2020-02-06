package de.moviemanager.data;

import org.junit.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import de.util.DateUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class MovieTest {
    @Test
    public void testGetDueDateOnMovieWithoutDueDate() {
        Movie m = new Movie(1);
        assertNull(m.getDueDate());
    }

    @Test
    public void testGetDueDateOnMovieWithDueDate() {
        Movie m = new Movie(1);
        assertNull(m.getDueDate());

        Date d = DateUtils.nowAtMidnight();
        m.setDueDate(d);

        assertEquals(m.getDueDate(), d);
    }

    @Test
    public void testSetDueDateOnMovieWithoutDueDate() {
        Movie m = new Movie(1);
        assertNull(m.getDueDate());

        Date d = DateUtils.nowAtMidnight();
        m.setDueDate(d);

        assertEquals(m.getDueDate(), d);
    }

    @Test
    public void testSetDueDateOnMovieWithDueDate() {
        Movie m = new Movie(1);
        assertNull(m.getDueDate());

        Date d = DateUtils.nowAtMidnight();
        m.setDueDate(d);

        assertEquals(m.getDueDate(), d);

        Calendar c = new GregorianCalendar(2002, 12, 2);
        d = DateUtils.normDateTimeToMidnight(c.getTime());
        m.setDueDate(d);

        assertEquals(m.getDueDate(), d);
    }
}
