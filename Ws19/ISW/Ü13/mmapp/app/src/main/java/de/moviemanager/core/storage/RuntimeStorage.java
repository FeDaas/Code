package de.moviemanager.core.storage;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.IntFunction;

import de.associations.BidirectionalAssociationSet;
import de.associations.RuleViolationCallbacks;
import de.moviemanager.R;
import de.moviemanager.core.json.ImagePyramidFromJsonObject;
import de.moviemanager.core.json.MovieFromJsonObject;
import de.moviemanager.core.json.PerformerFromJsonObject;
import de.moviemanager.data.ImagePyramid;
import de.moviemanager.data.ImagePyramid.ImageSize;
import de.moviemanager.data.Movie;
import de.moviemanager.data.Performer;
import de.moviemanager.data.Portrayable;
import de.storage.Register;
import de.storage.Storage;
import de.storage.StorageException;
import de.util.Identifiable;
import de.util.Pair;
import de.util.operationflow.ReversibleOperations.ReversibleTransformation;
import de.util.operationflow.ReversibleTransaction;
import de.util.operationflow.Transaction;

import static de.associations.BidirectionalAssociationSet.OverflowPolicy.THROW;
import static de.associations.BidirectionalAssociationSet.UnderflowPolicy.REMOVE_ASSOCIATION;
import static de.associations.BidirectionalAssociationSet.create;
import static de.util.Pair.paired;
import static de.util.operationflow.ReversibleOperations.reversibleTransformation;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

public class RuntimeStorage implements RuntimeStorageConcept {
    private static final Map<File, RuntimeStorage> INSTANCES = new HashMap<>();

    private final File home;
    private final String imagePath;
    private Storage physicalStorage;

    private Register<Movie> movies;
    private Register<Performer> performers;
    private Register<ImagePyramid> images;
    private BidirectionalAssociationSet<Movie, Performer> moviePerformerAssociations;


    public static RuntimeStorage getInstance(final File file) {
        INSTANCES.computeIfAbsent(file, RuntimeStorage::new);
        return INSTANCES.get(file);
    }

    private RuntimeStorage(final File home) {
        super();
        this.home = home;
        this.imagePath = this.home.getAbsoluteFile() + File.separator + "images";
        setup();
    }

    private void setup() {
        physicalStorage = Storage.openIn(home);
        setupAssociations();
        registerStorageGroups();
        setupRegister();
        updateAssociations();
    }

    private void registerStorageGroups() {
        physicalStorage.registerGroup(new JsonGroup<>(Movie.class, MovieFromJsonObject::new));
        physicalStorage.registerGroup(new JsonGroup<>(Performer.class, PerformerFromJsonObject::new));
        physicalStorage.registerGroup(new JsonGroup<>(ImagePyramid.class, ImagePyramidFromJsonObject::new));
        physicalStorage.registerGroup(createMoviePerformerGroup());
    }

    private void updateAssociations() {
        Class<BidirectionalAssociationSet> cls = BidirectionalAssociationSet.class;
        physicalStorage.getWrittenNames(moviePerformerAssociations.getClass())
                .forEach(name -> physicalStorage.read(cls, name));
        physicalStorage.write(moviePerformerAssociations);
    }

    private AssociationsGroup<Movie, Performer> createMoviePerformerGroup() {
        final AssociationsGroup<Movie, Performer> group = new AssociationsGroup<>(moviePerformerAssociations);
        group.setLeftMapper(Movie::id, this::tryGetMovieById);
        group.setRightMapper(Performer::id, this::tryGetPerformerById);
        return group;
    }

    private void setupRegister() {
        movies = setupRegister(Movie::new, loadExistingData(Movie.class));
        performers = setupRegister(Performer::new, loadExistingData(Performer.class));
        images = setupRegister(ImagePyramid::new, loadExistingData(ImagePyramid.class));
    }

