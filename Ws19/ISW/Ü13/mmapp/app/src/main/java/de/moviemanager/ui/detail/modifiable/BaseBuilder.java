package de.moviemanager.ui.detail.modifiable;

import android.widget.ScrollView;

import java.util.function.Function;

import de.util.operationflow.ReversibleOperations.ReversibleTransformation;

public class BaseBuilder<X, T, A extends ModifiableAttribute<X, T>> {
    private final ModifiableAppCompatActivity context;
    private TriFunction<ModifiableAppCompatActivity, ScrollView, Integer, A> constructor;
    private int editTextId;
    private ScrollView root;
    private Function<T, ReversibleTransformation<X>> source;
    private Function<X, T> contentExtractor;

    public BaseBuilder(ModifiableAppCompatActivity context) {
        this.context = context;
    }

    public BaseBuilder<X, T, A> setConstructor(TriFunction<ModifiableAppCompatActivity, ScrollView, Integer, A> constructor) {
        this.constructor = constructor;
        return this;
    }

    public BaseBuilder<X, T, A> setViewId(int editTextId) {
        this.editTextId = editTextId;
        return this;
    }

    public BaseBuilder<X, T, A> setContentExtractor(Function<X, T> contentExtractor) {
        this.contentExtractor = contentExtractor;
        return this;
    }

    public BaseBuilder<X, T, A> setRoot(ScrollView root) {
        this.root = root;
        return this;
    }

    public BaseBuilder<X, T, A> setSource(Function<T, ReversibleTransformation<X>> source) {
        this.source = source;
        return this;
    }

    public A build() {
        A attr = constructor.apply(context, root, editTextId);
        attr.setTransformationSource(source);
        attr.setContentExtractor(contentExtractor);
        callAdditionalSetters(attr);
        return attr;
    }

    void callAdditionalSetters(A attr) {
        // in base class not needed
    }

    @FunctionalInterface
    public interface TriFunction<A, B, C, D> {
        D apply(A first, B second, C third);
    }
}