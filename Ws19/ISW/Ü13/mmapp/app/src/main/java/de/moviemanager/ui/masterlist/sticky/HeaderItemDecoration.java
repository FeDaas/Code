package de.moviemanager.ui.masterlist.sticky;

import android.graphics.Canvas;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ItemDecoration;
import androidx.recyclerview.widget.RecyclerView.State;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import java.util.function.Predicate;

import de.moviemanager.ui.masterlist.viewholder.TypedViewHolder;
import de.util.Pair;

import static android.view.View.MeasureSpec.EXACTLY;
import static android.view.View.MeasureSpec.UNSPECIFIED;
import static android.view.View.MeasureSpec.makeMeasureSpec;
import static android.view.ViewGroup.getChildMeasureSpec;
import static de.util.Pair.paired;
import static java.util.stream.IntStream.range;

public class HeaderItemDecoration extends ItemDecoration {
    private final HeaderListener listener;
    private int stickyHeaderHeight;

    public HeaderItemDecoration(@NonNull HeaderListener listener) {
        this.listener = listener;
    }

    @Override
    public void onDrawOver(final Canvas canvas,
                           final RecyclerView parent,
                           final State state) {
        super.onDrawOver(canvas, parent, state);
        View topChild = parent.getChildAt(0);
        if (topChild == null)
            return;

        int topChildPosition = parent.getChildAdapterPosition(topChild);
        if (topChildPosition == RecyclerView.NO_POSITION)
            return;

        int headerPos = listener.getHeaderPositionForItemAt(topChildPosition);
        View currentHeader = getHeaderViewForItem(headerPos, parent);
        fixLayoutSize(parent, currentHeader);
        int contactPoint = currentHeader.getBottom();

        View childInContact = getChildInContact(parent, contactPoint, headerPos);
        drawHeader(canvas, currentHeader, childInContact);
    }

    private View getHeaderViewForItem(int headerPosition,
                                      final RecyclerView parent) {
        int layoutResId = listener.getHeaderLayout(headerPosition);
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View header = inflater.inflate(layoutResId, parent, false);
        listener.bindStickyHeaderData(header, headerPosition);
        return header;
    }

    private void fixLayoutSize(final ViewGroup parent,
                               final View view) {
        int childWidthSpec = calculateHorizontalChildMeasureSpecifications(parent, view);
        int childHeightSpec = calculateVerticalChildMeasureSpecifications(parent, view);

        view.measure(childWidthSpec, childHeightSpec);
        stickyHeaderHeight = view.getMeasuredHeight();
        view.layout(0, 0, view.getMeasuredWidth(), stickyHeaderHeight);
    }

    private int calculateHorizontalChildMeasureSpecifications(final ViewGroup parent,
                                                              final View view) {
        int parentWidth = makeMeasureSpec(parent.getWidth(), EXACTLY);
        int startPadding = parent.getPaddingStart();
        int endPadding = parent.getPaddingEnd();
        int childWidth = view.getLayoutParams().width;

        return getChildMeasureSpec(parentWidth, startPadding + endPadding, childWidth);
    }

    private int calculateVerticalChildMeasureSpecifications(final ViewGroup parent,
                                                            final View view) {
        int parentHeight = makeMeasureSpec(parent.getHeight(), UNSPECIFIED);
        int topPadding = parent.getPaddingTop();
        int botPadding = parent.getPaddingBottom();
        int childHeight = view.getLayoutParams().height;

        return getChildMeasureSpec(parentHeight, topPadding + botPadding, childHeight);
    }

    private View getChildInContact(final RecyclerView parent,
                                   int contactPoint,
                                   int currentHeaderPos) {
        final Predicate<Pair<View, Integer>> isInContact = p -> isChildInContact(p.first,
                contactPoint,
                currentHeaderPos,
                p.second);
        return range(0, parent.getChildCount())
                .mapToObj(i -> paired(parent.getChildAt(i), i))
                .filter(isInContact)
                .map(Pair::getFirst)
                .findFirst()
                .orElse(null);
    }

    private boolean isChildInContact(final View child,
                                     int contactPoint,
                                     int currentHeaderPos,
                                     int curPos) {
        int heightTolerance = calculateHeightTolerance(child, currentHeaderPos, curPos);
        int childBotPosition = child.getBottom() + (child.getTop() > 0 ? heightTolerance : 0);

        return child.getTop() <= contactPoint && contactPoint < childBotPosition;
    }

    private int calculateHeightTolerance(final View child,
                                         int curHeaderPos,
                                         int curPos) {
        if (isOtherHeader(child, curHeaderPos, curPos)) {
            return stickyHeaderHeight - child.getHeight();
        } else
            return 0;
    }

    private boolean isOtherHeader(final View child,
                                  int curHeaderPos,
                                  int curPos) {
        return curHeaderPos != curPos && isHeader(child);
    }

    private boolean isHeader(final View child) {
        final ViewHolder holder = (ViewHolder) child.getTag();
        final TypedViewHolder typed = (TypedViewHolder) holder;
        return typed.isHeader();
    }

    private void drawHeader(final Canvas canvas,
                            final View currentHeader,
                            final View childInContact) {
        if (childInContact != null && isHeader(childInContact)) {
            drawMovingHeader(canvas, currentHeader, childInContact);
        } else
            drawFixedHeader(canvas, currentHeader);
    }

    private void drawMovingHeader(final Canvas canvas,
                                  final View currentHeader,
                                  final View nextHeader) {
        canvas.save();
        float top = nextHeader.getTop();
        float headerHeight = currentHeader.getHeight();
        canvas.translate(0, top - headerHeight);
        currentHeader.draw(canvas);
        canvas.restore();
    }

    private void drawFixedHeader(final Canvas canvas,
                                 final View header) {
        canvas.save();
        canvas.translate(0, 0);
        header.draw(canvas);
        canvas.restore();
    }
}
