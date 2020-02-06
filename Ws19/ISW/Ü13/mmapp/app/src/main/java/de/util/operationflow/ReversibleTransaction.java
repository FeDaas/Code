package de.util.operationflow;

import java.util.ListIterator;
import java.util.Optional;

import de.util.operationflow.ReversibleOperations.ReversibleConsumer;
import de.util.operationflow.ReversibleOperations.ReversibleSupplier;
import de.util.operationflow.ReversibleOperations.ReversibleTransformation;

/**
 * A reversible transaction, which modifies an object of type T in such way, that each
 * transformation step (begin and end included) is revertible.
 * For more details see {@link Transaction#beginReversibleTransaction}<br>
 *
 * <br><b>Author</b>: <a href="mailto:schustrchr@gmail.com">Ctoffer</a><br>
 * @param <T> type of the object for which this transaction is created
 */
public class ReversibleTransaction<T> extends ProtoTransaction<T, ReversibleTransformation<T>> {
    private ReversibleSupplier<T> begin;
    private ReversibleConsumer<T> end;
    
    public ReversibleTransaction(ReversibleSupplier<T> begin, ReversibleConsumer<T> end) {
	    super(true);
	    this.begin = begin;
	    this.end = end;
    }
    
    @Override
    protected Optional<T> runOperations() {
	    T obj = begin.forward();
	    for(ReversibleTransformation<T> transform : operations)
	        obj = transform.forward(obj);
	
	    end.forward(obj);
	    return Optional.of(obj);
    }

    @Override
    protected void additionalClear() {
	    begin = null;
	    end = null;
    }
    
    @Override
    protected void runRollback() {
	    T obj = end.backward();
	    ListIterator<ReversibleTransformation<T>> iter = operations.listIterator(operations.size());
		while(iter.hasPrevious())
			iter.previous().backward(obj);
	
	    begin.backward(obj);
    }

}
