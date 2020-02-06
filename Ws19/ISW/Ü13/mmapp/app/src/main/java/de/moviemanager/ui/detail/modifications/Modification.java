package de.moviemanager.ui.detail.modifications;

import java.util.List;
import java.util.function.Consumer;

public class Modification <T> {
    private final T oldState;
    private final Consumer<T> undoAction;

    public Modification(T oldState, Consumer<T> undoAction) {
        this.oldState = oldState;
        this.undoAction = undoAction;
    }

    public T getOldState() {
        return oldState;
    }

    public void undo() {
        undoAction.accept(oldState);
    }

    public static Modification<Object> stack(List<Modification<?>> modifications) {
        return new Modification<>(null, o -> modifications.forEach(Modification::undo));
    }
}