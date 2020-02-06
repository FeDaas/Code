package de.moviemanager.ui.detail.modifiable;

import android.widget.ScrollView;

import androidx.annotation.IdRes;

import java.util.Date;

import de.moviemanager.ui.detail.modifications.Modification;
import de.moviemanager.ui.view.DateSelectionView;
import de.moviemanager.util.ScrollViewUtils;

public class DateAttribute<X> extends ModifiableAttribute<X, Date> {
    private final int id;
    private final ScrollView root;
    private DateSelectionView editDate;

    public DateAttribute(ModifiableAppCompatActivity modContext, ScrollView root, @IdRes int id) {
        super(modContext);
        this.root = root;
        this.id = id;
    }

    @Override
    public void bindViews() {
        editDate = getContext().findViewById(id);
    }

    @Override
    public void bindListeners() {
        editDate.setDateChangeListener(oldDate -> getContext()
                .addModification(new Modification<>(oldDate, old -> {
                    ScrollViewUtils.scrollToViewIfNeeded(root, editDate);
                    editDate.setDate(old);
                }))
        );
    }

    @Override
    protected void setContent(Date content) {
        editDate.setDate(content);
    }

    @Override
    public Date getContent() {
        return editDate.getDate();
    }

    public DateSelectionView accessDateSelectionView() {
        return this.editDate;
    }
}
