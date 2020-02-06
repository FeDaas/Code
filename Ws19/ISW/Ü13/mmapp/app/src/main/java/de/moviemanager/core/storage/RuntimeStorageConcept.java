package de.moviemanager.core.storage;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import java.util.List;
import java.util.Optional;

import de.moviemanager.data.ImagePyramid.ImageSize;
import de.moviemanager.data.Movie;
import de.moviemanager.data.Performer;
import de.moviemanager.data.Portrayable;
import de.util.Pair;
import de.util.operationflow.ReversibleTransaction;

public interface RuntimeStorageConcept {
    ReversibleTransaction<Movie> newMovie();
    ReversibleTransaction<Performer> newPerformer(Movie movie);

    ReversibleTransaction<Movie> updateMovie(Movie movie);
    ReversibleTransaction<Movie> updateMovie(int id);

    ReversibleTransaction<Performer> updatePerformer(int id);
    ReversibleTransaction<Performer> updatePerformer(Performer performer);

    ReversibleTransaction<Movie> removeMovie(Movie movie);
    ReversibleTransaction<Performer> removePerformer(Performer performer);

    void link(Movie movie, Performer performer);
    void unlink(Movie movie, Performer performer);
    boolean isLinked(Movie movie, Performer performer);

    List<Movie> getMovies();
    List<Performer> getPerformers();

    List<Movie> getLinkedMoviesOfPerformer(Performer performer);
    List<Performer> getLinkedPerformersOfMovie(Movie movie);

    Optional<Movie> getMovieById(int id);
    Optional<Performer> getPerformerById(int id);

    void setImageForPortrayable(Portrayable portrayable, Bitmap image);
    Pair<Drawable, Boolean> getImage(Context context, Portrayable portrayal, ImageSize size);
    Drawable getDefaultImage(Context context, ImageSize size);

    void clear();
    void selfDestruct();
}
