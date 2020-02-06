package de.moviemanager.ui.dialog;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import de.moviemanager.R;
import de.moviemanager.android.PluginBasedResultHandling;
import de.moviemanager.android.ResultHandling;
import de.moviemanager.android.ResultHandlingPlugin;
import de.moviemanager.data.Portrayable;
import de.moviemanager.data.proxy.PersistentProxy;
import de.moviemanager.data.proxy.PortrayableProxy;
import de.moviemanager.ui.adapter.base.ContentBinder;
import de.moviemanager.ui.adapter.base.DirectFilterable;
import de.moviemanager.ui.adapter.base.SimpleAdapter;
import de.moviemanager.ui.adapter.selection.MultiSelectionListener;
import de.moviemanager.ui.adapter.selection.SelectionAdapter;
import de.moviemanager.util.autobind.AutoBind;
import de.moviemanager.util.autobind.Bind;
import de.util.StringUtils;

import static de.moviemanager.ui.adapter.selection.SelectionAdapters.multiSelection;
import static de.moviemanager.ui.adapter.selection.SelectionAdapters.singleSelection;
import static de.moviemanager.util.BundleUtils.getOrDefault;
import static java.util.Comparator.comparing;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

public class PortrayableListDialog extends DialogFragment implements PluginBasedResultHandling {
    public enum Mode {
        SINGLE, MULTI, CONFIRMATION
    }

    private static final String ARG_MODE = "mode";
    private static final String ARG_TITLE = "title";
    private static final String ARG_MESSAGE = "message";
    private static final String ARG_DATA = "data";
    private static final String ARG_POSITIVE = "positive";
    private static final String ARG_NEGATIVE = "negative";
    private static final String ARG_HAS_ADD_BUTTON = "hasAddButton";

    private final ResultHandlingPlugin resultHandlingPlugin = new ResultHandlingPlugin(this);

    @Bind(R.id.root) private ConstraintLayout root;
    @Bind(R.id.dialog_title) private TextView showTitle;
    @Bind(R.id.dialog_message) private TextView showMessage;
    @Bind(R.id.search_bar) private SearchView search;
    @Bind(R.id.portrayables) private RecyclerView list;
    @Bind(R.id.positive_button) private Button positiveButton;
    @Bind(R.id.negative_button) private Button negativeButton;
    @Bind(R.id.add_button) private ImageButton addBtn;

    private Mode mode;
    private String message;
    private boolean hasAddButton;
    private List<PortrayableProxy> data;
    private List<PortrayableProxy> result;
    private Adapter<? extends ViewHolder> adapter;
    private Consumer<List<PortrayableProxy>> positiveListener;
    private Runnable negativeListener;
    private BiConsumer<ResultHandling, ListContext> addListener;
    private ListContext listContext;

    private PortrayableListDialog() {
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (getActivity() == null) {
            throw new IllegalStateException("Activity must be non-null");
        }

        return new Dialog(getActivity(), getTheme()) {
            @Override
            public void onBackPressed() {
                if (negativeButton.getVisibility() == View.VISIBLE)
                    negativeListener.run();
                dismiss();
            }
        };
    }

