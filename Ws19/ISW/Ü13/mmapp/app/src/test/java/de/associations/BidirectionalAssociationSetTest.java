package de.associations;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import de.associations.BidirectionalAssociationSet.OverflowPolicy;
import de.associations.BidirectionalAssociationSet.UnderflowPolicy;
import de.associations.mock.Foo;
import de.associations.mock.Moo;
import de.util.Pair;

import static de.associations.BidirectionalAssociationSet.OverflowPolicy.IGNORE;
import static de.associations.BidirectionalAssociationSet.UnderflowPolicy.REMOVE_ASSOCIATION;
import static de.associations.BidirectionalAssociationSet.create;
import static de.util.Pair.paired;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BidirectionalAssociationSetTest {
    private BidirectionalAssociationSet<Foo, Moo> ap1, ap2, ap3, ap1_c;
    private BidirectionalAssociationSet<Integer, String> ap4;
    
    private Foo f1, f2;
    private Moo m1, m2, m3;
    private RuleViolationCallbacks<Foo, Moo> callback;
    
    private boolean[] flags = new boolean[4];
    private static final int FORWARD_UNDERFLOW_INDEX = 0;
    private static final int FORWARD_OVERFLOW_INDEX = 1;
    private static final int BACKWARD_UNDERFLOW_INDEX = 2;
    private static final int BACKWARD_OVERFLOW_INDEX = 3;
    
    @BeforeEach
    void init() {
		initPools();
	
		initFoos();
		initMoos();
	
		initFlags();
		initCallback();
    }
    
    private void initPools() {
		ap1 = create(Foo.class, Moo.class, "0..*", "1..*");
		ap1_c = create(Foo.class, Moo.class, "0..*", "1..*");
		ap2 = create(Foo.class, Moo.class, "0..1", "0..1");
		ap3 = create(Foo.class, Moo.class, "1..2", "1..1");
		ap4 = create(Integer.class, String.class, "0..*", "1..*");
    }
    
    private void initFoos() {
		f1 = new Foo("f1", 3);
		f2 = new Foo("f2", 2);
    }
    
    private void initMoos() {
		m1 = new Moo("Moo1", 1);
		m2 = new Moo("Moo2", 1);
		m3 = new Moo("Moo3", 1);
    }
    
    private void initFlags() {
		flags = new boolean[4];
    }
    
    private void initCallback() {
		callback = new RuleViolationCallbacks<>();
		callback.setBackwardUnderflowCallback((p, e) -> {
	    	flags[BACKWARD_UNDERFLOW_INDEX] = true;
	    	assertEquals(UnderflowPolicy.REMOVE_ASSOCIATION, e);
		});
		callback.setBackwardOverflowCallback((p, e) -> {
	    	flags[BACKWARD_OVERFLOW_INDEX] = true;
	    	assertEquals(OverflowPolicy.IGNORE, e);
		});
		callback.setForwardUnderflowCallback((p, e) -> {
	    	flags[FORWARD_UNDERFLOW_INDEX] = true;
	    	assertEquals(UnderflowPolicy.REMOVE_ASSOCIATION, e);
		});
		callback.setForwardOverflowCallback((p, e) -> {
	    	flags[FORWARD_OVERFLOW_INDEX] = true;
	    	assertEquals(OverflowPolicy.IGNORE, e);
		});
    }
    
    @AfterEach
    void tearDown() {
		Foo.resetIds();
		Moo.resetIds();
    }
    
    @Test
    void testOneFixedMinimumWithUnderflowPolicyIgnore() {
		ap1.setPolicy(UnderflowPolicy.IGNORE);
		ap1.associate(f1, m1);
	
		assertEquals(1, ap1.getAssociatedObjectsOfT1(f1).map(List::size).orElse(-1).intValue());
		assertEquals(1, ap1.getAssociatedObjectsOfT2(m1).map(List::size).orElse(-1).intValue());
	
		ap1.disassociate(f1, m1);
		assertEquals(1, ap1.getAssociatedObjectsOfT1(f1).map(List::size).orElse(-1).intValue());
		assertTrue(ap1.getAssociatedObjectsOfT2(m1).isPresent());
		assertEquals(1, ap1.getAssociatedObjectsOfT2(m1).get().size());
    }
    
    @Test
    void testOneFixedMinimumWithUnderflowPolicyThrow() {
		ap1.setPolicy(UnderflowPolicy.THROW);
	
		ap1.associate(f1, m1);
		ap1.associate(f1, m2);

		Class<RuntimeException> excClass = RuntimeException.class;
		String msg = "Can't disassociate Moo1 from f1";
		assertThrows(excClass, () -> ap1.disassociate(f1, m1), msg);
    }
    
    @Test
    void testOneFixedMinimumWithUnderflowPolicyRemoveAssociation() {
    	// setup
		ap1.setPolicy(UnderflowPolicy.REMOVE_ASSOCIATION);
		ap1.associate(f1, m1);

		// precondition
		assertEquals(1, ap1.getAssociatedObjectsOfT1(f1).map(List::size).orElse(-1).intValue());
		assertEquals(1, ap1.getAssociatedObjectsOfT2(m1).get().size());

		// test
		ap1.disassociate(f1, m1);
		assertEquals(0, ap1.getAssociatedObjectsOfT1(f1).map(List::size).orElse(-1).intValue());
		assertFalse(ap1.getAssociatedObjectsOfT2(m1).isPresent());
    }
    
    @Test
    void testFixedMaximumWithOverflowPolicyIgnore() {
    	// setup
		ap2.setPolicy(OverflowPolicy.IGNORE);
		ap2.associate(f1, m1);

		// precondition
		assertEquals(1, ap2.getAssociatedObjectsOfT1(f1)
				.map(List::size)
				.orElse(-1)
				.intValue()
		);
		assertEquals(1, ap2.getAssociatedObjectsOfT2(m1)
				.map(List::size)
				.orElse(-1)
				.intValue()
		);

		// test
		ap2.associate(f1, m2);
		assertEquals(1, ap2.getAssociatedObjectsOfT1(f1)
				.map(List::size)
				.orElse(-1)
				.intValue()
		);
		assertEquals(1, ap2.getAssociatedObjectsOfT2(m1)
				.map(List::size)
				.orElse(-1)
				.intValue()
		);
		assertFalse(ap2.getAssociatedObjectsOfT2(m2).isPresent());
    }
    
    @Test
    void testFixedMaximumWithOverflowPolicyThrow() {
    	// setup
		ap2.setPolicy(OverflowPolicy.THROW);
		ap2.associate(f1, m1);

		// test
		final Class<RuntimeException> excClass = RuntimeException.class;
		final String msg = "Can't associate f1 with Moo2";
		assertThrows(excClass, () -> ap2.associate(f1, m2), msg);
    }
    
    @Test
    void testLoadAndInsertOfMappedIds() {
    	// setup
		ap1.associate(f1, m1);
		ap1.associate(f1, m3);
		ap1.associate(f2, m2);
	
		final List<Pair<Integer, Integer>> expected = new ArrayList<>();
		expected.add(paired(0, 0));
		expected.add(paired(0, 2));
		expected.add(paired(1, 1));

		// test
		final List<Pair<Integer, Integer>> actual = ap1.getMappedAssociations(Foo::id, Moo::id);
		assertEquals(expected.size(), actual.size());
		for(int i = 0; i < expected.size(); ++i) {
	    	final Pair<Integer, Integer> obj = actual.get(i);
	    	assertTrue(expected.contains(obj));
	    	int firstIndex = expected.indexOf(obj);
	    	int lastIndex = expected.lastIndexOf(obj);
	    	assertEquals(firstIndex, lastIndex);
		}
	
		ap1_c.insertMappedAssociations(actual, Foo::getById, Moo::getById);
		assertEquals(ap1, ap1_c);
    }
    
    @Test
    void testDenialOfMultiAssociation() {
    	// setup
		ap1.associate(f1, m1);
		ap1.associate(f1, m1);

		// test
		assertEquals(1, ap1.getAssociatedObjectsOfT1(f1)
				.map(List::size)
				.orElse(-1)
				.intValue()
		);
		assertEquals(1, ap1.getAssociatedObjectsOfT2(m1)
				.map(List::size)
				.orElse(-1)
				.intValue()
		);
    }

	@Test
	void testCallbacks() {
    	// setup
		ap3.setCallback(callback);
		ap3.setPolicies(REMOVE_ASSOCIATION, IGNORE);

		// precondition
		assertFlags(false, false, false, false);

		// test
		ap3.associate(f1, m1);
		ap3.associate(f1, m2);
		ap3.associate(f1, m3);
		assertFlags(false, true, false, false);

		ap3.associate(f2, m1);
		assertFlags(false, true, false, true);

		ap3.disassociate(f1, m1);
		assertFlags(false, true, true, true);

		ap3.disassociate(f1, m2);
		assertFlags(true, true, true, true);
	}

    private void assertFlags(boolean... expected) {
    	// test
    	assertEquals(expected[FORWARD_UNDERFLOW_INDEX], flags[FORWARD_UNDERFLOW_INDEX]);
		assertEquals(expected[FORWARD_OVERFLOW_INDEX], flags[FORWARD_OVERFLOW_INDEX]);
		assertEquals(expected[BACKWARD_UNDERFLOW_INDEX], flags[BACKWARD_UNDERFLOW_INDEX]);
		assertEquals(expected[BACKWARD_OVERFLOW_INDEX], flags[BACKWARD_OVERFLOW_INDEX]);
	}
    
    @Test
    void testToString() {
		// test
		assertEquals("AssociationPool{Foo --> [0..*] Moo, Moo --> [1..*] Foo}", ap1.toString());
		assertEquals("AssociationPool{Foo --> [0..1] Moo, Moo --> [0..1] Foo}", ap2.toString());
    }
    
    @Test
	void testEquals() {
		// test
		assertNotEquals(null, ap1_c);
		assertNotEquals("potatoe", ap1_c);
		assertEquals(ap1_c, ap1_c);
	
		assertEquals(ap1_c, ap1);
		ap1.associate(f1, m1);
		assertNotEquals(ap1_c, ap1);
	
		ap4.associate(0, "potatoe");
		assertNotEquals(ap1, ap4);
    }
    
    @Test
	void testHashCode() {
		assertEquals(ap1.hashCode(), ap1.hashCode());
		assertEquals(ap1, ap1_c);
		assertEquals(ap1.hashCode(), ap1_c.hashCode());
		assertNotEquals(ap1.hashCode(), ap4.hashCode());
    }

    @Test
	void testTypes() {
		assertEquals(Foo.class, ap1.getLeftType());
		assertEquals(Moo.class, ap1.getRightType());
    	assertEquals(Integer.class, ap4.getLeftType());
		assertEquals(String.class, ap4.getRightType());
	}

	@Test
	void testDefaultCallbacks() {
		final BidirectionalAssociationSet<Foo, Moo> set = create(
				Foo.class,
				Moo.class,
				"1..2",
				"1..2"
		);

		set.associate(f1, m1);
		set.associate(f1, m2);
		assertThrows(AssociationException.class, () -> set.associate(f1, m3));
	}
}
