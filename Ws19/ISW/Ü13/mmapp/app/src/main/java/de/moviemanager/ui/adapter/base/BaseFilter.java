package de.moviemanager.ui.adapter.base;

import android.widget.Filter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import static java.util.stream.Collectors.toList;

public class BaseFilter<T> extends Filter {
    private final List<T> data;
    private final BiPredicate<T, CharSequence> filterCriterion;
    private final Consumer<List<T>> update;

    public BaseFilter(final List<T> originalData,
               final BiPredicate<T, CharSequence> filterCriterion,
               final Consumer<List<T>> update) {
        this.data = originalData;
        this.filterCriterion = filterCriterion;
        this.update = update;
    }

    @Override
    protected FilterResults performFiltering(final CharSequence sequence) {
        List<T> resultList;

        if (isNonEmpty(sequence)) {
            resultList = data.stream()
                    .filter(obj -> filterCriterion.test(obj, sequence))
                    .collect(toList());
        } else {
            resultList = new ArrayList<>(data);
        }

        final FilterResults results = new FilterResults();
        results.count = resultList.size();
        results.values = resultList;
        return results;
    }

    private boolean isNonEmpty(final CharSequence sequence) {
        return sequence != null && sequence.length() > 0;
    }

    @Override
    protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
        if (filterResults != null) {
            Object obj = filterResults.values;
            update.accept((List<T>) obj);
        }
    }

    public static <T> BiPredicate<T, CharSequence> createFilterCriterion(
            final Function<T, CharSequence> mapper,
            final UnaryOperator<CharSequence> preprocessing,
            final BiPredicate<CharSequence, CharSequence> comparison) {
        return (obj, seq) -> {
            final CharSequence objectSequence = preprocessing.apply(mapper.apply(obj));
            final CharSequence externalSequence = preprocessing.apply(seq);
            return comparison.test(objectSequence, externalSequence);
        };
    }
}
