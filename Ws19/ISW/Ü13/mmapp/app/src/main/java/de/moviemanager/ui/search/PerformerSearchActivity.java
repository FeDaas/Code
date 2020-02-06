package de.moviemanager.ui.search;

import java.util.List;

import de.moviemanager.data.Performer;
import de.moviemanager.ui.detail.PerformerDetailActivity;


public class PerformerSearchActivity extends PortrayableSearchActivity<Performer> {

    public PerformerSearchActivity() {
        super();
    }

    @Override
    protected List<Performer> getListFromStorage() {
        return STORAGE.getPerformers();
    }

    @Override
    protected void showFrom(Performer elem) {
        PerformerDetailActivity.showAndNotifyIfOk(this, elem, data -> updateAfterEdit());
    }
}
