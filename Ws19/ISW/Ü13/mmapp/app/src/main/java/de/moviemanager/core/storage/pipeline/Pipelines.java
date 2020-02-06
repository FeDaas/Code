package de.moviemanager.core.storage.pipeline;

public final class Pipelines {
    private Pipelines(){}

    public static void discardAllPipelines() {
        MoviePipeline.discardPipelines();
        PerformerPipeline.discardPipelines();
    }
}
