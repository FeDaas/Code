package de.moviemanager.ui.masterlist.sticky;

import androidx.annotation.LayoutRes;
import android.view.View;

public interface HeaderListener {
    int getHeaderPositionForItemAt(int itemPosition);
    @LayoutRes int getHeaderLayout(int headerPosition);
    void bindStickyHeaderData(View header, int headerPosition);
}
