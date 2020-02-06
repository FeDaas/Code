package de.moviemanager.data;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class PerformerTest {
    @Test
    void testEquality() {
        final Performer p1 = createNewPerformer(0, "Performer1");
        final Performer p2 = createNewPerformer(0, "Performer1");
        final Map<Performer, Integer> m1 = new HashMap<>();
        m1.put(p1, 3);

        // precondition
        assertEquals(p1, p2);

        // test
        assertNotNull(m1.get(p1));
        assertNotNull(m1.get(p2));
    }

    private Performer createNewPerformer(int id, String name) {
        final Performer performer = new Performer(id);
        performer.setName(name);
        return performer;
    }
}
