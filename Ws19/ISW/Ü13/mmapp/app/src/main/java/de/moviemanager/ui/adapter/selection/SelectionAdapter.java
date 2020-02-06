package de.moviemanager.ui.adapter.selection;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.FrameLayout;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView.Adapter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiPredicate;

import de.moviemanager.R;
import de.moviemanager.ui.adapter.base.BaseFilter;
import de.moviemanager.ui.adapter.base.ContentBinder;
import de.moviemanager.ui.adapter.base.DirectFilterable;
import de.moviemanager.ui.adapter.base.IndexedAdapter;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

public abstract class SelectionAdapter<T>
        extends Adapter<SelectionViewHolder<T>>
        implements DirectFilterable, IndexedAdapter<SelectionProxy<T>> {
    protected final LayoutInflater inflater;
    protected final List<SelectionProxy<T>> data;
    protected final List<SelectionProxy<T>> modifiedData;

    @LayoutRes private int wrapperLayoutId = R.layout.listitem_selectable;
    @LayoutRes protected final int elementLayoutId;

    protected final BiPredicate<SelectionProxy<T>, CharSequence> filterCriterion;
    protected final Comparator<SelectionProxy<T>> orderCriterion;
    protected final ContentBinder<T> binder;

    protected SelectionAdapter(final Context context,
                               @LayoutRes int elementLayoutId,
                               final List<T> data,
                               final BiPredicate<T, CharSequence> filterCriterion,
                               final Comparator<T> orderCriterion,
                               final ContentBinder<T> binder) {
        this.inflater = LayoutInflater.from(context);
        this.elementLayoutId = elementLayoutId;
        this.data = data.stream().map(SelectionProxy::new).collect(toList());
        modifiedData = new ArrayList<>();

        this.filterCriterion = (proxy, seq) -> filterCriterion.test(proxy.getObj(), seq);
        this.orderCriterion = comparing(SelectionProxy::getObj, orderCriterion);
        this.binder = binder;

        updateWithFilteredData(this.data);
    }

    private void updateWithFilteredData(final List<SelectionProxy<T>> filtered) {
        modifiedData.clear();
        modifiedData.addAll(filtered);
        modifiedData.sort(orderCriterion);
        notifyDataSetChanged();
    }

    void setWrapperLayoutId(@LayoutRes int wrapperLayoutId) {
        this.wrapperLayoutId = wrapperLayoutId;
    }

    @NonNull
    @Override
    public SelectionViewHolder<T> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View view = inflater.inflate(wrapperLayoutId, parent, false);
        final FrameLayout root = view.findViewById(R.id.content);
        inflater.inflate(elementLayoutId, root, true);
        final SelectionViewHolder<T> holder = new SelectionViewHolder<>(this, view);
        holder.setOnSelectionChangedListener(this::onSelectionChanged);
        return holder;
    }

    protected abstract void onSelectionChanged(T element, boolean isSelected);

    @Override
    public void onBindViewHolder(@NonNull SelectionViewHolder<T> holder, int position) {
        holder.bindView(position, binder);
    }

    @Override
    public int getItemCount() {
        return modifiedData.size();
    }

    @Override
    public SelectionProxy<T> getElementByPosition(int position) {
        return modifiedData.get(position);
    }

    @Override
    public Filter getFilter() {
        return new BaseFilter<>(data, filterCriterion, this::updateWithFilteredData);
    }

    public void addElement(int position, T element) {
        final SelectionProxy<T> proxy = createProxyForInsert(element);
        onSelectionChanged(element, proxy.isEnabled());
        this.data.add(position, proxy);
        notifyItemInserted(position);
    }

    private SelectionProxy<T> createProxyForInsert(T element) {
        final SelectionProxy<T> proxy = new SelectionProxy<>(element);
        proxy.enable();
        return proxy;
    }

    public void filter(final CharSequence constraint) {
        getFilter().filter(constraint);
    }
}


