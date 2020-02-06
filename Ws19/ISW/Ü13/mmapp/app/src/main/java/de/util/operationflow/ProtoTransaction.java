package de.util.operationflow;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static de.util.operationflow.Transaction.ProgressState.ABORTED;
import static de.util.operationflow.Transaction.ProgressState.COMMITTED;
import static de.util.operationflow.Transaction.ProgressState.ROLLED_BACK;
import static de.util.operationflow.Transaction.ProgressState.UNDER_CONSTRUCTION;

/**
 * Base for transaction, which share the following contract:
 * <ul>
 *     <li>
 *         Each operation can only performed if the transaction is in the state
 *         {@link ProgressState#UNDER_CONSTRUCTION} except for rollback. Only in the state
 *         {@link ProgressState#COMMITTED} a rollback can be performed - if the rollback is
 *         supported by the transaction.
 *     </li>
 *     <li>
 *         commit: <br>
 *             A commit always puts the state of the transaction to {@link ProgressState#COMMITTED}
 *             even if the commit fails. A commit may throw a (subtype of) {@link RuntimeException}
 *             or return an empty optional if it fails.
 *     </li>
 *     <li>
 *         abort: <br>
 *             An abort always put the state of the transaction to {@link ProgressState#ABORTED}
 *             and removes all operations from the storage.
 *     </li>
 *     <li>
 *         rollback: <br>
 *             A rollback always puts the state of the transaction to {@link ProgressState#ROLLED_BACK}.
 *             Subclasses need to ensure that the state before this transaction was committed is
 *             restored without side-effects.
 *     </li>
 * </ul>
 *
 * <br><b>Author</b>: <a href="mailto:schustrchr@gmail.com">Ctoffer</a><br>
 * @param <T> type of the object for which this transaction is created
 * @param <O> type of the operation
 */
abstract class ProtoTransaction <T, O> implements Transaction<T, O> {
    private final boolean rollbackSupport;
    
    final List<O> operations;
    private ProgressState state;
    
    ProtoTransaction(boolean rollbackSupport) {
	    this.rollbackSupport = rollbackSupport;
	    operations = new ArrayList<>();
	    state = UNDER_CONSTRUCTION;
    }
    
    @Override
    public boolean supportsRollback() {
        return rollbackSupport;
    }
    
    @Override
    public Optional<T> commit() {
	    blockIfNotUnderConstruction();
	    state = COMMITTED;
        return runOperations();
    }

    protected abstract Optional<T> runOperations();
    
    @Override
    public Transaction<T, O> addOperation(O operation) {
        blockIfNotUnderConstruction();
        operations.add(operation);
        return this;
    }
    
    @Override
    public void abort() {
        blockIfNotUnderConstruction();
        state = ABORTED;
        operations.clear();
        additionalClear();
    }
    
    protected abstract void additionalClear();
    
    private void blockIfNotUnderConstruction() {
	    if(!isUnderConstruction()) {
            throw new TransactionException("Transaction already finished!");
        }
    }
    
    @Override
    public boolean isUnderConstruction() {
        return state == UNDER_CONSTRUCTION;
    }
    
    @Override
    public void rollback() {
	    blockIfRollbackUnsupported();
	    blockIfNotCommitted();
        state = ROLLED_BACK;
        runRollback();
    }
    
    protected abstract void runRollback(); 
    
    private void blockIfRollbackUnsupported() {
	    if(!supportsRollback()) {
            throw new TransactionException(getClass() + " doesn't support rollback.");
        }
    }
    
    private void blockIfNotCommitted() {
	    if(!wasCommitted()) {
	        final String msg = "Transaction must be comitted before a rollback can be performed";
	        throw new TransactionException(msg);
	    }
    }
    
    @Override
    public boolean wasCommitted() {
        return state == COMMITTED;
    }
    
    @Override
    public boolean wasAborted() {
        return state == ABORTED;
    }
    
    @Override
    public boolean wasRolledBack() {
        return state == ROLLED_BACK;
    }
}
