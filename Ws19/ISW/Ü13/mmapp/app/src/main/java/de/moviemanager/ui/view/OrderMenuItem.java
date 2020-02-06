package de.moviemanager.ui.view;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import de.moviemanager.R;
import de.moviemanager.ui.masterlist.OrderState;

public class OrderMenuItem {
    private final String title;
    private int orderRes;

    public OrderMenuItem(final String title, final OrderState state) {
        this.title = title;
        setState(state);
    }

    public void setState(final OrderState state) {
        if (state == OrderState.DESCENDING) {
            this.orderRes = R.drawable.ic_master_order_desc;
        } else if(state == OrderState.ASCENDING){
            this.orderRes = R.drawable.ic_master_order_asc;
        } else {
            this.orderRes = R.drawable.ic_master_order_neutral;
        }
    }

    public void updateView(View root) {
        final TextView titleView = root.findViewById(R.id.title);
        final ImageView imageView = root.findViewById(R.id.order_state);

        titleView.setText(title);
        imageView.setImageResource(orderRes);
    }
}
