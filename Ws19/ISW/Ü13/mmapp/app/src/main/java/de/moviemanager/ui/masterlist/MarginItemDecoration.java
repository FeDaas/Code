package de.moviemanager.ui.masterlist;

import android.content.Context;
import android.graphics.Rect;
import android.view.View;

import androidx.annotation.DimenRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MarginItemDecoration extends RecyclerView.ItemDecoration {
    private final int padding;

    public MarginItemDecoration(Context context, @DimenRes int dimension) {
        this(context.getResources().getDimension(dimension));
    }

    public MarginItemDecoration(float spaceHeight) {
        this.padding = (int) spaceHeight;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect,
                               @NonNull View view,
                               @NonNull RecyclerView parent,
                               @NonNull RecyclerView.State state) {
        if(shouldNotApplyTo(view)) {
            return;
        }

        if (parent.getChildAdapterPosition(view) == 0) {
            outRect.top = padding;
        }
        outRect.left = padding;
        outRect.right = padding;
        outRect.bottom = padding;

    }

    private boolean shouldNotApplyTo(View view) {
        return view == null;
    }
}
