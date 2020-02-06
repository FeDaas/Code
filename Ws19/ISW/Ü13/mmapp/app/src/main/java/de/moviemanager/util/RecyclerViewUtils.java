package de.moviemanager.util;

import android.content.Context;
import android.graphics.Canvas;

import androidx.annotation.DrawableRes;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import java.util.function.Consumer;

import de.moviemanager.ui.masterlist.swipe.SwipeController;

public enum  RecyclerViewUtils {
    ;

    public static void addSwipeSupport(Context context,
                                       RecyclerView view,
                                       @DrawableRes int icon,
                                       Consumer<ViewHolder> action) {
        SwipeController swipeController = new SwipeController(context,
                icon,
                action);
        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeController);
        itemTouchhelper.attachToRecyclerView(view);
        view.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
                swipeController.onDraw(c);
            }
        });
    }

    public static void setLinearLayoutTo(final Context context, final RecyclerView recyclerView) {
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
    }
}
