package de.moviemanager.core.storage.pipeline;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import de.moviemanager.data.Movie;
import de.moviemanager.data.Performer;
import de.moviemanager.data.proxy.PersistentProxy;
import de.moviemanager.util.AndroidStringUtils;
import de.util.operationflow.ReversibleOperations.ReversibleTransformation;
import de.util.operationflow.ReversibleTransaction;
import de.util.operationflow.Transaction;

import static de.moviemanager.core.storage.RuntimeStorageAccess.getInstance;
import static java.util.Objects.requireNonNull;

public class PerformerPipeline {
    private static final Map<String, PerformerPipeline> REGISTERED_PIPELINES;
    private static final Map<String, PersistentProxy> PROXIES;
    private static final Map<String, Function<Movie, ReversibleTransaction<Performer>>> CREATORS;

    static {
        REGISTERED_PIPELINES = new HashMap<>();
        PROXIES = new HashMap<>();
        CREATORS = new HashMap<>();
    }

    public static String beginPipeline() {
        final String key = AndroidStringUtils.generateIdentifier(REGISTERED_PIPELINES::containsKey);
        REGISTERED_PIPELINES.put(key, new PerformerPipeline(key));

        return key;
    }

    static void discardPipelines() {
        Set<String> keys = new HashSet<>(REGISTERED_PIPELINES.keySet());

        keys.forEach(PerformerPipeline::discardPipeline);
    }

    private static void discardPipeline(final String key) {
        REGISTERED_PIPELINES.remove(key);
        PROXIES.remove(key);
    }

    public static void setCreationCore(final String key, final Function<Movie, ReversibleTransaction<Performer>> core) {
        final PerformerPipeline pipeline = REGISTERED_PIPELINES.get(key);
        if (pipeline != null) {
            pipeline.setCreationCore(core);
        }
    }

    public static void setUpdateCore(final String key, final ReversibleTransaction<Performer> core) {
        final PerformerPipeline pipeline = REGISTERED_PIPELINES.get(key);
        if (pipeline != null) {
            pipeline.setUpdateCore(core);
        }
    }

    public static void setPersistentLinkedMovies(final String key, final ReversibleTransformation<Performer> operation) {
        final PerformerPipeline pipeline = REGISTERED_PIPELINES.get(key);
        if(pipeline != null) {
            pipeline.setPersistentLinkedMovies(operation);
        }

    }

    public static void setLinkedMovies(final String key, final List<Movie> movies) {
        final PerformerPipeline pipeline = REGISTERED_PIPELINES.get(key);
        if (pipeline != null) {
            pipeline.setLinkedMovies(movies);
        }
    }

    public static void addTemporaryMovie(final String key,
                                             final Supplier<ReversibleTransaction<Movie>> creator) {
        final PerformerPipeline pipeline = REGISTERED_PIPELINES.get(key);
        if (pipeline != null) {
            pipeline.addTemporaryMovie(requireNonNull(creator));
        }
    }

    public static void commit(final String key) {
        final PerformerPipeline pipeline = REGISTERED_PIPELINES.get(key);
        if (pipeline != null) {
            pipeline.commit();
        }
    }

    public static Optional<PersistentProxy> getResultOf(final String key) {
        return Optional.ofNullable(PROXIES.get(key));
    }

    public static String saveTemporaryCreator(final Function<Movie, ReversibleTransaction<Performer>> creator) {
        final String key = AndroidStringUtils.generateIdentifier(CREATORS::containsKey);
        CREATORS.put(key, creator);
        return key;
    }

    public static Function<Movie, ReversibleTransaction<Performer>> pullTemporaryFunction(final String key) {
        final Function<Movie, ReversibleTransaction<Performer>> creator = CREATORS.get(key);
        CREATORS.remove(key);
        return creator;
    }

    private final String name;
    private Function<Movie, ReversibleTransaction<Performer>> creationCore;
    private ReversibleTransaction<Performer> updateCore;
    private ReversibleTransformation<Performer> persistentLinkedMovies;
    private final List<Supplier<ReversibleTransaction<Movie>>> temporaryMovieCreators;
    private final List<Movie> linkedMovies;

    private PerformerPipeline(final String name) {
        this.name = name;
        temporaryMovieCreators = new ArrayList<>();
        linkedMovies = new ArrayList<>();
    }

    private void setCreationCore(final Function<Movie, ReversibleTransaction<Performer>> core) {
        setCore(core, null);
    }

    private void setUpdateCore(final ReversibleTransaction<Performer> core) {
        setCore(null, core);
    }

    private void setCore(final Function<Movie, ReversibleTransaction<Performer>> create, final ReversibleTransaction<Performer> update) {
        creationCore = create;
        updateCore = update;
    }

    private void addTemporaryMovie(final Supplier< ReversibleTransaction<Movie>> creator) {
        this.temporaryMovieCreators.add(creator);
    }

    private void setPersistentLinkedMovies(final ReversibleTransformation<Performer> operation) {
        this.persistentLinkedMovies = operation;
    }

    private void setLinkedMovies(final List<Movie> linkedMovies) {
        this.linkedMovies.clear();
        this.linkedMovies.addAll(linkedMovies);
    }

    private void commit() {
        final ReversibleTransaction<Performer> core = forgeCore();
        final Optional<Performer> performerOpt = core.commit();

        if(performerOpt.isPresent()) {
            final Performer performer = performerOpt.get();
            PROXIES.put(name, new PersistentProxy(performer));
        }
    }

    private ReversibleTransaction<Performer> forgeCore() {
        final List<Movie> movies = new ArrayList<>(linkedMovies);
        final List<Movie> temporaryMovies = createTemporaries();
        movies.addAll(temporaryMovies);
        ReversibleTransaction<Performer> result;

        if(creationCore != null) {
            final Movie movie = movies.remove(0);
            result = creationCore.apply(movie);
        } else {
            result = updateCore;
        }

        result.addOperation(temporaryToTransformation(temporaryMovies));
        result.addOperation(persistentLinkedMovies);

        return result;
    }

    private List<Movie> createTemporaries() {
        return temporaryMovieCreators.stream()
                .map(Supplier::get)
                .map(Transaction::commit)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private ReversibleTransformation<Performer> temporaryToTransformation(final List<Movie> temporaryMovies) {
        return new ReversibleTransformation<Performer>(){

            @Override
            public Performer forward(Performer obj) {
                for(Movie m : temporaryMovies) {
                    getInstance().link(m, obj);
                }
                return obj;
            }

            @Override
            public Performer backward(Performer obj) {
                for(Movie m : temporaryMovies) {
                    getInstance().unlink(m, obj);
                }
                return obj;
            }
        };
    }
}
