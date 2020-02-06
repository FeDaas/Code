package de.moviemanager.ui.detail.modifiable;

import android.widget.ScrollView;

import androidx.annotation.IdRes;

import de.moviemanager.ui.detail.modifications.Modification;
import de.moviemanager.ui.view.CustomRatingBar;
import de.moviemanager.util.ScrollViewUtils;


public class RatingAttribute <X> extends ModifiableAttribute <X, Double> {
    private final int id;
    private final ScrollView root;
    private CustomRatingBar edit;

    public RatingAttribute(ModifiableAppCompatActivity modContext, ScrollView root, @IdRes int id) {
        super(modContext);
        this.id = id;
        this.root = root;
    }

    @Override
    public void bindViews() {
        edit = getContext().findViewById(id);
    }

    @Override
    public void bindListeners() {
        edit.setOnRatingChanged(rating -> {
            getContext().hideKeyboard();
            getContext().addModification(new Modification<>(edit.getRating(), this::undo));
        });
    }

    private void undo(double rating) {
        edit.setRating(rating);
        ScrollViewUtils.scrollToViewIfNeeded(root, edit);
    }

    @Override
    protected void setContent(Double content) {
        edit.setRating(content.floatValue());
    }

    @Override
    public Double getContent() {
        return edit.getRating();
    }
}
