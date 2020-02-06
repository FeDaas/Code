package de.moviemanager.ui.detail.modifiable;

import java.util.function.Consumer;
import java.util.function.Function;

import de.moviemanager.ui.detail.modifications.Modification;
import de.util.operationflow.ReversibleOperations.ReversibleTransformation;

public abstract class ModifiableAttribute<X, T> {
    private final ModifiableAppCompatActivity modContext;
    private Function<T, ReversibleTransformation<X>> source;
    private Function<X, T> contentExtractor;
    private Consumer<T> additionalSetterLogic;

    ModifiableAttribute(ModifiableAppCompatActivity modContext) {
        this.modContext = modContext;
        this.additionalSetterLogic = obj -> {};
    }

    ModifiableAppCompatActivity getContext() {
        return modContext;
    }

    public abstract void bindViews();
    public abstract void bindListeners();

    public void setContentExtractor(Function<X, T> contentExtractor) {
        this.contentExtractor = contentExtractor;
    }

    public void setAdditionalSetterLogic(Consumer<T> additionalSetterLogic) {
        this.additionalSetterLogic = additionalSetterLogic;
    }

    public void setContentModel(X model) {
        T content = contentExtractor.apply(model);
        setContent(content);
        runAdditionalSetterLogic(content);
    }

    protected abstract void setContent(T content);

    private void runAdditionalSetterLogic(T content) {
        additionalSetterLogic.accept(content);
    }

    protected abstract T getContent();

    public void setTransformationSource(Function<T, ReversibleTransformation<X>> source) {
        this.source = source;
    }

    public ReversibleTransformation<X> toTransformation() {
        return source.apply(getContent());
    }

    public static <X> ImageAttribute.Builder<X> createImageAttribute(ModifiableAppCompatActivity context) {
        return new ImageAttribute.Builder<>(context);
    }

    public static <X> TextInputAttribute.Builder<X> createTextInputAttribute(ModifiableAppCompatActivity context) {
        return new TextInputAttribute.Builder<>(context);
    }

    public Modification<T> modifyValue(T newValue) {
        Modification<T> modification = new Modification<>(getContent(), this::setContent);
        setContent(newValue);
        return modification;
    }
}
