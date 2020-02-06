package de.moviemanager.util;

import android.graphics.Rect;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import de.moviemanager.ui.detail.DeepScrollTouchListener;

public enum  ScrollViewUtils {
    ;

    public static void scrollToViewIfNeeded(final ScrollView scrollView, final View view) {
        if(!isViewVisible(scrollView, view)) {
            if(isScrollViewAbove(scrollView, view))
                scrollView.post(() -> scrollView.scrollTo(0, view.getBottom()));
            else if(isScrollViewBelow(scrollView, view))
                scrollView.post(() -> scrollView.scrollTo(0, view.getTop()));
        }
    }

    private static boolean isViewVisible(final ScrollView scrollView, final View view) {
        final Rect scrollBounds = new Rect();
        scrollView.getDrawingRect(scrollBounds);

        float top = view.getY();
        float bottom = top + view.getHeight();

        return scrollBounds.top < top && bottom < scrollBounds.bottom;
    }

    public static boolean isScrollViewAbove(final ScrollView scrollView, final View view) {
        final Rect scrollBounds = new Rect();
        scrollView.getDrawingRect(scrollBounds);
        return scrollBounds.bottom <= view.getTop();
    }

    public static boolean isScrollViewBelow(final ScrollView scrollView, final View view) {
        final Rect scrollBounds = new Rect();
        scrollView.getDrawingRect(scrollBounds);
        return view.getBottom() <= scrollBounds.top;
    }

    public static void enableDeepScroll(final TextView textView) {
        textView.setMovementMethod(new ScrollingMovementMethod());
        textView.setOnTouchListener(new DeepScrollTouchListener(textView));
    }
}
