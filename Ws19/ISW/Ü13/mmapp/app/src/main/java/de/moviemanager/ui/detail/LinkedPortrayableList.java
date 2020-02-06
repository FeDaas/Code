package de.moviemanager.ui.detail;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.ObjIntConsumer;

import de.moviemanager.R;
import de.moviemanager.android.ResultHandling;
import de.moviemanager.android.ResultHandlingActivity;
import de.moviemanager.data.Portrayable;
import de.moviemanager.data.proxy.PersistentProxy;
import de.moviemanager.data.proxy.PortrayableProxy;
import de.moviemanager.data.proxy.TemporaryProxy;
import de.moviemanager.ui.adapter.LinkedDataAdapter;
import de.moviemanager.ui.detail.modifications.Modification;
import de.moviemanager.ui.dialog.PortrayableListDialog;
import de.moviemanager.ui.dialog.PortrayableListDialog.ListContext;
import de.moviemanager.ui.masterlist.MarginItemDecoration;
import de.moviemanager.util.RecyclerViewUtils;
import de.util.Pair;
import de.util.PrimitiveUtils;
import de.util.StringUtils;
import de.util.operationflow.ReversibleOperations.ReversibleTransformation;

import static de.moviemanager.ui.dialog.PortrayableListDialog.Mode.MULTI;
import static de.moviemanager.util.RecyclerViewUtils.addSwipeSupport;
import static de.util.Pair.paired;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

class LinkedPortrayableList<T extends Portrayable> {
    private static final int ACTION_LINK = 1;
    private static final int ACTION_NEUTRAL = 0;
    private static final int ACTION_UNLINK = -1;

    private final ResultHandlingActivity context;

    private final RecyclerView list;
    private final List<PortrayableProxy> linkedPortrayables;
    private final Map<PortrayableProxy, Integer> linkedPortrayableStates;
    private LinkedDataAdapter adapter;

    private Runnable onLinkedDataChanged;
    private Runnable onModificationUndo;
    private Consumer<Modification<?>> addModification;

    LinkedPortrayableList(final ResultHandlingActivity context, final RecyclerView list) {
        this.context = context;
        this.list = list;
        linkedPortrayables = new ArrayList<>();
        linkedPortrayableStates = new HashMap<>();
        initAdapter();
    }

    private void initAdapter() {
        RecyclerViewUtils.setLinearLayoutTo(context, list);
        adapter = new LinkedDataAdapter(context,
                linkedPortrayables,
                R.layout.listitem_portrayable_detail_edit
        );
        list.setAdapter(adapter);

        addSwipeSupport(context, list, R.drawable.ic_link_broken, this::onUnlinkSelected);
        list.addItemDecoration(new MarginItemDecoration(context, R.dimen.default_padding));
    }

    private void onUnlinkSelected(ViewHolder viewHolder) {
        int pos = viewHolder.getAdapterPosition();
        addModification(new Modification<>(Pair.paired(pos, linkedPortrayables.get(pos)), oldState -> {
            int position = oldState.first;
            PortrayableProxy obj = oldState.second;
            link(position, obj);
            onModificationUndo();
        }));
        unlink(pos);
    }

    private void addModification(Modification<?> mod) {
        addModification.accept(mod);
    }

    private void onModificationUndo() {
        onModificationUndo.run();
    }

    private void link(int index, PortrayableProxy obj) {
        linkedPortrayables.add(index, obj);
        linkedPortrayableStates.put(obj, ACTION_LINK);
        onLinkedDataChanged();
        adapter.notifyItemInserted(index);
    }

    private void unlink(int pos) {
        PortrayableProxy obj = linkedPortrayables.remove(pos);
        linkedPortrayableStates.put(obj, ACTION_UNLINK);
        onLinkedDataChanged();
        adapter.notifyItemRemoved(pos);
    }

    public void link(final PortrayableProxy obj) {
        link(linkedPortrayables.size(), obj);
    }

    <P extends Portrayable> void setInitialState(final List<P> data) {
        linkedPortrayables.clear();
        linkedPortrayableStates.clear();
        final List<PortrayableProxy> proxies = data.stream().
                map(PersistentProxy::new)
                .collect(toList());
        linkedPortrayables.addAll(proxies);
        proxies.forEach(proxy -> linkedPortrayableStates.put(proxy, ACTION_NEUTRAL));
        adapter.notifyDataSetChanged();
    }

    void showSelectionDialog(final Class<T> clazz,
                             final List<T> sample,
                             boolean hasAddButton,
                             final BiConsumer<ResultHandling, ListContext> createTemp
    ) {
        new PortrayableListDialog.Builder(context)
                .setMode(MULTI)
                .setTitle("Select at least one " + clazz.getSimpleName())
                .setData(sample)
                .setPositive(R.string.confirm, createPositiveListener())
                .setNegative(R.string.cancel, () -> {})
                .setAddListener(createTemp)
                .hasAddButton(hasAddButton)
                .show();
    }

