package de.moviemanager.ui.masterlist.viewholder;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import de.moviemanager.R;

import static de.moviemanager.ui.masterlist.elements.Type.HEADER;

public class HeaderViewHolder extends TypedViewHolder {
    private final TextView categoryView;

    public HeaderViewHolder(@NonNull View itemView) {
        super(itemView, HEADER);
        categoryView = itemView.findViewById(R.id.categoryView);
    }

    public void setCategoryText(String categoryText) {
        this.categoryView.setText(categoryText);
    }
}
