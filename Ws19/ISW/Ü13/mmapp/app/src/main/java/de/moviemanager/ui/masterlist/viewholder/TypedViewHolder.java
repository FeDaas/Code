package de.moviemanager.ui.masterlist.viewholder;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import android.view.View;

import de.moviemanager.ui.masterlist.elements.Type;
import de.moviemanager.ui.masterlist.elements.Typified;
import de.moviemanager.ui.masterlist.elements.TypifiedBase;

public class TypedViewHolder extends ViewHolder implements Typified {
    private final TypifiedBase base;

    TypedViewHolder(@NonNull View itemView, Type type) {
        super(itemView);
        base = new TypifiedBase(type);
        itemView.setTag(this);
    }

    @Override
    public int getTypeAsInt() {
        return base.getTypeAsInt();
    }

    @Override
    public boolean isHeader() {
        return base.isHeader();
    }

    @Override
    public boolean isContent() {
        return base.isContent();
    }

    @Override
    public boolean isDivider() {
        return base.isDivider();
    }

    @Override
    public boolean hasSameTypeAs(Typified e) {
        return base.hasSameTypeAs(e);
    }

    @Override
    public String getTypeAsString() {
        return base.getTypeAsString();
    }
}
