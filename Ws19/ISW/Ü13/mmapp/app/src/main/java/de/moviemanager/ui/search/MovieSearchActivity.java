package de.moviemanager.ui.search;

import java.util.List;

import de.moviemanager.data.Movie;
import de.moviemanager.ui.detail.MovieDetailActivity;

public class MovieSearchActivity extends PortrayableSearchActivity<Movie> {

    public MovieSearchActivity() {
        super();
    }

    @Override
    protected List<Movie> getListFromStorage() {
        return STORAGE.getMovies();
    }

    @Override
    protected void showFrom(final Movie elem) {
        MovieDetailActivity.showAndNotifyIfOk(this, elem, data -> updateAfterEdit());
    }
}
