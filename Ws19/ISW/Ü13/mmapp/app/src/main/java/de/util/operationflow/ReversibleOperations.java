package de.util.operationflow;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import de.util.operationflow.ReversibleOperations.ReversibleTransformation;

public final class ReversibleOperations {
    private ReversibleOperations() {
    }

    public interface ReversibleSupplier<T> {
	    T forward();
	    void backward(T obj);
    }
    
    public interface ReversibleTransformation<T> {
	    T forward(T obj);
	    T backward(T obj);
    }

    public interface ReversibleConsumer<T> {
	    void forward(T obj);
	    T backward();
    }
    
    public static <T, A> ReversibleTransformation<T> reversibleTransformation(
            Function<T, A> getter, BiConsumer<T, A> setter, A object) {
	    GetSetTransformation<T, A> transform = new GetSetTransformation<>(object);
	    transform.injectGetterSetter(getter, setter);
	    return transform;
    }
    
    public static <T> ReversibleTransformation<T> reversibleTransformation(
            Consumer<T> forward, Consumer<T> backward) {
	    return new ComplementTransformation<>(forward, backward);
    }
}

class GetSetTransformation <T, A> implements ReversibleTransformation<T> {
    private final A object;
    private Function<T, A> getter;
    private BiConsumer<T, A> setter;
    private A old;
    
    public GetSetTransformation(final A object) {
	    this.object = object;
    }
    
    public void injectGetterSetter(Function<T, A> getter, BiConsumer<T, A> setter) {
	    this.getter = getter;
	    this.setter = setter;
    }
    
    @Override
    public T forward(T obj) {
	    old = getter.apply(obj);
	    setter.accept(obj, object);
	    return obj;
    }

    @Override
    public T backward(T obj) {
	    setter.accept(obj, old);
	    return obj;
    }  
}

class ComplementTransformation <T> implements ReversibleTransformation<T> {
    private final Consumer<T> forward;
    private final Consumer<T> backward;
    
    public ComplementTransformation(final Consumer<T> forward, final Consumer<T> backward) {
	    this.forward = forward;
	    this.backward = backward;
    }

    @Override
    public T forward(final T obj) {
	    forward.accept(obj);
	    return obj;
    }

    @Override
    public T backward(final T obj) {
	    backward.accept(obj);
	    return obj;
    }
}
