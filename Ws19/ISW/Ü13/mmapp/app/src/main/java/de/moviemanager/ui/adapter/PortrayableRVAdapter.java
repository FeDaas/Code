package de.moviemanager.ui.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ItemDecoration;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.google.android.material.snackbar.Snackbar;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import de.moviemanager.R;
import de.moviemanager.android.ResultHandlingFragment;
import de.moviemanager.core.storage.RuntimeStorageAccess;
import de.moviemanager.data.Movie;
import de.moviemanager.data.Portrayable;
import de.moviemanager.ui.masterfragments.onetimetask.OneTimeTaskExecutorRudiment;
import de.moviemanager.ui.masterlist.ElementOrder;
import de.moviemanager.ui.masterlist.categorizer.OrderGroup;
import de.moviemanager.ui.masterlist.elements.ContentElement;
import de.moviemanager.ui.masterlist.elements.Element;
import de.moviemanager.ui.masterlist.elements.HeaderElement;
import de.moviemanager.ui.masterlist.elements.Type;
import de.moviemanager.ui.masterlist.sticky.HeaderItemDecoration;
import de.moviemanager.ui.masterlist.sticky.HeaderListener;
import de.moviemanager.ui.masterlist.swipe.SwipeController;
import de.moviemanager.ui.masterlist.viewholder.ContentViewHolder;
import de.moviemanager.ui.masterlist.viewholder.DividerViewHolder;
import de.moviemanager.ui.masterlist.viewholder.HeaderViewHolder;
import de.moviemanager.ui.masterlist.viewholder.TypedViewHolder;
import de.util.Pair;
import de.util.operationflow.ReversibleOperations.ReversibleTransformation;
import de.util.operationflow.ReversibleTransaction;
import de.util.operationflow.Transaction;

import static de.moviemanager.data.ImagePyramid.ImageSize.MEDIUM;
import static de.moviemanager.ui.detail.PortrayableDetailActivity.INITIAL_PORTRAYABLE;
import static de.util.ObjectUtils.requireAllNonNull;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

