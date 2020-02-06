package de.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Supplier;

import de.storage.mock.IdMock;
import de.util.operationflow.Transaction;

import static de.util.operationflow.ReversibleOperations.reversibleTransformation;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegisterTest {
    private final Supplier<NoSuchElementException> REGISTER_EXCEPTION = NoSuchElementException::new;
    private Register<IdMock> register1;

    @BeforeEach
    void init() {
        register1 = new Register<>(IdMock::new);
    }

    @Test
    void testCreateNew() {
        // precondition
        assertEquals(0, register1.totalSpace());
        assertEquals(0, register1.usedSpace());
        assertEquals(0, register1.freeSpace());

        // test
        final Optional<IdMock> obj = register1.startCreationTransaction().commit();
        assertTrue(obj.isPresent());
        assertEquals(0, obj.get().id());

        // postcondition
        final List<IdMock> elems = register1.getElements();
        assertEquals(1, register1.getElements().size());
        assertEquals(obj.get(), elems.get(0));
        assertEquals(1, register1.totalSpace());
        assertEquals(1, register1.usedSpace());
        assertEquals(0, register1.freeSpace());
    }

    @Test
    void testIdReuse() throws Throwable {
        // setup
        final Optional<IdMock> obj1 = register1.startCreationTransaction().commit();
        final Optional<IdMock> obj2 = register1.startCreationTransaction().commit();
        final Transaction<IdMock, ?> removal = register1.startRemovalTransactionFor(obj1.orElseThrow(REGISTER_EXCEPTION));
        final Optional<IdMock> obj3 = removal.commit();

        // precondition
        assertEquals(obj1, obj3);
        assertEquals(2, register1.totalSpace());
        assertEquals(1, register1.usedSpace());
        assertEquals(1, register1.freeSpace());

        // test
        final Optional<IdMock> obj4 = register1.startCreationTransaction().commit();
        assertEquals(0, obj4.orElseThrow(REGISTER_EXCEPTION).id());
        removal.rollback();

        assertNotEquals(obj4.get(), register1.getElementById(0).orElseThrow(REGISTER_EXCEPTION));
        assertEquals(obj1.get(), register1.getElementById(0).orElseThrow(REGISTER_EXCEPTION));
    }

    @Test
    void testModificationWithRollback() throws Throwable {
        // setup
        final Optional<IdMock> opt1 = register1.startCreationTransaction().commit();
        final IdMock obj1 = opt1.orElseThrow(REGISTER_EXCEPTION);

        // precondition
        assertEquals("", obj1.getAttribute());

        // test
        final Transaction<IdMock, ?> modification = register1.startUpdateTransactionFor(obj1)
                .addOperation(reversibleTransformation(
                        IdMock::getAttribute,
                        IdMock::changeAttribute,
                        "potato"));
        final Optional<IdMock> opt2 = modification.commit();
        final IdMock obj2 = opt2.orElseThrow(REGISTER_EXCEPTION);
        assertEquals(obj1, obj2);
        assertEquals("potato", obj2.getAttribute());

        modification.rollback();
        assertEquals(obj1, obj2);
        assertEquals("", obj1.getAttribute());
    }

    @Test
    void testStorageSaveAndDeleteNonNull() {
        // test
        assertThrows(NullPointerException.class, () -> register1.setStorageSave(null));
        assertThrows(NullPointerException.class, () -> register1.setStorageDelete(null));
    }

    @Test
    void testStorageMethods() throws Throwable {
        // setup
        final List<IdMock> objs = new ArrayList<>();
        register1.setStorageSave(objs::add);
        register1.setStorageDelete(objs::remove);
        final Optional<IdMock> opt1 = register1.startCreationTransaction().commit();
        final Optional<IdMock> opt2 = register1.startCreationTransaction().commit();
        final Optional<IdMock> opt3 = register1.startCreationTransaction().commit();
        Transaction<IdMock, ?> removalOf2 = register1.startRemovalTransactionFor(opt2.orElseThrow(REGISTER_EXCEPTION));
        removalOf2.commit();

        // precondition
        assertTrue(opt1.isPresent());
        assertTrue(opt3.isPresent());

        // test
        final List<IdMock> internalObjects = register1.getElements();
        assertEquals(2, internalObjects.size());
        assertEquals(objs.get(0), opt1.orElseThrow(REGISTER_EXCEPTION));
        assertEquals(objs.get(1), opt3.orElseThrow(REGISTER_EXCEPTION));
        assertEquals(objs.get(0), internalObjects.get(0));
        assertEquals(objs.get(1), internalObjects.get(1));
    }

    @Test
    void testCreationAndRemovalWithRollbackInReverseOrder() throws Throwable {
        // setup
        final Transaction<IdMock, ?> creation = register1.startCreationTransaction();
        final IdMock m1 = creation.commit().orElseThrow(REGISTER_EXCEPTION);
        final Transaction<IdMock, ?> removal = register1.startRemovalTransactionFor(m1);
        removal.commit();

        // precondition
        assertEquals(0, register1.usedSpace());
        assertEquals(1, register1.freeSpace());

        // test
        removal.rollback();
        assertEquals(1, register1.usedSpace());
        assertEquals(0, register1.freeSpace());
        assertEquals(m1, register1.getElementById(0).orElseThrow(REGISTER_EXCEPTION));

        creation.rollback();
        assertEquals(0, register1.usedSpace());
        assertEquals(1, register1.freeSpace());
    }

    @Test
    void testRegisterWithExistingData() {
        // setup
        final  List<IdMock> existing = new ArrayList<>();
        existing.add(new IdMock(0));
        existing.add(new IdMock(3));
        existing.add(new IdMock(1));
        final Register<IdMock> register = new Register<>(IdMock::new, existing);

        // precondition
        assertEquals(1, register.freeSpace());
        assertEquals(3, register.usedSpace());
        assertEquals(4, register.totalSpace());

        // test
        final Optional<IdMock> nextOpt = register.startCreationTransaction().commit();
        assertTrue(nextOpt.isPresent());
        assertEquals(2, nextOpt.get().id());
    }
}
