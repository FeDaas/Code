package de.util.operationflow;

import java.util.Optional;

import de.util.operationflow.ReversibleOperations.ReversibleConsumer;
import de.util.operationflow.ReversibleOperations.ReversibleSupplier;

/**
 * Basic interface for transactions. A transaction models a sequence of operations
 * which can either be committed - all operations will be performed in order - or
 * aborted - no operation will be performed.
 * <p>
 *     In addition to this basic behaviour a committed transaction can be rolled back.
 *     This means, that each operations performed in the commit will be reverted in such way,
 *     as if the transaction was never committed at all.
 * </p>
 * <p>
 *     <b>Note:</b> Not all transactions need to support a rollback, sometimes it's not possible
 *     (or desired) to revert a sequence of operations.
 * </p>
 *
 * <br><b>Author</b>: <a href="mailto:schustrchr@gmail.com">Ctoffer</a><br>
 *
 * @param <T> type of the object modified by this transaction
 * @param <O> type of the operation
 */
public interface Transaction <T, O> {
    /**
     * States that a transaction can have:
     * <ul>
     *     <li>
     *         <i>UNDER_CONSTRUCTION</i>: The transaction is neither committed nor aborted.
     *     </li>
     *     <li>
     *         <i>COMMITTED</i>: The transaction was committed and all stored
     *                           operations got executed.
     *     </li>
     *     <li>
     *         <i>ABORTED</i>: The transaction was aborted and none of the stored operations
     *                         got executed.
     *     </li>
     *     <li>
     *         <i>ROLLED_BACK</i>: The transaction was rolled back after being committed and every
     *                             stored operation was reverted.
     *     </li>
     * </ul>
     */
    enum ProgressState {
	    UNDER_CONSTRUCTION, COMMITTED, ABORTED, ROLLED_BACK
    }

    /**
     * Creates a transaction, which only contains a sequence of {@link Runnable}s.
     * This kind of transaction does not create or modify a specific single object, but instead
     * execute a sequence of operations of any kind. Because the operations can be of any kind
     * they can't be reverted in general.
     *
     * @return non revertible transaction
     */
    static Transaction<Void, Runnable> beginSimpleTransaction() {
	    return new SimpleTransaction();
    }

    /**
     * Creates a transaction, which supports rollback. This transaction is designed to create and
     * adapt an object of type S in a revertible pipeline. The pipeline contains the elements
     * p<sub>0</sub> to p<sub>n</sub> which will be executed in 3 steps:
     * <ol>
     *     <li>
     *         p<sub>0</sub>: retrieve object from the begin
     *     </li>
     *     <li>
     *         p<sub>1</sub> to p<sub>n-1</sub>: transform the object with the stored
     *         transformations in order
     *     </li>
     *     <li>
     *         p<sub>n</sub>: consume object at the end
     *     </li>
     * </ol>
     * In the consumption step one could inform listeners or add the object to a list. In addition
     * the object gets returned by the {@link Transaction#commit()}-call, so this could also
     * be done after running to the pipeline.<br>
     * The rollback is done by inverting the pipeline:
     * <ol>
     *     <li>
     *         p<sub>n</sub>: retrieve object from the end
     *     </li>
     *     <li>
     *         p<sub>n-1</sub> to p<sub>1</sub>: revert each transformation in reverse order
     *     </li>
     *     <li>
     *         p<sub>0</sub>: consume object at the begin
     *     </li>
     * </ol>
     * The rollback can't be rolled back again.<br>
     * Scheme of state transitions:<br>
     * <img
     *  src="doc-files/revertible_transaction_scheme.png"
     *  alt="scheme of transaction state transitions"
     *  width="800"
     *  ><br>
     * Scheme of revertible pipeline:<br>
     * <img
     *  src="doc-files/revertible_transaction_pipeline.png"
     *  alt="scheme of revertible transaction pipeline"
     *  width="800"
     *  ><br>
     *
     * @param begin the begin of the pipeline
     * @param end the end of the pipeline
     * @param <S> type of the object around this transaction is centered
     * @return a new revertible transaction
     */
    static <S> ReversibleTransaction<S> beginReversibleTransaction(
            final ReversibleSupplier<S> begin, final ReversibleConsumer<S> end) {
	    return new ReversibleTransaction<>(begin, end);
    }

    /**
     * Commits the transaction and executes all operations. If the transaction fails this method
     * may return an empty {@link Optional}.<br>
     * After this method the state of the transaction is {@link ProgressState#COMMITTED}.
     *
     * @return the modified object or empty if transaction fails
     */
    Optional<T> commit();

    /**
     * Adds a operation to this transaction, while under construction.
     *
     * @param operation the operation which should be added
     * @return this transaction
     * @throws RuntimeException if this transaction is not in state
     *         {@link ProgressState#UNDER_CONSTRUCTION}
     */
    Transaction<T, O> addOperation(O operation);

    /**
     * Aborts this transaction and sets it into the state {@link ProgressState#ABORTED}.
     *
     * @throws RuntimeException if this transaction is not in
     *         state {@link ProgressState#UNDER_CONSTRUCTION}
     */
    void abort();

    /**
     * Returns true if this transaction is in the state {@link ProgressState#UNDER_CONSTRUCTION}.
     * If this is the case all other state checkers must return false.
     * @see Transaction#wasCommitted()
     * @see Transaction#wasAborted()
     * @see Transaction#wasRolledBack()
     *
     * @return true if state of transaction is {@link ProgressState#UNDER_CONSTRUCTION}
     */
    boolean isUnderConstruction();

    /**
     * Returns true if this transaction is in the state {@link ProgressState#COMMITTED}.
     * If this is the case all other state checkers must return false.
     * @see Transaction#isUnderConstruction()
     * @see Transaction#wasAborted()
     * @see Transaction#wasRolledBack()
     *
     * @return true if state of transaction is {@link ProgressState#COMMITTED}
     */
    boolean wasCommitted();

    /**
     * Returns true if this transaction is in the state {@link ProgressState#ABORTED}.
     * If this is the case all other state checkers must return false.
     * @see Transaction#isUnderConstruction()
     * @see Transaction#wasCommitted()
     * @see Transaction#wasRolledBack()
     *
     * @return true if state of transaction is {@link ProgressState#ABORTED}
     */
    boolean wasAborted();

    /**
     * Returns true if this transaction is in the state {@link ProgressState#ROLLED_BACK}.
     * If this is the case all other state checkers must return false.
     * @see Transaction#isUnderConstruction()
     * @see Transaction#wasCommitted()
     * @see Transaction#wasAborted()
     *
     * @return true if state of transaction is {@link ProgressState#ROLLED_BACK}
     */
    boolean wasRolledBack();

    /**
     * Returns true if this transaction supports a rollback.
     *
     * @return true if this transaction supports a rollback
     */
    boolean supportsRollback();

    /**
     * Reverts all changes produced by the commit. After this operation the state of the transaction
     * is {@link ProgressState#ROLLED_BACK}
     *
     * @throws RuntimeException if this transaction does not support rollback
     * @throws RuntimeException if this transaction is not in state {@link ProgressState#COMMITTED}
     */
    void rollback();
}
