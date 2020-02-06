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

import de.moviemanager.data.Movie;
import de.moviemanager.data.Performer;
import de.moviemanager.data.proxy.PersistentProxy;
import de.moviemanager.util.AndroidStringUtils;
import de.util.operationflow.ReversibleOperations.ReversibleTransformation;
import de.util.operationflow.ReversibleTransaction;

import static java.util.Objects.requireNonNull;

public class MoviePipeline {
    private static final Map<String, MoviePipeline> REGISTERED_PIPELINES;
    private static final Map<String, PersistentProxy> PROXIES;
    private static final Map<String, Supplier<ReversibleTransaction<Movie>>> CREATORS;

    static {
        REGISTERED_PIPELINES = new HashMap<>();
        PROXIES = new HashMap<>();
        CREATORS = new HashMap<>();
    }

    public static String beginPipeline() {
        final String key = AndroidStringUtils.generateIdentifier(REGISTERED_PIPELINES::containsKey);
        REGISTERED_PIPELINES.put(key, new MoviePipeline(key));

        return key;
    }

    static void discardPipelines() {
        Set<String> keys = new HashSet<>(REGISTERED_PIPELINES.keySet());

        keys.forEach(MoviePipeline::discardPipeline);
    }

    private static void discardPipeline(final String key) {
        REGISTERED_PIPELINES.remove(key);
        PROXIES.remove(key);
    }

    public static void setCore(final String key, final ReversibleTransaction<Movie> core) {
        final MoviePipeline pipeline = REGISTERED_PIPELINES.get(key);
        if (pipeline != null) {
            pipeline.setCore(requireNonNull(core));
        }
    }

    public static void setPersistentLinkedPerformers(final String key, final ReversibleTransformation<Movie> operation) {
        final MoviePipeline pipeline = REGISTERED_PIPELINES.get(key);
        if(pipeline != null) {
            pipeline.setPersistentLinkedPerformers(operation);
        }
    }

    public static void addTemporaryPerformer(final String key,
                                             final Function<Movie, ReversibleTransaction<Performer>> creator) {
        final MoviePipeline pipeline = REGISTERED_PIPELINES.get(key);
        if (pipeline != null) {
            pipeline.addTemporaryPerformer(requireNonNull(creator));
        }
    }

    public static void commit(final String key) {
        final MoviePipeline pipeline = REGISTERED_PIPELINES.get(key);
        if (pipeline != null) {
            pipeline.commit();
        }
    }

    public static Optional<PersistentProxy> getResultOf(final String key) {
        return Optional.ofNullable(PROXIES.get(key));
    }

    public static String saveTemporaryCreator(final Supplier<ReversibleTransaction<Movie>> creator) {
        final String key = AndroidStringUtils.generateIdentifier(CREATORS::containsKey);
        CREATORS.put(key, creator);
        return key;
    }

    public static Supplier<ReversibleTransaction<Movie>> pullTemporaryFunction(final String key) {
        final Supplier<ReversibleTransaction<Movie>> creator = CREATORS.get(key);
        CREATORS.remove(key);
        return creator;
    }

    private final String name;
    private ReversibleTransaction<Movie> coreTransaction;
    private ReversibleTransformation<Movie> persistentLinkedPerformers;
    private final List<Function<Movie, ReversibleTransaction<Performer>>> temporaryPerformers;

    private MoviePipeline(final String name) {
        this.name = name;
        this.temporaryPerformers = new ArrayList<>();
    }

    private void setCore(final ReversibleTransaction<Movie> core) {
        this.coreTransaction = core;
    }

    private void setPersistentLinkedPerformers(ReversibleTransformation<Movie> operation) {
        this.persistentLinkedPerformers = operation;
    }

    private void addTemporaryPerformer(final Function<Movie, ReversibleTransaction<Performer>> creator) {
        this.temporaryPerformers.add(creator);
    }

    private void commit() {
        final Optional<Movie> movieOpt = coreTransaction.commit();
        if (movieOpt.isPresent()) {
            final Movie movie = movieOpt.get();
            temporaryPerformers.forEach(tmp -> tmp.apply(movie).commit());
            persistentLinkedPerformers.forward(movie);
            PROXIES.put(name, new PersistentProxy(movie));
        }
    }
}
