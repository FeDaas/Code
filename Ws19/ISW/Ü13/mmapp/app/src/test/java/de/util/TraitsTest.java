package de.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.util.mock.TraitMock;
import de.util.mock.TraitMock.SpecializedTraitMock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class TraitsTest {
    private TraitMock obj1;
    private TraitMock obj2;
    private TraitMock obj3;
    private SpecializedTraitMock objS1;
    
    @BeforeEach
    void setup() {
		obj1 = new TraitMock("obj1", 0, "potatoe");
		obj2 = new TraitMock("obj1", 0, "potatoe");
		obj3 = new TraitMock("obj3", 1, "lel");
		objS1 = new SpecializedTraitMock("obj1", 0, "potatoe");
    }
    
    @Test
    void testEqualsBaseBehaviour() {
		assertNotEquals(null, obj1);
		assertNotEquals("obj1", obj1);
		assertEquals(obj1, obj1);
		assertEquals(obj1, obj2);
		assertNotEquals(obj1, obj3);
    }
    
    @Test
    void testDynamicHashBaseBehaviour() {
		assertEquals(obj1.hashCode(), obj1.hashCode());
		assertEquals(obj1.hashCode(), obj2.hashCode());
		assertNotEquals(obj1.hashCode(), obj3.hashCode());
	
		obj1.setS("s");
		assertNotEquals(obj1.hashCode(), obj2.hashCode());
    }
    
    @Test
    void testAgainstSubclassing() {
		assertNotEquals(objS1, obj1);
		assertNotEquals(obj1, objS1);
		assertNotEquals(obj1.hashCode(), objS1.hashCode());
    }

    @Test
	void testEqualsAndHashCodeConsistency() {
    	// setup
		final Traits tr1 = new Traits(TraitMock.class);
		final Traits tr2 = new Traits(TraitMock.class);
		final Traits tr3 = new Traits(Traits.class);

		// test
		assertNotEquals(null, tr1);
		assertNotEquals(TraitMock.class, tr1);
		assertEquals(tr1, tr1);
		assertEquals(tr1.hashCode(), tr1.hashCode());
		assertEquals(tr1, tr2);
		assertEquals(tr1.hashCode(), tr2.hashCode());
		assertNotEquals(tr1, tr3);
		assertNotEquals(tr1.hashCode(), tr3.hashCode());
	}

	@Test
	void testToString() {
    	// setup
		final Traits tr1 = new Traits(TraitMock.class);
		final String clsName = TraitMock.class.getCanonicalName();
		final String fieldNames = "[i, name, s]";
		final String methodNames = "[createFancy, getDiff, getNameLength]";
		final String format = "Traits{cls=%s, fields=%s, methods=%s}";
		final String expected = String.format(format, clsName, fieldNames, methodNames);

		// test
		assertEquals(expected, tr1.toString());
	}

	@Test
	void testSubclassedObjects() {
    	// setup
    	SpecializedTraitMock obj1 = new SpecializedTraitMock("name", 0, "s1");
		SpecializedTraitMock obj2 = new SpecializedTraitMock("name", 0, "s1");
		SpecializedTraitMock obj3 = new SpecializedTraitMock("name", 0, "s3");

		// test
		assertEquals(obj1, obj2);
		assertNotEquals(obj1, obj3);
	}
}