    private Consumer<List<PortrayableProxy>> createPositiveListener() {
        return proxies -> {
            final Pair<List<PortrayableProxy>, Map<PortrayableProxy, Integer>> oldState = paired(
                    new ArrayList<>(linkedPortrayables),
                    new HashMap<>(linkedPortrayableStates)
            );
            addModification(new Modification<>(oldState, old -> {
                onModificationUndo();
                linkedPortrayables.clear();
                linkedPortrayables.addAll(old.first);
                linkedPortrayableStates.clear();
                linkedPortrayableStates.putAll(old.second);
                updateLinkedMovies();
            }));
            linkAll(proxies);
        };
    }

    private void linkAll(final List<PortrayableProxy> selected) {
        linkedPortrayables.addAll(selected);
        selected.forEach(p -> linkedPortrayableStates.put(p, ACTION_LINK));
        updateLinkedMovies();
    }

    private void updateLinkedMovies() {
        linkedPortrayables.sort(comparing(
                PortrayableProxy::getName,
                StringUtils::alphabeticalComparison)
        );
        adapter.notifyDataSetChanged();
        onLinkedDataChanged();
    }

    private void onLinkedDataChanged() {
        onLinkedDataChanged.run();
    }

    boolean containsNot(final PersistentProxy obj) {
        return !contains(obj);
    }

    boolean containsNot(Portrayable portrayable) {
        return linkedPortrayables.stream()
                .filter(PortrayableProxy::isTemporary)
                .map(TemporaryProxy.class::cast)
                .noneMatch(proxy -> proxy.matches(portrayable));
    }

    private boolean contains(final PortrayableProxy obj) {
        return linkedPortrayables.contains(obj);
    }

    boolean containsAtLeastOneElement() {
        return !linkedPortrayables.isEmpty();
    }

    public PortrayableProxy getFirst() {
        return linkedPortrayables.get(0);
    }

    <X extends Portrayable> ReversibleTransformation<X> toCommitOperation(
            final BiConsumer<X, T> storageLink, final BiConsumer<X, T> storageUnlink) {
        return new Commit<>(storageLink, storageUnlink);
    }

    List<TemporaryProxy> getTemporaries() {
        return linkedPortrayables.stream()
                .filter(PortrayableProxy::isTemporary)
                .map(TemporaryProxy.class::cast)
                .collect(toList());
    }


    void setOnLinkedDataChanged(Runnable onLinkedDataChanged) {
        this.onLinkedDataChanged = onLinkedDataChanged;
    }

    void setOnModificationUndo(Runnable onModificationUndo) {
        this.onModificationUndo = onModificationUndo;
    }

    void setAddModification(Consumer<Modification<?>> addModification) {
        this.addModification = addModification;
    }

    List<PortrayableProxy> getUnlinked() {
        return linkedPortrayableStates.keySet().stream()
                .filter(p -> PrimitiveUtils.fromObject(linkedPortrayableStates.get(p)) == ACTION_UNLINK)
                .collect(toList());
    }

    List<T> getPersistentOnes() {
        return linkedPortrayables.stream()
                .filter(PortrayableProxy::isPersistent)
                .map(PersistentProxy.class::cast)
                .map(PersistentProxy::getSource)
                .map(p -> (T) p)
                .collect(toList());
    }

    public int size() {
        return linkedPortrayables.size();
    }

    class Commit<X> implements ReversibleTransformation<X> {
        private final BiConsumer<X, T> storageLink;
        private final BiConsumer<X, T> storageUnlink;

        Commit(final BiConsumer<X, T> storageLink, final BiConsumer<X, T> storageUnlink) {
            this.storageLink = storageLink;
            this.storageUnlink = storageUnlink;
        }

        @Override
        public X forward(X obj) {
            forEach((proxy, state) -> {
                if (proxy.isTemporary())
                    return;
                final PersistentProxy persistent = (PersistentProxy) proxy;
                T portrayable = (T) persistent.getSource();

                if (state == ACTION_LINK)
                    storageLink.accept(obj, portrayable);
                else if (state == ACTION_UNLINK)
                    storageUnlink.accept(obj, portrayable);
            });
            return obj;
        }

        private void forEach(final ObjIntConsumer<PortrayableProxy> action) {
            linkedPortrayableStates
                    .entrySet()
                    .stream()
                    .sorted(comparing(e -> -e.getValue()))
                    .forEach(entry -> action.accept(entry.getKey(), entry.getValue()));
        }

        @Override
        public X backward(X obj) {
            forEach((proxy, state) -> {
                if (proxy.isTemporary())
                    return;
                final PersistentProxy persistent = (PersistentProxy) proxy;
                T portrayable = (T) persistent.getSource();

                if (state == ACTION_LINK)
                    storageUnlink.accept(obj, portrayable);
                else if (state == ACTION_UNLINK)
                    storageLink.accept(obj, portrayable);
            });
            return obj;
        }

    }
}
