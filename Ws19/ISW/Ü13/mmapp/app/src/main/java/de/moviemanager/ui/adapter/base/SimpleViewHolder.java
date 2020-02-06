package de.moviemanager.ui.adapter.base;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

public class SimpleViewHolder<T> extends ViewHolder {
    private final IndexedAdapter<T> adapter;
    public final ViewGroup root;

    SimpleViewHolder(final IndexedAdapter<T> adapter,
                            @NonNull View itemView,
                            @IdRes int rootId) {
        super(itemView);
        itemView.setTag(this);
        this.adapter = adapter;
        root = itemView.findViewById(rootId);
    }

    public void bindView(int position, ContentBinder<T> binder) {
        final T element = adapter.getElementByPosition(position);
        binder.bindViewToElement(root, element);
    }
}
