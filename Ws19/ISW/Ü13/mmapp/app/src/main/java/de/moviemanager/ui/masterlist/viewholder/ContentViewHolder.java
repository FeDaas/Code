package de.moviemanager.ui.masterlist.viewholder;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.Date;

import de.moviemanager.R;
import de.util.DateUtils;

import static de.moviemanager.ui.masterlist.elements.Type.CONTENT;

public class ContentViewHolder extends TypedViewHolder {

    private final ImageView imageView;
    private final TextView titleView;
    private final TextView metadataView;
    private final ImageView rentedBanner;

    public ContentViewHolder(@NonNull View itemView) {
        super(itemView, CONTENT);
        imageView = itemView.findViewById(R.id.content_image);
        titleView = itemView.findViewById(R.id.content_title);
        metadataView = itemView.findViewById(R.id.content_subtitle);
        rentedBanner = itemView.findViewById(R.id.dueStatusIcon);
    }

    public void setImage(Drawable d) {
        imageView.setImageDrawable(d);
    }

    public void setTitle(String title) {
        titleView.setText(title);
    }

    public void setMetaText(String metaText) {
        this.metadataView.setText(metaText);
    }

    // Subject to change, just a quick hack for testing purposes
    public void setRented(Date dueDate) {
        Date today = DateUtils.nowAtMidnight();
        if(dueDate == null) {
            rentedBanner.setVisibility(View.INVISIBLE);
        } else {
            String dueString = this.metadataView.getText().toString();
            if(!(this.metadataView.getText().equals(DateUtils.dateToText(dueDate)))) {
                dueString +="\t" + DateUtils.dateToText(dueDate);
            }
            this.metadataView.setText(dueString);
            if(dueDate.compareTo(today) > 0) {
                rentedBanner.setImageResource(R.drawable.ic_due_banner_small);
            } else {
                rentedBanner.setImageResource(R.drawable.ic_overdue_banner_small);
            }
            rentedBanner.setVisibility(View.VISIBLE);
        }
    }

}
