package de.util.operationflow;

import static java.util.Optional.empty;

import java.util.Optional;

/**
 * A transaction which executes a sequence of {@link Runnable}s, which can not
 * be reverted.
 *
 * <br><b>Author</b>: <a href="mailto:schustrchr@gmail.com">Ctoffer</a><br>
 */
class SimpleTransaction extends ProtoTransaction<Void, Runnable> {
    
    SimpleTransaction() {
        super(false);
    }
    
    @Override
    protected Optional<Void> runOperations() {
	    operations.forEach(Runnable::run);
	    return empty();
    }
    
    @Override
    protected void additionalClear() {
        // No additional clear needed
    }
    
    @Override
    protected void runRollback() {
        throw new UnsupportedOperationException();
    }
}
