package de.moviemanager.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.ObjIntConsumer;
import java.util.stream.Collectors;

import de.moviemanager.R;
import de.moviemanager.core.storage.RuntimeStorageAccess;
import de.moviemanager.data.Portrayable;
import de.util.Pair;

import static android.drm.DrmStore.DrmObjectType.CONTENT;
import static androidx.recyclerview.widget.RecyclerView.ViewHolder;
import static de.moviemanager.data.ImagePyramid.ImageSize.SMALL;
import static de.util.Pair.paired;
import static de.util.StringUtils.normedMinimumEditDistance;

public class SearchListAdapter<T extends Portrayable>
        extends RecyclerView.Adapter<RecyclerView.ViewHolder>
        implements Filterable {
    private static final RuntimeStorageAccess STORAGE = RuntimeStorageAccess.getInstance();

    public static final int MAX_RESULTS = 5;
    public static final int UNLIMITED = -1;
    private static final double[] THRESHOLDS = {0.0, 0.02, 0.03,
            0.05, 0.1, 0.25,
            0.4, 0.5, 0.55,
            0.65, 0.7};

    private final Context context;
    private final @LayoutRes int itemLayout;
    private List<T> originalData;
    private List<T> filteredData;
    private final List<T> limitedData;
    private final LayoutInflater layoutInflater;

    private final View.OnClickListener itemClickListener;
    private Consumer<T> onItemClick;
    private IntConsumer onSizeChangeListener;
    private ObjIntConsumer<String> onQueryProcessedListener;

    private final int listConstraint;

    public SearchListAdapter(@NonNull final Context context,
                             final List<T> originalData,
                             final int listConstraint) {
        this(context, originalData, listConstraint, true);
    }

    public SearchListAdapter(@NonNull final Context context,
                             final List<T> originalData,
                             final int listConstraint,
                             boolean useSmall) {
        this.context = context;
        this.originalData = originalData;
        this.layoutInflater = LayoutInflater.from(context);
        this.filteredData = new ArrayList<>(originalData);
        this.limitedData = new ArrayList<>();
        this.listConstraint = listConstraint;

        if(useSmall) {
            this.itemLayout = R.layout.listitem_portrayable_detail_small;
        } else {
            this.itemLayout = R.layout.listitem_portrayable_detail;
        }

        this.itemClickListener = v -> {
            ViewHolder holder = (ViewHolder) v.getTag();
            int pos = holder.getAdapterPosition();
            T elem = limitedData.get(pos);
            onItemClick.accept(elem);
        };
    }

    @Override
    public int getItemViewType(int position) {
        return CONTENT;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(itemLayout, parent, false);
        view.setOnClickListener(itemClickListener);
        ViewHolder holder = new ViewHolder(view) {
        };
        view.setTag(holder);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        if (position >= getItemCount())
            return;
        T element = limitedData.get(position);
        ImageView showImage = viewHolder.itemView.findViewById(R.id.show_image);
        TextView showTitle = viewHolder.itemView.findViewById(R.id.dialog_title);

        showImage.setImageDrawable(STORAGE.getImage(context.getApplicationContext(), element, SMALL).first);
        showTitle.setText(element.name());
    }

    @Override
    public int getItemCount() {
        return limitedData.size();
    }

    public void filter(final CharSequence constraint) {
        getFilter().filter(constraint);
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                if (constraint.length() == 0)
                    filteredData = new ArrayList<>();
                else
                    filteredData = applyFilter(originalData, constraint.toString());


                filteredData = applyLimitConstraint(filteredData, listConstraint);
                FilterResults results = new FilterResults();

                results.values = filteredData;
                results.count = filteredData.size();

                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                updateLimitedData((List<T>) results.values);
                if(onQueryProcessedListener != null) {
                    onQueryProcessedListener.accept(constraint.toString(), results.count);
                }
            }
        };
    }

    private static <E extends Portrayable> List<E> applyFilter(@NonNull final List<E> originalData,
                                                               final String constraint) {
        final List<E> results = originalData.stream()
                .map(v -> paired(v, normedMinimumEditDistance(constraint, v.name())))
                .sorted(Comparator.comparing(Pair::getSecond))
                .filter(p -> p.second > adaptiveThreshold(constraint.length()))
                .map(Pair::getFirst)
                .collect(Collectors.toList());
        Collections.reverse(results);
        return results;
    }

    private static double adaptiveThreshold(int length) {

        if(THRESHOLDS.length <= length) {
            return THRESHOLDS[THRESHOLDS.length - 1];
        }

        return THRESHOLDS[length];
    }

    private static <E> List<E> applyLimitConstraint(final List<E> data, int constraint) {
        List<E> result = data;
        if (constraint == MAX_RESULTS && data.size() > MAX_RESULTS) {
            result = data.stream().limit(MAX_RESULTS).collect(Collectors.toList());
        }
        return result;
    }

    public void refilterList(List<T> newData, CharSequence constraint) {
        this.originalData = new ArrayList<>(newData);
        this.filteredData = applyFilter(originalData, constraint.toString());
        updateLimitedData(applyLimitConstraint(filteredData, listConstraint));
    }

    private void updateLimitedData(List<T> results) {
        this.limitedData.clear();
        this.limitedData.addAll(results);
        if(onSizeChangeListener != null) {
            onSizeChangeListener.accept(limitedData.size());
        }
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(Consumer<T> onItemClick) {
        this.onItemClick = onItemClick;
    }

    public int getResultSize() {
        return limitedData.size();
    }

    public void setOnSizeChangeListener(final IntConsumer listener) {
        this.onSizeChangeListener = listener;
    }

    public void setOnQueryProcessedListener(final ObjIntConsumer<String> listener) {
        this.onQueryProcessedListener = listener;
    }

    public double bestGuessValue(final String word) {
        if(limitedData.isEmpty()) {
            return 0.0;
        } else {
            return normedMinimumEditDistance(limitedData.get(0).name(), word);
        }
    }
}
