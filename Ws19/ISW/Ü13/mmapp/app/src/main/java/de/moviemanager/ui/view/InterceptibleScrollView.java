package de.moviemanager.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ScrollView;

import java.util.function.BiConsumer;

public class InterceptibleScrollView extends ScrollView {
    private BiConsumer<View, MotionEvent> dispatchListener;

    public InterceptibleScrollView(final Context context) {
        super(context);
    }

    public InterceptibleScrollView(final Context context,
                                     final AttributeSet attrs) {
        super(context, attrs);
    }

    public InterceptibleScrollView(final Context context,
                                     final AttributeSet attrs,
                                     final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public InterceptibleScrollView(final Context context,
                                     final AttributeSet attrs,
                                     final int defStyleAttr,
                                     final int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setOnDispatchListener(final Runnable dispatchListener) {
        setOnDispatchListener((view, ev) -> dispatchListener.run());
    }

    private void setOnDispatchListener(final BiConsumer<View, MotionEvent> listener) {
        dispatchListener = listener;
    }

    @Override
    public boolean dispatchTouchEvent(final MotionEvent ev) {
        if (dispatchListener != null) {
            dispatchListener.accept(this, ev);
        }
        return super.dispatchTouchEvent(ev);
    }
}
