package de.util.operationflow;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import de.util.mock.DummyPerson;
import de.util.operationflow.ReversibleOperations.ReversibleConsumer;
import de.util.operationflow.ReversibleOperations.ReversibleSupplier;
import de.util.operationflow.ReversibleOperations.ReversibleTransformation;

import static de.util.operationflow.ReversibleOperations.reversibleTransformation;
import static de.util.operationflow.Transaction.beginReversibleTransaction;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReversibleTransactionTest {
    @Test
    void testRollbackCommit() {
		Transaction<DummyPerson, ?> transaction = createTransaction();
		assertTrue(transaction.supportsRollback());

		Optional<DummyPerson> personOptional = transaction.commit();
		assertTrue(personOptional.isPresent());
		DummyPerson person = personOptional.get();
		assertEquals("dummyChanged", person.tellName());
		assertEquals(21 + 3, person.getAge());
	
		transaction.rollback();
		assertTrue(transaction.wasRolledBack());
 		assertEquals("dummy1", person.tellName());
		assertEquals(21, person.getAge());
    }

    private Transaction<DummyPerson, ReversibleTransformation<DummyPerson>> createTransaction() {
		ReversibleSupplier<DummyPerson> begin = createBegin();
		ReversibleConsumer<DummyPerson> end = createEnd();
		Transaction<DummyPerson, ReversibleTransformation<DummyPerson>> transaction = beginReversibleTransaction(begin, end);
		transaction.addOperation(reversibleTransformation(
				DummyPerson::tellName,
				DummyPerson::changeNameTo,
				"dummyChanged"
		));
		transaction.addOperation(reversibleTransformation(
				DummyPerson::incrementAge,
				DummyPerson::decrementAge
		));
		transaction.addOperation(reversibleTransformation(
				DummyPerson::incrementAge,
				DummyPerson::decrementAge
		));
		transaction.addOperation(reversibleTransformation(
				DummyPerson::incrementAge,
				DummyPerson::decrementAge
		));
		return transaction;
	}
    
    private ReversibleSupplier<DummyPerson> createBegin() {
		return new ReversibleSupplier<DummyPerson>() {

	    	@Override
	    	public DummyPerson forward() {
				return new DummyPerson("dummy1", 21);
	    	}

	    	@Override
	    	public void backward(DummyPerson person) {
	    	}
		};
    }
    
    private ReversibleConsumer<DummyPerson> createEnd() {
		return new ReversibleConsumer<DummyPerson>() {

	    	private DummyPerson stored;
	    
	    	@Override
	    	public void forward(DummyPerson obj) {
				stored = obj;
	    	}

	   		@Override
	    	public DummyPerson backward() {
				return stored;
	    	}
		};
    }
    
    @Test
    void testRollbackBeforeCommit() {
		ReversibleSupplier<DummyPerson> begin = createBegin();
		ReversibleConsumer<DummyPerson> end = createEnd();
		Transaction<DummyPerson, ?> transaction = beginReversibleTransaction(begin, end);
	
		assertThrows(RuntimeException.class, transaction::rollback);
    }
    
    @Test
    void testAbort() {
		ReversibleSupplier<DummyPerson> begin = createBegin();
		ReversibleConsumer<DummyPerson> end = createEnd();
		Transaction<DummyPerson, ReversibleTransformation<DummyPerson>> transaction = beginReversibleTransaction(begin, end);
		ReversibleTransformation<DummyPerson> operation = reversibleTransformation(DummyPerson::incrementAge,
				DummyPerson::decrementAge);
		transaction.abort();

		Class<RuntimeException> excClass = RuntimeException.class;
		assertThrows(excClass, transaction::abort);
		assertThrows(excClass, transaction::commit);
		assertThrows(excClass, transaction::rollback);
		assertThrows(excClass, () -> transaction.addOperation(operation));
    }
}