    @Override
    public ResultHandlingPlugin getPlugin() {
        return resultHandlingPlugin;
    }

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater,
                             final ViewGroup container,
                             final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_portrayable_list, container);
    }

    @Override
    public void onViewCreated(@NonNull final View view,
                              @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setMembersFromArguments();

        AutoBind.bindAll(this, view);
        setupList();
        setupUIForMode();
    }

    private void setMembersFromArguments() {
        final Bundle args = getArguments();

        if (args != null) {
            mode = Mode.values()[args.getInt(ARG_MODE)];

            message = args.getString(ARG_MESSAGE);
            result = new ArrayList<>();
        }
    }

    private void setupList() {
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        list.setLayoutManager(linearLayoutManager);

        data = ofNullable(getArguments())
                .map(bundle -> bundle.getParcelableArrayList("data"))
                .orElseGet(ArrayList::new)
                .stream()
                .map(obj -> (Portrayable) obj)
                .map(PersistentProxy::new)
                .collect(toList());

        adapter = chooseAdapter();
        list.setAdapter(adapter);
    }

    private Adapter<? extends ViewHolder> chooseAdapter() {
        switch (mode) {
            case CONFIRMATION:
                return createSimpleAdapter();
            case SINGLE:
                return createSingleAdapter();
            case MULTI:
                return createMultiAdapter();
            default:
                return null;
        }
    }

    private SimpleAdapter<PortrayableProxy> createSimpleAdapter() {
        return SimpleAdapter.<PortrayableProxy>builder(getContext())
                .setData(data)
                .setElementLayout(R.layout.listitem_portrayable_detail_small, R.id.root)
                .setOrderCriterion(comparing(
                        PortrayableProxy::getName,
                        StringUtils::alphabeticalComparison)
                )
                .setBinder(createBinder())
                .build();
    }

    private ContentBinder<PortrayableProxy> createBinder() {
        return (parent, element) -> {
            showImage(parent, element);
            showName(parent, element);
        };
    }

    private void showImage(final ViewGroup parent, final PortrayableProxy element) {
        final ImageView imageView = parent.findViewById(R.id.show_image);
        imageView.setImageDrawable(element.getImage(getContext()));
    }

    private void showName(final ViewGroup parent, final PortrayableProxy element) {
        final TextView nameView = parent.findViewById(R.id.dialog_title);
        nameView.setText(element.getName());
    }

    private SelectionAdapter<PortrayableProxy> createSingleAdapter() {
        return singleSelection(getContext(), this::runSingleSelect)
                .setElementLayoutId(R.layout.listitem_portrayable_detail)
                .setData(data)
                .setFilterCriterion(this::filterProxy)
                .setOrderCriterion(comparing(
                        PortrayableProxy::getName,
                        StringUtils::alphabeticalComparison)
                )
                .setContentBinder(createBinder())
                .build();
    }

    private void runSingleSelect(final PortrayableProxy element) {
        result.clear();
        if (element != null) {
            result.add(element);
            setPositiveEnabled(true);
            positiveListener.accept(result);
        } else {
            setPositiveEnabled(false);
        }
    }

    private SelectionAdapter<PortrayableProxy> createMultiAdapter() {
        return multiSelection(getContext(), createSelectionListener())
                .setElementLayoutId(R.layout.listitem_portrayable_detail)
                .setData(data)
                .setFilterCriterion(this::filterProxy)
                .setOrderCriterion(comparing(
                        PortrayableProxy::getName,
                        StringUtils::alphabeticalComparison)
                )
                .setContentBinder(createBinder())
                .build();
    }

    private boolean filterProxy(final PortrayableProxy proxy, final CharSequence seq) {
        final String s1 = proxy.getName().toLowerCase();
        final String s2 = seq.toString().toLowerCase();
        return s1.contains(s2);
    }

    private MultiSelectionListener<PortrayableProxy> createSelectionListener() {
        return new MultiSelectionListener<PortrayableProxy>() {
            @Override
            public void onElementSelected(PortrayableProxy element) {
                result.remove(element);
                result.add(element);
                setPositiveEnabled(!result.isEmpty());
            }

            @Override
            public void onElementUnselected(PortrayableProxy element) {
                result.remove(element);
                setPositiveEnabled(!result.isEmpty());
            }
        };
    }

    private void setPositiveEnabled(boolean enabled) {
        int colorId = enabled ? R.color.white : R.color.lighter_gray;

        positiveButton.setTextColor(getColor(colorId));
        positiveButton.setEnabled(enabled);
    }

    private int getColor(@ColorRes int color) {
        return ofNullable(getContext())
                .map(context -> context.getColor(color))
                .orElse(0xFFFFFFFF);
    }

    private void setupUIForMode() {
        final Bundle args = getArguments();

        final String title = getOrDefaultById(args, ARG_TITLE, R.string.dialog_title);
        final String positiveText = getOrDefaultById(args, ARG_POSITIVE, R.string.confirm);
        final String negativeText = getOrDefaultById(args, ARG_NEGATIVE, R.string.cancel);

        showTitle.setText(title);
        showMessage.setText(message);
        positiveButton.setText(positiveText);
        negativeButton.setText(negativeText);
        hasAddButton = getOrDefault(args, ARG_HAS_ADD_BUTTON, false);
        listContext = new ListContext(data, list, search::getQuery);

        setupSearch();
        setupButtons();

        if (message.trim().isEmpty()) {
            showMessage.setVisibility(View.GONE);
            root.findViewById(R.id.message_divider).setVisibility(View.GONE);
        }
        root.invalidate();
    }

    private String getOrDefaultById(final Bundle args,
                                    final String argName,
                                    @StringRes final int defaultId) {
        return getOrDefault(args, argName, getString(defaultId));
    }

    private void setupSearch() {
        if (mode == Mode.CONFIRMATION)
            search.setVisibility(View.GONE);
        else {
            search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextChange(final String constraint) {
                    return onQueryTextSubmit(constraint);
                }

                @Override
                public boolean onQueryTextSubmit(final String constraint) {
                    ((Filterable) adapter).getFilter().filter(constraint);
                    return true;
                }
            });
        }
    }

    private void setupButtons() {
        positiveButton.setOnClickListener(v -> {
            positiveListener.accept(result);
            dismiss();
        });
        negativeButton.setOnClickListener(v -> {
            negativeListener.run();
            dismiss();
        });

        setPositiveEnabled(mode == Mode.CONFIRMATION);

        if (hasAddButton)
            addBtn.setOnClickListener(v -> addListener.accept(this, listContext));
        else {
            addBtn.setVisibility(View.GONE);
        }
    }

    @Override
    public final void onActivityResult(final int requestCode,
                                       final int resultCode,
                                       @Nullable final Intent data) {
        if(resultHandlingPlugin.canProcess(requestCode, resultCode)) {
            resultHandlingPlugin.onActivityResult(requestCode, resultCode, data);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public static Builder builder(final FragmentActivity context) {
        return new Builder(context);
    }

    public static class Builder {
        private final FragmentActivity context;
        private Mode mode;
        private String title;
        private String message;
        private String positiveText;
        private String negativeText;
        private boolean hasAddButton;
        private List<? extends Portrayable> data;
        private Consumer<List<PortrayableProxy>> positiveListener;
        private Runnable negativeListener;
        private BiConsumer<ResultHandling, ListContext> addListener;

        public Builder(@NonNull FragmentActivity context) {
            this.context = context;
            mode = Mode.SINGLE;
            title = context.getString(R.string.dialog_title);
            message = "";
            positiveText = context.getString(R.string.Ok);
            negativeText = context.getString(R.string.cancel);
            hasAddButton = false;
            data = new ArrayList<>();
            positiveListener = li -> {
            };
            negativeListener = () -> {
            };
        }

        public Builder setMode(Mode mode) {
            this.mode = mode;
            return this;
        }

        public Builder setTitle(@StringRes int id) {
            return setTitle(context.getString(id));
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setMessage(@StringRes final int id) {
            return setMessage(context.getString(id));
        }

        Builder setMessage(@StringRes final int id, Object... objects) {
            return setMessage(context.getString(id, objects));
        }

        Builder setMessage(final String message) {
            this.message = message;
            return this;
        }

        public <X extends Portrayable> Builder setData(final List<X> data) {
            this.data = data;
            return this;
        }

        public Builder setPositive(@StringRes int text, Consumer<List<PortrayableProxy>> listener) {
            return setPositive(context.getString(text), listener);
        }

        Builder setPositive(String text, Consumer<List<PortrayableProxy>> listener) {
            this.positiveText = text;
            this.positiveListener = listener;
            return this;
        }

        public Builder setNegative(@StringRes int text, Runnable listener) {
            return setNegative(context.getString(text), listener);
        }

        Builder setNegative(String text, Runnable listener) {
            this.negativeText = text;
            this.negativeListener = listener;
            return this;
        }

        public Builder hasAddButton(final boolean hasAddButton) {
            this.hasAddButton = hasAddButton;
            return this;
        }

        public Builder setAddListener(BiConsumer<ResultHandling, ListContext> listener) {
            addListener = listener;
            return this;
        }

        public void show() {
            final PortrayableListDialog dialog = create(mode,
                    title,
                    message,
                    data,
                    positiveText,
                    negativeText,
                    hasAddButton
            );

            dialog.positiveListener = positiveListener;
            dialog.negativeListener = negativeListener;
            dialog.addListener = addListener;

            final FragmentManager fm = context.getSupportFragmentManager();
            dialog.show(fm, "list_dialog_" + mode);
        }

        private static PortrayableListDialog create(final Mode mode,
                                                    final String title,
                                                    final String message,
                                                    final List<? extends Portrayable> data,
                                                    final String positive,
                                                    final String negative,
                                                    final boolean hasAddButton) {
            final PortrayableListDialog frag = new PortrayableListDialog();
            final Bundle args = new Bundle();
            args.putInt(ARG_MODE, mode.ordinal());
            args.putString(ARG_TITLE, title);
            args.putString(ARG_MESSAGE, message);
            args.putParcelableArrayList(ARG_DATA, new ArrayList<>(data));
            args.putString(ARG_POSITIVE, positive);
            args.putString(ARG_NEGATIVE, negative);
            args.putBoolean(ARG_HAS_ADD_BUTTON, hasAddButton);
            frag.setArguments(args);
            return frag;
        }
    }

    public static class ListContext {
        private final List<PortrayableProxy> data;
        private final Adapter<?> adapter;
        private final Supplier<CharSequence> constraintGetter;

        ListContext(final List<PortrayableProxy> data,
                    final RecyclerView recyclerView,
                    final Supplier<CharSequence> constraintGetter) {
            this.data = data;
            this.adapter = recyclerView.getAdapter();
            this.constraintGetter = constraintGetter;
        }

        public void addElement(final PortrayableProxy object) {
            addElement(data.size(), object);
        }

        void addElement(int position, final PortrayableProxy object) {
            data.add(position, object);
            if (adapter instanceof SelectionAdapter) {
                ((SelectionAdapter<PortrayableProxy>) adapter).addElement(position, object);
            }

            if (adapter instanceof DirectFilterable) {
                ((DirectFilterable) adapter).filter(constraintGetter.get());
            }
        }
    }
}