public abstract class PortrayableRVAdapter<T extends Portrayable>
        extends RecyclerView.Adapter<TypedViewHolder>
        implements HeaderListener, Filterable {
    protected static final RuntimeStorageAccess STORAGE = RuntimeStorageAccess.getInstance();

    private final OneTimeTaskExecutorRudiment taskExecutor;
    private final ResultHandlingFragment host;
    private final Context context;
    private final OrderGroup<T> orders;
    private Pair<Integer, String> orderMeta;
    private final List<T> modelData;
    private final List<Element> originalData;
    private final List<Element> filteredData;
    private final LayoutInflater layoutInflater;
    private final View.OnClickListener itemClickListener;

    private BiPredicate<T, String> filter;

    PortrayableRVAdapter(final OneTimeTaskExecutorRudiment taskExecutor,
                                   @NonNull final ResultHandlingFragment host,
                                   @NonNull final OrderGroup<T> orders,
                                   final List<T> modelData,
                                   final String constraint) {
        this.taskExecutor = taskExecutor;
        this.host = host;
        this.context = host.getContext();
        this.orders = orders;
        this.modelData = modelData;
        this.originalData = new ArrayList<>();
        this.filteredData = new ArrayList<>();

        orders.forEach(ElementOrder::unselect);
        selectOrder(orders.getDefaultIndex(), constraint);

        this.layoutInflater = LayoutInflater.from(context);
        itemClickListener = v -> {
            T model = getModelFromViewHolder((TypedViewHolder) v.getTag());
            switchToDetailsOf(model);
        };
    }

    private T getModelFromViewHolder(@NonNull TypedViewHolder holder) {
        int position = holder.getAdapterPosition();
        ContentElement<T> content = (ContentElement<T>) filteredData.get(position);
        return content.retrieveContentModel();
    }

    public void createObject() {
        Intent intent = host.createIntent(getDetailEditActivity());
        intent.putExtra(INITIAL_PORTRAYABLE, (T) null);
        host.startActivityForResult(intent, Activity.RESULT_OK, this::afterRequestedCreation);
    }

    protected abstract Class<? extends Activity> getDetailEditActivity();

    protected abstract void afterRequestedCreation(final Intent result);

    private ReversibleTransaction<T> removeObject(T model) {
        modelData.remove(model);
        final ReversibleTransaction<T> transaction = removeFromStorage(model);
        transaction.commit();

        reselectOrder();
        return transaction;
    }

    protected abstract ReversibleTransaction<T> removeFromStorage(T model);

    private void switchToDetailsOf(@NonNull T model) {
        final Intent intent = new Intent(context, getDetailActivity());
        intent.putExtra(INITIAL_PORTRAYABLE, model);
        host.startActivityForResult(intent, this::afterUpdate);
    }

    protected abstract Class<? extends Activity> getDetailActivity();

    protected abstract void afterUpdate(Intent result);

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        final SwipeController swipeController = new SwipeController(
                context,
                R.drawable.ic_delete_enabled,
                vh -> onDeleteSelected(recyclerView, vh));
        final ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeController);
        final ItemDecoration stickHeaderDecoration = new HeaderItemDecoration(this);

        while (recyclerView.getItemDecorationCount() > 0) {
            recyclerView.removeItemDecorationAt(0);
        }

        recyclerView.addItemDecoration(stickHeaderDecoration);
        itemTouchhelper.attachToRecyclerView(recyclerView);
        recyclerView.addItemDecoration(new ItemDecoration() {
            @Override
            public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                swipeController.onDraw(c);
            }
        });
    }

    private void onDeleteSelected(final RecyclerView recyclerView,
                                  @NonNull final ViewHolder vh) {
        int pos = vh.getAdapterPosition();
        final Element elem = filteredData.get(pos);
        final ContentElement<T> content = (ContentElement<T>) elem;
        final T model = content.retrieveContentModel();
        canDelete(model, obj -> {
            final ReversibleTransaction<T> transaction = removeObject(obj);
            showUndo(recyclerView, obj, transaction);
        });
    }

    protected void canDelete(T model, Consumer<T> delete) {
        delete.accept(model);
    }

    private void showUndo(final RecyclerView recyclerView,
                              final T obj,
                              final ReversibleTransaction<T> transaction) {
        final ViewGroup root = (ViewGroup) recyclerView.getParent();
        final String message = format(context.getString(R.string.undo_message), obj.name());
        final int snackbarTextId = com.google.android.material.R.id.snackbar_text;

        final Snackbar undoMessage = Snackbar.make(
                root,
                message,
                Snackbar.LENGTH_INDEFINITE
        );
        undoMessage.setActionTextColor(context.getColor(R.color.colorAccent));
        final View view = undoMessage.getView();
        view.setBackgroundResource(R.drawable.background_border_view);
        ((TextView) view.findViewById(snackbarTextId)).setTextColor(context.getColor(R.color.black));

        taskExecutor.addOneTimeTask(undoMessage::dismiss);
        undoMessage.setAction(R.string.undo_action, v -> recoverObject(transaction, obj));
        undoMessage.show();
    }

    private void recoverObject(@NonNull Transaction<T, ReversibleTransformation<T>> transaction, T model) {
        transaction.rollback();
        modelData.add(model);
        reselectOrder();
    }

    @Override
    public int getItemViewType(int position) {
        return filteredData.get(position).getTypeAsInt();
    }

    @NonNull
    @Override
    public TypedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        Type type = Type.values()[viewType];
        switch (type) {
            case CONTENT:
                view = layoutInflater.inflate(R.layout.listitem_master_content, parent, false);
                view.setOnClickListener(itemClickListener);
                return new ContentViewHolder(view);
            case DIVIDER:
                view = layoutInflater.inflate(R.layout.listitem_master_divider, parent, false);
                return new DividerViewHolder(view);
            case HEADER:
            default:
                view = layoutInflater.inflate(R.layout.listitem_master_header, parent, false);
                return new HeaderViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final TypedViewHolder viewHolder, int position) {
        if (position >= getItemCount())
            return;
        final Element element = filteredData.get(position);

        if (!element.hasSameTypeAs(viewHolder)) {
            throw new InvalidParameterException("Required " + element.getTypeAsString()
                    + " as ViewHolder-Type, but found " + viewHolder.getTypeAsString());
        }

        if (element.isHeader()) {
            bindHeaderDataToView(viewHolder, element);
        } else if (element.isContent()) {
            bindContentDataToView(viewHolder, element);
        }
    }

    private void bindHeaderDataToView(final ViewHolder viewHolder, final Element element) {
        final HeaderViewHolder holder = (HeaderViewHolder) viewHolder;
        final HeaderElement<T> hElement = (HeaderElement<T>) element;
        holder.setCategoryText(hElement.getHeader());
    }

    private void bindContentDataToView(ViewHolder viewHolder, Element element) {
        ContentViewHolder holder = (ContentViewHolder) viewHolder;
        ContentElement<T> cElement = (ContentElement<T>) element;
        holder.setImage(STORAGE.getImage(context, cElement.retrieveContentModel(), MEDIUM).first);
        holder.setTitle(cElement.getContent());
        holder.setMetaText(cElement.getMeta());

        // Subject to change, just a quick hack for testing purposes
        List<Movie> movies = STORAGE.getMovies();
        for (Movie m : movies) {
            if(m.getTitle().equals(cElement.getTitle())) {
                holder.setRented(m.getDueDate());
            }
        }
    }

    @Override
    public int getItemCount() {
        return filteredData.size();
    }

    @Override
    public int getHeaderPositionForItemAt(int itemPosition) {
        int headerPosition = 0;
        do {
            if (this.isItemHeader(itemPosition)) {
                headerPosition = itemPosition;
                break;
            }
            itemPosition -= 1;
        } while (itemPosition >= 0);
        return headerPosition;
    }

    private boolean isItemHeader(int itemPosition) {
        if (itemPosition < 0 || itemPosition >= getItemCount())
            return false;
        return filteredData.get(itemPosition).isHeader();
    }

    @Override
    public int getHeaderLayout(int headerPosition) {
        return R.layout.listitem_master_header;
    }

    @Override
    public void bindStickyHeaderData(View header, int headerPosition) {
        if (headerPosition >= getItemCount())
            return;
        final ViewHolder viewHolder = new HeaderViewHolder(header);
        final Element element = filteredData.get(headerPosition);

        bindHeaderDataToView(viewHolder, element);
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(final CharSequence constraint) {
                orderMeta = orderMeta.mapSecond(s -> constraint.toString());
                final FilterResults results = new FilterResults();
                final List<Element> filteredOriginal = new ArrayList<>(originalData);
                applyFilter(filteredOriginal, constraint);

                results.values = filteredOriginal;
                results.count = filteredOriginal.size();
                return results;
            }

            @Override
            protected void publishResults(final CharSequence constraint,
                                          final FilterResults results) {
                filteredData.clear();
                filteredData.addAll((List<Element>) results.values);
                notifyDataSetChanged();
            }
        };
    }

    private void applyFilter(@NonNull List<Element> filteredData, CharSequence constraint) {
        filteredData.forEach(e -> e.setVisible(true));
        if (constraint != null && constraint.length() > 0) {
            filteredData.stream()
                    .filter(Element::isHeader)
                    .map(e -> (HeaderElement<T>) e)
                    .forEach(h -> h.filter(o -> filter.test(o, constraint.toString())));
        }
        filteredData.removeIf(e -> !e.isVisible());
    }

    public void selectOrder(int index, String constraint) {
        orderMeta = Pair.paired(index, constraint);
        originalData.clear();
        filteredData.clear();
        originalData.addAll(orders.select(index, modelData));
        filter = orders.getFilterLogic();
        filteredData.addAll(originalData);
        applyFilter(filteredData, constraint);
        notifyDataSetChanged();
    }

    public void reselectOrder() {
        int index = orderMeta.first;
        final String constraint = orderMeta.second;
        this.originalData.clear();
        filteredData.clear();
        this.originalData.addAll(orders.reselect(index, modelData));
        this.filteredData.addAll(originalData);
        applyFilter(filteredData, constraint);
        notifyDataSetChanged();
    }

    public int getNumberOfDisplayedItems() {
        return filteredData.size();
    }

    public Element getItem(int index) {
        return filteredData.get(index);
    }

    public static <P extends Portrayable> PortrayableRVAdapterBuilder<P> builder() {
        return new PortrayableRVAdapterBuilder<>();
    }

    public static class PortrayableRVAdapterBuilder<T extends Portrayable> {
        private OneTimeTaskExecutorRudiment taskExecutor;
        private ResultHandlingFragment host;
        private OrderGroup<T> orders;
        private List<T> modelData;
        private String constraint;
        
        private Supplier<Class<? extends Activity>> getDetailActivity;
        private Supplier<Class<? extends Activity>> getDetailEditActivity;
        private Consumer<Intent> afterRequestedCreation;
        private Function<T, ReversibleTransaction<T>> removeFromStorage;
        private Consumer<Intent> afterUpdate;
        private BiConsumer<T, Consumer<T>> canDelete;

        public PortrayableRVAdapterBuilder<T> setTaskExecutor(
                final OneTimeTaskExecutorRudiment taskExecutor) {
            this.taskExecutor = taskExecutor;
            return this;
        }

        public PortrayableRVAdapterBuilder<T> setHost(final ResultHandlingFragment host) {
            this.host = host;
            return this;
        }

        public PortrayableRVAdapterBuilder<T> setOrders(final OrderGroup<T> orders) {
            this.orders = orders;
            return this;
        }

        public PortrayableRVAdapterBuilder<T> setModelData(final List<T> modelData) {
            this.modelData = modelData;
            return this;
        }

        public PortrayableRVAdapterBuilder<T> setConstraint(final String constraint) {
            this.constraint = constraint;
            return this;
        }

        public PortrayableRVAdapterBuilder<T> setDetailActivityGetter(
                final Supplier<Class<? extends Activity>> getDetailActivity) {
            this.getDetailActivity = getDetailActivity;
            return this;
        }

        public PortrayableRVAdapterBuilder<T> setDetailEditActivityGetter(
                final Supplier<Class<? extends Activity>> getDetailEditActivity) {
            this.getDetailEditActivity = getDetailEditActivity;
            return this;
        }

        public PortrayableRVAdapterBuilder<T> setAfterRequestedCreation(
                final Consumer<Intent> afterRequestedCreation) {
            this.afterRequestedCreation = afterRequestedCreation;
            return this;
        }

        public PortrayableRVAdapterBuilder<T> setRemoveFromStorage(
                final Function<T, ReversibleTransaction<T>> removeFromStorage) {
            this.removeFromStorage = removeFromStorage;
            return this;
        }

        public PortrayableRVAdapterBuilder<T> setAfterUpdate(
                final Consumer<Intent> afterUpdate) {
            this.afterUpdate = afterUpdate;
            return this;
        }

        public PortrayableRVAdapterBuilder<T> setCanDelete(
                final BiConsumer<T, Consumer<T>> canDelete) {
            this.canDelete = canDelete;
            return this;
        }

        public PortrayableRVAdapter<T> build() {
            requireAllNonNull(taskExecutor, host, orders, modelData, constraint);
            return new PortrayableRVAdapter<T>(taskExecutor, host, orders, modelData, constraint) {
                @Override
                protected Class<? extends Activity> getDetailEditActivity() {
                    return requireNonNull(getDetailEditActivity).get();
                }

                @Override
                protected void afterRequestedCreation(final Intent result) {
                    requireNonNull(afterRequestedCreation).accept(result);
                }

                @Override
                protected ReversibleTransaction<T> removeFromStorage(final T model) {
                    return requireNonNull(removeFromStorage).apply(model);
                }

                @Override
                protected Class<? extends Activity> getDetailActivity() {
                    return requireNonNull(getDetailActivity).get();
                }

                @Override
                protected void afterUpdate(final Intent result) {
                    requireNonNull(afterUpdate).accept(result);
                }

                @Override
                protected void canDelete(final T model, final Consumer<T> delete) {
                    if(canDelete == null) {
                        super.canDelete(model, delete);
                    } else {
                        canDelete.accept(model, delete);
                    }
                }
            };
        }
    }
}

