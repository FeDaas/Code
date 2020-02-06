package de.moviemanager.ui.masterfragments;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListPopupWindow;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

import de.moviemanager.R;
import de.moviemanager.android.ResultHandlingFragment;
import de.moviemanager.core.storage.RuntimeStorageAccess;
import de.moviemanager.data.Movie;
import de.moviemanager.data.Performer;
import de.moviemanager.data.Portrayable;
import de.moviemanager.ui.MasterActivity;
import de.moviemanager.ui.adapter.OrderWindowAdapter;
import de.moviemanager.ui.adapter.PortrayableRVAdapter;
import de.moviemanager.ui.masterlist.OrderState;
import de.moviemanager.ui.masterlist.categorizer.OrderGroup;
import de.moviemanager.ui.view.OrderMenuItem;
import de.moviemanager.util.DimensionUtils;
import de.moviemanager.util.autobind.AutoBind;
import de.moviemanager.util.autobind.Bind;

import static androidx.core.content.ContextCompat.getDrawable;
import static de.moviemanager.ui.MasterActivity.FRAGMENT_NAME;
import static de.moviemanager.util.AndroidStringUtils.buildQueryPredicate;
import static de.moviemanager.util.Listeners.createOnTextChangedListener;
import static de.moviemanager.util.RecyclerViewUtils.setLinearLayoutTo;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;

public abstract class PortrayableMasterFragment<T extends Portrayable> extends ResultHandlingFragment {
    private static final String ORIGINAL_DATA = "original_data_arg";
    final BiPredicate<T, String> nameContainsInput = buildQueryPredicate(
            String::contains,
            Portrayable::name,
            String::toLowerCase
    );

    protected static final RuntimeStorageAccess STORAGE = RuntimeStorageAccess.getInstance();

    private int nameId;
    @Bind(R.id.portrayables) private RecyclerView list;
    @Bind(R.id.filter) protected EditText filter;
    @Bind(R.id.criterion_button) private ImageView orderButton;
    @Bind(R.id.add_button) private FloatingActionButton addButton;
    List<T> originalData;
    OrderGroup<T> orders;

    public static PortrayableMasterFragment<Movie> newMovieFragmentInstance(
            final @StringRes int name,
            final List<Movie> originalData) {
        final Bundle args = new Bundle();
        args.putParcelableArrayList(ORIGINAL_DATA, new ArrayList<>(originalData));
        args.putInt(FRAGMENT_NAME, name);

        final PortrayableMasterFragment<Movie> fragment = new MovieMasterFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static PortrayableMasterFragment<Performer> newPerformerFragmentInstance(
            final @StringRes int name,
            final List<Performer> originalData) {
        final Bundle args = new Bundle();
        args.putParcelableArrayList(ORIGINAL_DATA, new ArrayList<>(originalData));
        args.putInt(FRAGMENT_NAME, name);

        final PortrayableMasterFragment<Performer> fragment = new PerformerMasterFragment();
        fragment.setArguments(args);
        return fragment;
    }



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        STORAGE.updateRequiredPermissions(getActivity());
        STORAGE.openMovieManagerStorage();
        retrieveArguments();

        return inflater.inflate(R.layout.fragment_master, container, false);
    }

    private void retrieveArguments() {
        originalData = ofNullable(getArguments())
                .map(b -> b.getParcelableArrayList(ORIGINAL_DATA))
                .orElse(new ArrayList<>())
                .stream()
                .map(obj -> (T) obj)
                .collect(toList());
        nameId = ofNullable(getArguments())
                .map(b -> b.getInt(FRAGMENT_NAME))
                .orElse(R.string.default_fragment_name);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        AutoBind.bindAll(this, view);

        createOrders();
        setupRecyclerView();

        orderButton.setOnClickListener(this::openOrderPopupMenu);
        filter.addTextChangedListener(createOnTextChangedListener(getAdapter().getFilter()::filter));
        addButton.setOnClickListener(v -> getAdapter().createObject());
    }

    @Override
    public void onResume() {
        super.onResume();
        final Activity activity = getActivity();
        if (activity != null) {
            MasterActivity master = (MasterActivity) activity;
            master.setTitle(nameId);
            master.setBottomNavigationTo(nameId);
        }
    }

    protected abstract void createOrders();

    private void openOrderPopupMenu(final View anchor) {
        final List<OrderMenuItem> items = stream(orders.spliterator(), false)
                .map(order -> new OrderMenuItem(order.getName(), order.getState()))
                .collect(toList());

        items.get(orders.getSelectionIndex()).setState(orders.getState());

        final OrderWindowAdapter adapter = new OrderWindowAdapter(getContext(), items);
        final Context context = getContext();
        if(context != null) {
            final ListPopupWindow popupMenu = new ListPopupWindow(context);
            popupMenu.setInputMethodMode(ListPopupWindow.INPUT_METHOD_NOT_NEEDED);
            popupMenu.setAnchorView(anchor);
            popupMenu.setWidth((int) DimensionUtils.dpToPixels(getContext(), 168));
            popupMenu.setAdapter(adapter);
            popupMenu.setOnItemClickListener((parent, view, pos, id) -> {
                popupMenu.dismiss();
                selectNewOrder(pos, items);
                adapter.notifyDataSetChanged();
            });
            popupMenu.show();
        }
    }

    private void selectNewOrder(int index, final List<OrderMenuItem> items) {
        items.forEach(i -> i.setState(OrderState.NEUTRAL));

        getAdapter().selectOrder(index, filter.getText().toString());

        int id;

        if (orders.isDescending()) {
            id = R.drawable.ic_master_order_desc;
        } else {
            id = R.drawable.ic_master_order_asc;
        }

        items.get(index).setState(orders.getState());

        final Activity activity = getActivity();
        if(activity != null) {
            final Drawable drawable = getDrawable(activity, id);
            orderButton.setImageDrawable(drawable);
        }
    }

    protected abstract PortrayableRVAdapter<T> getAdapter();

    private void setupRecyclerView() {
        list.setVisibility(View.VISIBLE);
        list.setHasFixedSize(true);
        setLinearLayoutTo(getContext(), list);
        list.setAdapter(createAdapter());
    }

    protected abstract PortrayableRVAdapter<T> createAdapter();
}




