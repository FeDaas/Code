package de.moviemanager.ui.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import de.moviemanager.R;
import de.moviemanager.data.proxy.PortrayableProxy;

public class LinkedDataAdapter extends RecyclerView.Adapter<ViewHolder> {
    private final Context context;
    private final List<PortrayableProxy> data;
    private Consumer<PortrayableProxy> onItemClick;
    private final int itemLayout;

    public LinkedDataAdapter(final Context context, final List<PortrayableProxy> data, @LayoutRes int itemLayout) {
        this.context = context;
        this.itemLayout = itemLayout;
        this.data = Objects.requireNonNull(data);
        this.onItemClick = t -> Log.d("LinkedDataAdapter", "onItemClick: " + t);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, int i) {
        final View v = LayoutInflater.from(context).inflate(itemLayout, parent, false);
        v.setOnClickListener(this::onItemClick);
        final ViewHolder holder = new ViewHolder(v){};
        v.setTag(holder);
        return holder;
    }

    private void onItemClick(View v) {
        final ViewHolder holder = (ViewHolder) v.getTag();
        final int pos = holder.getAdapterPosition();
        final PortrayableProxy elem = data.get(pos);
        onItemClick.accept(elem);
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int position) {
        if (position >= getItemCount())
            return;

        final PortrayableProxy element = data.get(position);
        final ImageView showImage = viewHolder.itemView.findViewById(R.id.show_image);
        final TextView showTitle = viewHolder.itemView.findViewById(R.id.dialog_title);
        showImage.setImageDrawable(element.getImage(context));
        showTitle.setText(element.getName());
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void setOnItemClickListener(final Consumer<PortrayableProxy> onItemClick) {
        this.onItemClick = onItemClick;
    }

    public void update(final List<PortrayableProxy> updatedData) {
        data.clear();
        data.addAll(updatedData);
        notifyDataSetChanged();
    }
}
