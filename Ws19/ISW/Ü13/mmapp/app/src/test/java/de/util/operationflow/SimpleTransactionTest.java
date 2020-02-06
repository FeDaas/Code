package de.util.operationflow;

import org.junit.jupiter.api.Test;

import java.util.List;

import static de.util.operationflow.Transaction.beginSimpleTransaction;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimpleTransactionTest {
    
    @Test
    void testLifecycle() {
		List<Boolean> conditions = asList(false, false, true);
		Transaction<Void, Runnable> transaction1 = beginSimpleTransaction();
		assertTrue(transaction1.isUnderConstruction());
	
		transaction1.addOperation(() -> conditions.set(0, true));
		transaction1.addOperation(() -> conditions.set(1, true));
		assertEquals(asList(false, false, true), conditions);
		transaction1.commit();
	
		assertEquals(asList(true, true, true), conditions);
		assertTrue(transaction1.wasCommitted());

		Transaction<Void, Runnable> transaction2 = beginSimpleTransaction();
		transaction2.addOperation(() -> conditions.set(0, false)).abort();
		assertEquals(asList(true, true, true), conditions);
		assertTrue(transaction2.wasAborted());
    }
    
    @Test
    void testModificationAfterFinishing() {
		Transaction<Void, Runnable> transaction = beginSimpleTransaction();
		transaction.commit();

		final Class<RuntimeException> excClass = RuntimeException.class;
		assertThrows(excClass, transaction::commit);
		assertThrows(excClass, () -> transaction.addOperation(() -> {}));
		assertThrows(excClass, transaction::abort);
    }
    
    @Test
    void testCommitStates() {
		Transaction<Void, Runnable> transaction = beginSimpleTransaction();
		assertTrue(transaction.isUnderConstruction());
		assertFalse(transaction.wasAborted());
		assertFalse(transaction.wasCommitted());
		assertFalse(transaction.wasRolledBack());
	
		transaction.commit();
		assertFalse(transaction.isUnderConstruction());
		assertFalse(transaction.wasAborted());
		assertTrue(transaction.wasCommitted());
		assertFalse(transaction.wasRolledBack());
    }
    
    @Test
    void testAbortStates() {
		Transaction<Void, Runnable> transaction = beginSimpleTransaction();
		assertTrue(transaction.isUnderConstruction());
		assertFalse(transaction.wasAborted());
		assertFalse(transaction.wasCommitted());
		assertFalse(transaction.wasRolledBack());
	
		transaction.abort();
		assertFalse(transaction.isUnderConstruction());
		assertTrue(transaction.wasAborted());
		assertFalse(transaction.wasCommitted());
		assertFalse(transaction.wasRolledBack());
    }
    
    @Test
    void testRollbackIsNotSupported() {
		Transaction<Void, Runnable> transaction = beginSimpleTransaction();
		assertFalse(transaction.supportsRollback());
		String msg = transaction.getClass() + " doesn't support rollback.";
		assertThrows(RuntimeException.class, transaction::rollback, msg);
    }
}
