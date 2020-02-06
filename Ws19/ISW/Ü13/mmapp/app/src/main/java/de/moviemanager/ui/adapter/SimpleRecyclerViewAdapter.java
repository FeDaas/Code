package de.moviemanager.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import static de.moviemanager.ui.masterlist.elements.Type.CONTENT;

public class SimpleRecyclerViewAdapter extends RecyclerView.Adapter<ViewHolder> {
    private final LayoutInflater inflater;
    private final List<Map<String, ?>> data;
    private final int resource;
    private String[] fieldNames;
    private int[] boundViews;
    private List<Function<Object, String>> mappers;
    private OnClickListener onItemClick;

    public SimpleRecyclerViewAdapter(final Context context,
                                     final List<Map<String, ?>> data,
                                     @LayoutRes int resource,
                                     final String[] fieldNames,
                                     int[] boundViews,
                                     final List<Function<Object, String>> mappers) {
        this.inflater = LayoutInflater.from(context);
        this.data = data;
        this.resource = resource;
        if(fieldNames.length != boundViews.length) {
            throw new IllegalArgumentException("Field names and bound views don't match in size.");
        }
        this.fieldNames = fieldNames;
        this.boundViews = boundViews;
        this.mappers = mappers;
        this.onItemClick = v -> {};
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = inflater.inflate(resource, viewGroup, false);
        view.setOnClickListener(onItemClick);
        ViewHolder holder = new ViewHolder(view) {};
        view.setTag(holder);
        return holder;
    }

    public void setOnItemClick(OnClickListener onItemClick) {
        this.onItemClick = Objects.requireNonNull(onItemClick);
    }

    @Override
    public int getItemViewType(int position) {
        return CONTENT.ordinal();
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int pos) {
        Map<String, ?> map = data.get(pos);
        for(int i = 0; i < fieldNames.length; ++i) {
            String key = fieldNames[i];
            String content = mappers.get(i).apply(map.get(key));
            int id = boundViews[i];

            View view = viewHolder.itemView.findViewById(id);
            ((TextView) view).setText(content);
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }
}