    private <X> List<X> loadExistingData(final Class<X> cls) {
        return physicalStorage.getWrittenNames(cls)
                .stream()
                .map(name -> physicalStorage.read(cls, name))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toList());
    }

    private <X extends Identifiable> Register<X> setupRegister(final IntFunction<X> constuctor,
                                                               final List<X> data) {
        Register<X> register = new Register<>(constuctor, data);
        register.setStorageSave(physicalStorage::write);
        register.setStorageDelete(physicalStorage::delete);
        return register;
    }

    private void setupAssociations() {
        moviePerformerAssociations = create(Movie.class, Performer.class, "0..*", "1..*");
        moviePerformerAssociations.setPolicies(REMOVE_ASSOCIATION, THROW);
        RuleViolationCallbacks<Movie, Performer> callbacks = new RuleViolationCallbacks<>();
        callbacks.setBackwardUnderflowCallback((pair, policy) -> {
            Performer p = pair.first;
            performers.startRemovalTransactionFor(p).commit();
        });
        moviePerformerAssociations.setCallback(callbacks);
    }

    @Override
    public ReversibleTransaction<Movie> newMovie() {
        return movies.startCreationTransaction();
    }

    @Override
    public ReversibleTransaction<Performer> newPerformer(final Movie movie) {
        ReversibleTransaction<Performer> transaction = performers.startCreationTransaction();
        transaction.addOperation(new ReversibleTransformation<Performer>() {
            @Override
            public Performer forward(Performer obj) {
                link(movie, obj);
                return obj;
            }

            @Override
            public Performer backward(Performer obj) {
                unlink(movie, obj);
                return obj;
            }

            @NonNull
            @Override
            public String toString() {
                return "Initial link";
            }
        });
        return transaction;
    }

    @Override
    public ReversibleTransaction<Movie> updateMovie(final Movie movie) {
        return movies.startUpdateTransactionFor(movie);
    }

    @Override
    public ReversibleTransaction<Movie> updateMovie(final int id) {
        return updateMovie(tryGetMovieById(id));
    }

    private Movie tryGetMovieById(final int id) {
        return tryGetById(id, this::getMovieById, Movie.class);
    }

    private <A> A tryGetById(int id, final IntFunction<Optional<A>> getter, final Class<A> clazz) {
        final Optional<A> optional = getter.apply(id);
        if (optional.isPresent()) {
            return optional.get();
        } else {
            throw new StorageException(String.format("No %s with id=%d was found",
                    clazz.getSimpleName(),
                    id)
            );
        }
    }

    @Override
    public ReversibleTransaction<Performer> updatePerformer(final int id) {
        return updatePerformer(tryGetPerformerById(id));
    }

    private Performer tryGetPerformerById(final int id) {
        return tryGetById(id, this::getPerformerById, Performer.class);
    }

    @Override
    public ReversibleTransaction<Performer> updatePerformer(final Performer performer) {
        return performers.startUpdateTransactionFor(performer);
    }

    @Override
    public ReversibleTransaction<Movie> removeMovie(final Movie movie) {
        ReversibleTransaction<Movie> transaction = movies.startRemovalTransactionFor(movie);

        transaction.addOperation(new ReversibleTransformation<Movie>() {
            private List<Performer> linkedPerformers;
            private final List<Transaction<Performer, ?>> transactions = new ArrayList<>();
            private ReversibleTransaction<ImagePyramid> imageTransaction;

            @Override
            public Movie forward(final Movie obj) {
                final Optional<ImagePyramid> opt = images.getElementById(obj.getImageId());
                if (opt.isPresent()) {
                    imageTransaction = images.startRemovalTransactionFor(opt.get());
                    imageTransaction.addOperation(removeImageFromStorage());
                    imageTransaction.commit();
                }

                linkedPerformers = new ArrayList<>(moviePerformerAssociations
                        .getAssociatedObjectsOfT1(obj)
                        .orElse(new ArrayList<>()));

                for (int i = 0; i < linkedPerformers.size(); ++i) {
                    final Performer performer = linkedPerformers.get(i);
                    final List<Movie> associatedMovies = getLinkedMoviesOfPerformer(performer);

                    if (associatedMovies.size() == 1) {
                        final Transaction<Performer, ?> trans = removePerformer(performer);
                        transactions.add(trans);
                        trans.commit();
                    } else {
                        unlink(obj, linkedPerformers.get(i));
                    }
                }

                return obj;
            }

            @Override
            public Movie backward(final Movie obj) {
                transactions.forEach(Transaction::rollback);
                linkedPerformers.forEach(p -> link(obj, p));
                if (imageTransaction != null) {
                    imageTransaction.rollback();
                }
                return obj;
            }
        });

        return transaction;
    }

    private ReversibleTransformation<ImagePyramid> removeImageFromStorage() {
        return new ReversibleTransformation<ImagePyramid>() {
            Bitmap stored;

            @Override
            public ImagePyramid forward(final ImagePyramid obj) {
                Optional<Bitmap> opt = obj.loadBitmap(imagePath, ImageSize.LARGE);
                opt.ifPresent(bitmap -> stored = bitmap);
                obj.updateImage(imagePath, null);
                return obj;
            }

            @Override
            public ImagePyramid backward(final ImagePyramid obj) {
                if (stored != null) {
                    obj.updateImage(imagePath, stored);
                }
                return obj;
            }
        };
    }

    @Override
    public ReversibleTransaction<Performer> removePerformer(final Performer performer) {
        final ReversibleTransaction<Performer> transaction = performers.startRemovalTransactionFor(performer);

        transaction.addOperation(new ReversibleTransformation<Performer>() {
            private List<Movie> movies;
            private ReversibleTransaction<ImagePyramid> imageTransaction;

            @Override
            public Performer forward(final Performer obj) {
                final Optional<ImagePyramid> opt = images.getElementById(obj.getImageId());
                if (opt.isPresent()) {
                    imageTransaction = images.startRemovalTransactionFor(opt.get());
                    imageTransaction.addOperation(removeImageFromStorage());
                    imageTransaction.commit();
                }

                movies = new ArrayList<>(getLinkedMoviesOfPerformer(obj));
                movies.forEach(m -> unlink(m, obj));
                return obj;
            }

            @Override
            public Performer backward(final Performer obj) {
                movies.forEach(m -> link(m, obj));
                if (imageTransaction != null) {
                    imageTransaction.rollback();
                }
                return obj;
            }
        });

        return transaction;
    }

    @Override
    public void link(final Movie movie, final Performer performer) {
        moviePerformerAssociations.associate(movie, performer);
        physicalStorage.write(moviePerformerAssociations);
    }

    @Override
    public void unlink(final Movie movie, final Performer performer) {
        moviePerformerAssociations.disassociate(movie, performer);
        physicalStorage.write(moviePerformerAssociations);
    }

    @Override
    public boolean isLinked(final Movie movie, final Performer performer) {
        return moviePerformerAssociations
                .getAssociatedObjectsOfT1(movie)
                .map(li -> li.contains(performer))
                .orElse(false);
    }

    @Override
    public List<Movie> getMovies() {
        return movies.getElements();
    }

    @Override
    public List<Performer> getPerformers() {
        return performers.getElements();
    }

    @Override
    public List<Movie> getLinkedMoviesOfPerformer(final Performer performer) {
        return moviePerformerAssociations.getAssociatedObjectsOfT2(performer).orElse(emptyList());
    }

    @Override
    public List<Performer> getLinkedPerformersOfMovie(final Movie movie) {
        return moviePerformerAssociations.getAssociatedObjectsOfT1(movie).orElse(emptyList());
    }

    @Override
    public Optional<Movie> getMovieById(int id) {
        return movies.getElementById(id);
    }

    @Override
    public Optional<Performer> getPerformerById(int id) {
        return performers.getElementById(id);
    }

    @Override
    public void setImageForPortrayable(final Portrayable portrayable, final Bitmap image) {
        int imageId = portrayable.getImageId();
        final ImagePyramid pyramid = images.getElementById(imageId)
                .orElseGet(() -> newImagePyramid(portrayable.getClass().getSimpleName().toLowerCase()));
        portrayable.setImageId(pyramid.id());
        pyramid.updateImage(imagePath, image);
    }

    private ImagePyramid newImagePyramid(final String prefix) {
        final Optional<ImagePyramid> optional = images.startCreationTransaction()
                .addOperation(reversibleTransformation(
                        ImagePyramid::getPrefix,
                        ImagePyramid::setPrefix,
                        prefix)
                )
                .commit();

        if (optional.isPresent()) {
            return optional.get();
        } else {
            throw new StorageException("Pyramid of images could not been created.");
        }
    }

    @Override
    public Pair<Drawable, Boolean> getImage(final Context context,
                                            final Portrayable portrayal,
                                            final ImageSize size) {
        return ofNullable(portrayal)
                .map(Portrayable::getImageId)
                .flatMap(images::getElementById)
                .flatMap(pyramid -> pyramid.loadBitmap(imagePath, size))
                .map(bitmap -> new BitmapDrawable(context.getResources(), bitmap))
                .map(Drawable.class::cast)
                .map(image -> paired(image, true))
                .orElseGet(() -> paired(getDefaultImage(context, size), false));
    }

    @Override
    public Drawable getDefaultImage(final Context context, final ImageSize size) {
        switch (size) {
            case LARGE:
                return context.getDrawable(R.drawable.default_image_large);
            case MEDIUM:
                return context.getDrawable(R.drawable.default_image_medium);
            case SMALL:
            default:
                return context.getDrawable(R.drawable.default_image_small);
        }
    }

    @Override
    public void clear() {
        selfDestruct();
        setup();
    }

    @Override
    public void selfDestruct() {
        physicalStorage.deleteStorage();
    }

    void close() {
        physicalStorage.close();
    }
}

