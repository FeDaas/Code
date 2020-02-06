package de.moviemanager.ui.search;

import android.app.Activity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;
import androidx.annotation.StringRes;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.Locale;

import de.moviemanager.R;

public class SearchInfo {
    private final Activity context;
    private final ConstraintLayout anchor;
    private final ImageView icon;
    private final TextView text;
    private final LinearLayout additionalInformation;

    @DrawableRes
    private final int infoIconId;
    @DrawableRes
    private final int unsuccessfulIconId;
    @StringRes
    private final int infoTextId;
    @StringRes
    private final int unsuccessfulTextId;

    public SearchInfo(final Activity context) {
        this.context = context;
        anchor = context.findViewById(R.id.search_info);
        icon = anchor.findViewById(R.id.search_info_icon);
        text = anchor.findViewById(R.id.search_info_text);
        additionalInformation = anchor.findViewById(R.id.additional_information);

        infoIconId = R.drawable.ic_search_gray;
        unsuccessfulIconId = R.drawable.ic_search_unsuccessful;
        infoTextId = R.string.search_info;
        unsuccessfulTextId = R.string.search_unsuccessful;
    }

    public void show(final String query, int resultLength) {
        anchor.setVisibility(View.VISIBLE);
        if (query.isEmpty()) {
            icon.setImageResource(infoIconId);
            text.setText(infoTextId);
            showAdditionalInformation(true);
        } else if (resultLength == 0) {
            icon.setImageResource(unsuccessfulIconId);
            final String info = String.format(Locale.US,
                    context.getString(unsuccessfulTextId),
                    query
            );
            text.setText(info);
            showAdditionalInformation(false);
        } else {
            anchor.setVisibility(View.INVISIBLE);
        }
    }

    private void showAdditionalInformation(boolean shouldShow) {
        if (additionalInformation != null) {
            if (shouldShow) {
                additionalInformation.setVisibility(View.VISIBLE);
            } else {
                additionalInformation.setVisibility(View.INVISIBLE);
            }
        }
    }

    public void addOnClickActionTo(@IdRes int id, Runnable action) {
        View view = anchor.findViewById(id);
        if (view != null) {
            view.setOnClickListener(v -> action.run());
        }
    }
}
