package de.moviemanager.ui.masterlist.categorizer;

import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

import de.moviemanager.data.Movie;
import de.moviemanager.ui.masterlist.elements.ContentElement;
import de.moviemanager.ui.masterlist.elements.DividerElement;
import de.moviemanager.ui.masterlist.elements.HeaderElement;
import de.util.DateUtils;

import static de.util.DateUtils.dateToText;
import static de.util.DateUtils.textToDate;
import static de.util.StringUtils.alphabeticalComparison;

public class Due extends Categorizer<String, Movie> {

    private String notRentedString = "Not rented";
    private String overdueString = "Overdue";

    @Override
    public String getCategoryNameFor(Movie obj) {
        Date dueDate = obj.getDueDate();
        if (dueDate == null) {
            return notRentedString;
        }
        if (dueDate.before(DateUtils.nowAtMidnight())) {
            return overdueString;
        }
        String dueDateText = dateToText(obj.getDueDate());
        return dueDateText.substring(dueDateText.length() - 7);
    }

    @Override
    public HeaderElement<Movie> createHeader(final String category) {
        return new HeaderElement<>(category);
    }

    @Override
    protected ContentElement<Movie> createContent(Movie obj) {
        Date dueDate = obj.getDueDate();
        if (dueDate == null) {
            return new ContentElement<>(obj.name(), "");
        } else {
            return new ContentElement<>(obj.name(), dateToText(obj.getDueDate()));
        }
    }

    @Override
    public DividerElement createDivider() {
        return new DividerElement();
    }

    @Override
    public int compareCategories(String cat1, String cat2) {
        if (cat1.equals(notRentedString) || cat2.equals(overdueString)) {
            return 1;
        }
        if (cat1.equals(overdueString) || cat2.equals(notRentedString)) {
            return -1;
        }
        return alphabeticalComparison(cat1, cat2);
    }

    @Override
    public int compareContent(ContentElement<Movie> element1, ContentElement<Movie> element2) {
        Date dateElement1 = textToDate("dd.MM.yyyy", element1.getMeta());
        Date dateElement2 = textToDate("dd.MM.yyyy", element2.getMeta());

        if(dateElement1 == null || dateElement2 == null)
        {
            return alphabeticalComparison(element1.getTitle(), element2.getTitle());
        }
        else
        {
            return dateElement1.compareTo(dateElement2);
        }
    }

    private HashMap<String, Integer> buildMonths() {
        HashMap<String, Integer> months = new HashMap<>();
        months.put("January", 1);
        months.put("February", 2);
        months.put("March", 3);
        months.put("April", 4);
        months.put("May", 5);
        months.put("June", 6);
        months.put("July", 7);
        months.put("August", 8);
        months.put("September", 9);
        months.put("October", 10);
        months.put("November", 11);
        months.put("December", 12);
        return months;
    }
}
