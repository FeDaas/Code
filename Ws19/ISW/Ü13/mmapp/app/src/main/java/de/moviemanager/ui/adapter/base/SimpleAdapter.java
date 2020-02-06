package de.moviemanager.ui.adapter.base;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;

import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView.Adapter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiPredicate;

import de.util.ObjectUtils;

public class SimpleAdapter<T>
        extends Adapter<SimpleViewHolder<T>>
        implements DirectFilterable, IndexedAdapter<T> {
    protected final LayoutInflater inflater;
    protected final List<T> data;
    private final List<T> modifiedData;

    @LayoutRes
    private final int elementLayoutId;
    @IdRes
    private final int rootId;

    private final BiPredicate<T, CharSequence> filterCriterion;
    private final Comparator<T> orderCriterion;
    private final ContentBinder<T> binder;

    private SimpleAdapter(final Context context,
                          @LayoutRes int elementLayoutId,
                          @IdRes int rootId,
                          final List<T> data,
                          final BiPredicate<T, CharSequence> filterCriterion,
                          final Comparator<T> orderCriterion,
                          final ContentBinder<T> binder) {
        this.inflater = LayoutInflater.from(context);
        this.elementLayoutId = elementLayoutId;
        this.rootId = rootId;
        this.data = new ArrayList<>(data);
        modifiedData = new ArrayList<>();

        this.filterCriterion = filterCriterion;
        this.orderCriterion = orderCriterion;
        this.binder = binder;

        updateWithFilteredData(this.data);
    }

    private void updateWithFilteredData(final List<T> filtered) {
        modifiedData.clear();
        modifiedData.addAll(filtered);
        if (orderCriterion != null) {
            modifiedData.sort(orderCriterion);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SimpleViewHolder<T> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View view = inflater.inflate(elementLayoutId, parent, false);
        return new SimpleViewHolder<>(this, view, rootId);
    }

    @Override
    public void onBindViewHolder(@NonNull SimpleViewHolder<T> holder, int position) {
        holder.bindView(position, binder);
    }

    @Override
    public int getItemCount() {
        return modifiedData.size();
    }

    @Override
    public T getElementByPosition(int position) {
        return modifiedData.get(position);
    }

    @Override
    public Filter getFilter() {
        if (filterCriterion != null) {
            return new BaseFilter<>(data, filterCriterion, this::updateWithFilteredData);
        } else {
            throw new UnsupportedOperationException("No filter defined!");
        }
    }

    @Override
    public void filter(final CharSequence constraint) {
        getFilter().filter(constraint);
    }

    public static <T> Builder<T> builder(final Context context) {
        return new Builder<>(context);
    }

    public static class Builder<T> {
        private final Context context;
        private @LayoutRes int elementLayoutId;
        private @IdRes int rootId;
        private List<T> data;
        private BiPredicate<T, CharSequence> filterCriterion;
        private Comparator<T> orderCriterion;
        private ContentBinder<T> binder;

        private Builder(final Context context) {
            this.context = context;
        }

        public Builder<T> setElementLayout(@LayoutRes int elementLayoutId, @IdRes int rootId) {
            this.elementLayoutId = elementLayoutId;
            this.rootId = rootId;
            return this;
        }

        public Builder<T> setData(final List<T> data) {
            this.data = data;
            return this;
        }

        public Builder<T> setFilterCriterion(BiPredicate<T, CharSequence> filterCriterion) {
            this.filterCriterion = filterCriterion;
            return this;
        }

        public Builder<T> setOrderCriterion(Comparator<T> orderCriterion) {
            this.orderCriterion = orderCriterion;
            return this;
        }

        public Builder<T> setBinder(ContentBinder<T> binder) {
            this.binder = binder;
            return this;
        }

        public SimpleAdapter<T> build() {
            ObjectUtils.requireAllNonNull(context, data, binder);

            return new SimpleAdapter<>(context,
                    elementLayoutId, rootId,
                    data,
                    filterCriterion,
                    orderCriterion,
                    binder
            );
        }
    }
}



