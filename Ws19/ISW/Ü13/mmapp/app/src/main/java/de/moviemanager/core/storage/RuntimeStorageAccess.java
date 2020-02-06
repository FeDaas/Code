package de.moviemanager.core.storage;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import java.io.File;
import java.util.List;
import java.util.Optional;

import de.moviemanager.data.ImagePyramid;
import de.moviemanager.data.Movie;
import de.moviemanager.data.Performer;
import de.moviemanager.data.Portrayable;
import de.moviemanager.ui.MasterActivity;
import de.storage.StorageException;
import de.util.Pair;
import de.util.operationflow.ReversibleTransaction;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.os.Environment.DIRECTORY_DOCUMENTS;
import static android.os.Environment.getExternalStoragePublicDirectory;
import static androidx.core.content.ContextCompat.checkSelfPermission;

public final class RuntimeStorageAccess implements RuntimeStorageConcept {
    private static final RuntimeStorageAccess INSTANCE = new RuntimeStorageAccess();

    private boolean permissionsGranted = false;
    private RuntimeStorage storage;

    public static RuntimeStorageAccess getInstance() {
        return INSTANCE;
    }

    private RuntimeStorageAccess() {

    }

    public boolean areRequiredPermissionsGrantedFor(final Activity activity) {
        updateRequiredPermissions(activity);
        return permissionsGranted;
    }

    public void updateRequiredPermissions(final Activity activity) {
        permissionsGranted = false;
        for(String permission : getRequiredPermissions()) {
            if (checkSelfPermission(activity, permission) != PERMISSION_GRANTED) {
                return;
            }
        }

        permissionsGranted = true;
    }

    public String[] getRequiredPermissions() {
        return new String[]{WRITE_EXTERNAL_STORAGE};
    }

    public void openMovieManagerStorage() {
        if(!permissionsGranted) {
            throw new StorageException("Permission for storage was not granted!");
        }
        File base = getExternalStoragePublicDirectory(DIRECTORY_DOCUMENTS);
        File directory = new File(base, MasterActivity.STORAGE_NAME);

        if(!isStorageOpened()) {
            openStorageIn(directory);
        }
    }

    private void openStorageIn(final File directory) {
        if(isStorageOpened()) {
            throw new StorageException("Storage was already opened!");
        }

        if (!directory.getParentFile().exists()) {
            directory.getParentFile().mkdirs();
        }
        storage = RuntimeStorage.getInstance(directory);
    }

    private boolean isStorageOpened() {
        return storage != null;
    }

    private RuntimeStorage getStorage() {
        if(!isStorageOpened())
            throw new StorageException("Storage is not open!");

        return storage;
    }

    @Override
    public ReversibleTransaction<Movie> newMovie() {
        return getStorage().newMovie();
    }

    @Override
    public ReversibleTransaction<Performer> newPerformer(Movie movie) {
        return getStorage().newPerformer(movie);
    }

    @Override
    public ReversibleTransaction<Movie> updateMovie(Movie movie) {
        return getStorage().updateMovie(movie);
    }

    @Override
    public ReversibleTransaction<Movie> updateMovie(int id) {
        return getStorage().updateMovie(id);
    }

    @Override
    public ReversibleTransaction<Performer> updatePerformer(int id) {
        return getStorage().updatePerformer(id);
    }

    @Override
    public ReversibleTransaction<Performer> updatePerformer(Performer performer) {
        return getStorage().updatePerformer(performer);
    }

    @Override
    public ReversibleTransaction<Movie> removeMovie(Movie movie) {
        return getStorage().removeMovie(movie);
    }

    @Override
    public ReversibleTransaction<Performer> removePerformer(Performer performer) {
        return getStorage().removePerformer(performer);
    }

    @Override
    public void link(Movie movie, Performer performer) {
        getStorage().link(movie, performer);
    }

    @Override
    public void unlink(Movie movie, Performer performer) {
        getStorage().unlink(movie, performer);
    }

    @Override
    public boolean isLinked(Movie movie, Performer performer) {
        return getStorage().isLinked(movie, performer);
    }

    @Override
    public List<Movie> getMovies() {
        return getStorage().getMovies();
    }

    @Override
    public List<Performer> getPerformers() {
        return getStorage().getPerformers();
    }

    @Override
    public List<Movie> getLinkedMoviesOfPerformer(Performer performer) {
        return getStorage().getLinkedMoviesOfPerformer(performer);
    }

    @Override
    public List<Performer> getLinkedPerformersOfMovie(Movie movie) {
        return getStorage().getLinkedPerformersOfMovie(movie);
    }

    @Override
    public Optional<Movie> getMovieById(int id) {
        return getStorage().getMovieById(id);
    }

    @Override
    public Optional<Performer> getPerformerById(int id) {
        return getStorage().getPerformerById(id);
    }

    @Override
    public void setImageForPortrayable(Portrayable portrayable, Bitmap image) {
        getStorage().setImageForPortrayable(portrayable, image);
    }

    @Override
    public Pair<Drawable, Boolean> getImage(Context context, Portrayable portrayal, ImagePyramid.ImageSize size) {
        return getStorage().getImage(context, portrayal, size);
    }

    @Override
    public Drawable getDefaultImage(Context context, ImagePyramid.ImageSize size) {
        return getStorage().getDefaultImage(context, size);
    }

    @Override
    public void selfDestruct() {
        getStorage().selfDestruct();
    }

    @Override
    public void clear() {
        getStorage().clear();
    }
}
