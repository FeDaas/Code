package de.moviemanager.ui.detail;

import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.widget.TextView;

import static android.view.MotionEvent.ACTION_MASK;
import static android.view.MotionEvent.ACTION_UP;

public class DeepScrollTouchListener implements View.OnTouchListener {
    private static final int MOVE_UP = 1;
    private static final int MOVE_DOWN = -1;

    private final TextView textView;
    private float initY;

    public DeepScrollTouchListener(final TextView textView) {
        this.textView = textView;
    }

    @Override
    public boolean onTouch(final View v, final MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            initY = event.getY();
        } else {
            int direction = estimateDirection(event);

            final ViewParent parent = v.getParent();
            parent.requestDisallowInterceptTouchEvent(true);

            if (hasReachedVerticalLimit(direction)) {
                parent.requestDisallowInterceptTouchEvent(false);
            }

            if (isActionUp(event)) {
                parent.requestDisallowInterceptTouchEvent(false);
                v.performClick();
            }
        }
        return false;
    }

    private int estimateDirection(final MotionEvent event) {
        int direction = 0;
        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            float deltaY = initY - event.getY();
            initY = event.getY();
            if (deltaY < 0) {
                direction = MOVE_DOWN;
            } else {
                direction = MOVE_UP;
            }
        }

        return direction;
    }

    private boolean hasReachedVerticalLimit(final int direction) {
        final int maxScroll = calculateMaxScroll();

        return (textView.getScrollY() == 0 && direction == MOVE_DOWN)
                || (textView.getScrollY() == maxScroll && direction == MOVE_UP);
    }

    private int calculateMaxScroll() {
        int maxScroll = textView.getLineHeight() * (textView.getLayout().getLineCount() - textView.getMaxLines()) + 2;
        if (maxScroll < 0)
            maxScroll = 0;

        return maxScroll;
    }

    private boolean isActionUp(final MotionEvent event) {
        return (event.getAction() & ACTION_MASK) == ACTION_UP;
    }
}
