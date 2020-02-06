package de.moviemanager.ui.masterlist.categorizer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiPredicate;

import de.moviemanager.ui.masterlist.ElementOrder;
import de.moviemanager.ui.masterlist.OrderState;
import de.moviemanager.ui.masterlist.elements.Element;

import static java.util.stream.IntStream.range;

public class OrderGroup<T> implements Iterable<ElementOrder<T>> {
    private final int defaultIndex;
    private int selectionIndex;
    private final List<ElementOrder<T>> orders;

    public OrderGroup(int defaultIndex) {
        orders = new ArrayList<>();
        this.defaultIndex = defaultIndex;
        this.selectionIndex = -1;
    }

    public void addOrder(String name, Categorizer<String, T> categorizer, BiPredicate<T, String> filterLogic) {
        ElementOrder<T> order = new ElementOrder<>(name, categorizer, filterLogic);
        orders.add(order);
    }

    public int getDefaultIndex() {
        return defaultIndex;
    }

    public List<Element> select(int index, List<T> input) {
        unselectAllExcept(index);
        this.selectionIndex = index;
        return orders.get(index).select(input);
    }

    public List<Element> reselect(int index, List<T> input) {
        unselectAllExcept(index);
        this.selectionIndex = index;
        return orders.get(index).reselect(input);
    }

    private void unselectAllExcept(int index) {
        range(0, orders.size())
                .filter(i -> i != index)
                .mapToObj(orders::get)
                .forEach(ElementOrder::unselect);
    }

    public int getSelectionIndex() {
        return selectionIndex;
    }

    public String getName(int index) {
        return orders.get(index).getName();
    }

    @Override
    public Iterator<ElementOrder<T>> iterator() {
        return orders.iterator();
    }

    public boolean isDescending() {
        return orders.get(selectionIndex).isDescending();
    }
    
    public OrderState getState() {
        return orders.get(selectionIndex).getState();
    }

    public BiPredicate<T, String> getFilterLogic() {
        return orders.get(selectionIndex).getFilterLogic();
    }
}
