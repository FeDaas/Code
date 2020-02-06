package de.moviemanager.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import java.util.List;

import de.moviemanager.R;
import de.moviemanager.ui.view.OrderMenuItem;

import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;

public class OrderWindowAdapter extends BaseAdapter {
    private final LayoutInflater inflater;
    private final List<OrderMenuItem> items;

    public OrderWindowAdapter(final Context context, final List<OrderMenuItem> items) {
        this.inflater = LayoutInflater.from(requireNonNull(context));
        this.items = unmodifiableList(requireNonNull(items));
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public OrderMenuItem getItem(int i) {
        return items.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if(view == null) {
            view = inflater.inflate(R.layout.menu_item_sort_order, null);
            viewHolder = new ViewHolder(view) {};
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        getItem(i).updateView(viewHolder.itemView);

        return view;
    }
}