package de.moviemanager.ui.masterlist;

import java.util.List;
import java.util.function.BiPredicate;

import de.moviemanager.ui.masterlist.categorizer.Categorizer;
import de.moviemanager.ui.masterlist.elements.Element;

public class ElementOrder<T> {
    private final String name;
    private final Categorizer<String, T> categorizer;
    private final BiPredicate<T, String> filterLogic;
    private OrderState state;

    public ElementOrder(final String name,
                        final Categorizer<String, T> categorizer,
                        final BiPredicate<T, String> filterLogic) {
        this.name = name;
        this.categorizer = categorizer;
        this.filterLogic = filterLogic;
        this.state = OrderState.ASCENDING;
    }


    public List<Element> select(final List<T> input) {
        if (isNeutral()) {
            state = OrderState.ASCENDING;
        } else {
            state = state.swap();
        }
        return reselect(input);
    }

    public List<Element> reselect(final List<T> input) {
        return categorizer.createToCategorizedList(input, isDescending());
    }

    public void unselect() {
        this.state = OrderState.NEUTRAL;
    }

    public String getName() {
        return name;
    }

    private boolean isNeutral() {
        return state == OrderState.NEUTRAL;
    }

    public boolean isDescending() {
        return state == OrderState.DESCENDING;
    }

    public boolean isAscending() {
        return state == OrderState.ASCENDING;
    }

    public OrderState getState() {
        return state;
    }

    public BiPredicate<T, String> getFilterLogic() {
        return filterLogic;
    }
}
