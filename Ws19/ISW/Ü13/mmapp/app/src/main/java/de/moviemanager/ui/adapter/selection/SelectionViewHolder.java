package de.moviemanager.ui.adapter.selection;

import android.view.View;
import android.widget.CompoundButton;
import android.widget.FrameLayout;

import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import de.moviemanager.R;
import de.moviemanager.ui.adapter.base.ContentBinder;
import de.util.BiBooleanConsumer;

class SelectionViewHolder<T> extends ViewHolder {
    private final SelectionAdapter<T> adapter;
    private CompoundButton selectedMarker;
    private FrameLayout contentRoot;
    private BiBooleanConsumer<T> listener;

    SelectionViewHolder(final SelectionAdapter<T> adapter, final View item) {
        super(item);
        item.setTag(this);
        this.adapter = adapter;
        this.selectedMarker = item.findViewById(R.id.selection_marker);
        this.contentRoot = item.findViewById(R.id.content);
    }

    void setOnSelectionChangedListener(final BiBooleanConsumer<T> listener) {
        this.listener = listener;
        selectedMarker.setOnCheckedChangeListener(this::onCheckedChange);
    }

    private void onCheckedChange(final CompoundButton box, boolean isChecked) {
        if (listener != null && box.getId() == selectedMarker.getId()) {
            onItemClicked(listener, isChecked);
        }
    }

    private void onItemClicked(final BiBooleanConsumer<T> listener, boolean isChecked) {
        int position = getAdapterPosition();
        final SelectionProxy<T> element = adapter.getElementByPosition(position);
        listener.accept(element.getObj(), isChecked);
        element.switchEnabled();
    }

    void bindView(int position, ContentBinder<T> binder) {
        final SelectionProxy<T> element = adapter.getElementByPosition(position);
        selectedMarker.setOnCheckedChangeListener(null);
        selectedMarker.setChecked(element.isEnabled());
        selectedMarker.setOnCheckedChangeListener(this::onCheckedChange);
        binder.bindViewToElement(contentRoot, element.getObj());
    }
}
