package de.associations;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.util.Pair;

import static de.associations.BidirectionalAssociationSet.OverflowPolicy.THROW;
import static de.associations.BidirectionalAssociationSet.UnderflowPolicy.IGNORE;
import static org.junit.jupiter.api.Assertions.assertEquals;

class RuleViolationCallbacksTest {
    private static final int FORWARD_UNDERFLOW = 0;
    private static final int FORWARD_OVERFLOW = 1;
    private static final int BACKWARD_UNDERFLOW = 2;
    private static final int BACKWARD_OVERFLOW = 3;

    private boolean[] flags;
    private RuleViolationCallbacks<Integer, Integer> callbacks;
    private Pair<Integer, Integer> samplePair;

    @BeforeEach
    void setup() {
        resetFlags();
        callbacks = new RuleViolationCallbacks<>();
        samplePair = Pair.paired(42, 69);
    }

    @AfterEach
    void resetFlags() {
        flags = new boolean[]{false, false, false, false};
    }

    @Test
    void testDefaultForwardUnderflow() {
        // precondition
        assertFlags(false, false, false, false);

        // test
        callbacks.onForwardUnderflow(samplePair, IGNORE);

        // postcondition
        assertFlags(false, false, false, false);
    }

    private void assertFlags(boolean... expected) {
        assertEquals(4, expected.length);
        assertEquals(4, flags.length);
        assertEquals(expected[FORWARD_UNDERFLOW], flags[FORWARD_UNDERFLOW]);
        assertEquals(expected[FORWARD_OVERFLOW], flags[FORWARD_OVERFLOW]);
        assertEquals(expected[BACKWARD_UNDERFLOW], flags[BACKWARD_UNDERFLOW]);
        assertEquals(expected[BACKWARD_OVERFLOW], flags[BACKWARD_OVERFLOW]);
    }

    @Test
    void testDefaultForwardOverflow() {
        // precondition
        assertFlags(false, false, false, false);

        // test
        callbacks.onForwardOverflow(samplePair, THROW);

        // postcondition
        assertFlags(false, false, false, false);
    }

    @Test
    void testDefaultBackwardUnderflow() {
        // precondition
        assertFlags(false, false, false, false);

        // test
        callbacks.onBackwardUnderflow(samplePair, IGNORE);

        // postcondition
        assertFlags(false, false, false, false);
    }

    @Test
    void testDefaultBackwardOverflow() {
        // precondition
        assertFlags(false, false, false, false);

        // test
        callbacks.onBackwardOverflow(samplePair, THROW);

        // postcondition
        assertFlags(false, false, false, false);
    }

    @Test
    void testForwardUnderflowIgnore() {
        // setup
        callbacks.setForwardUnderflowCallback((p, u) -> flags[FORWARD_UNDERFLOW] = true);

        // precondition
        assertFlags(false, false, false, false);

        // test
        callbacks.onForwardUnderflow(samplePair, IGNORE);

        // postcondition
        assertFlags(true, false, false, false);
    }

    @Test
    void testForwardOverflowThrow() {
        // setup
        callbacks.setForwardOverflowCallback((p, u) -> flags[FORWARD_OVERFLOW] = true);

        // precondition
        assertFlags(false, false, false, false);

        // test
        callbacks.onForwardOverflow(samplePair, THROW);

        // postcondition
        assertFlags(false, true, false, false);
    }

    @Test
    void testBackwardUnderflowIgnore() {
        // setup
        callbacks.setBackwardUnderflowCallback((p, u) -> flags[BACKWARD_UNDERFLOW] = true);

        // precondition
        assertFlags(false, false, false, false);

        // test
        callbacks.onBackwardUnderflow(samplePair, IGNORE);

        // postcondition
        assertFlags(false, false, true, false);
    }

    @Test
    void testBackwardOverflowThrow() {
        // setup
        callbacks.setBackwardOverflowCallback((p, u) -> flags[BACKWARD_OVERFLOW] = true);

        // precondition
        assertFlags(false, false, false, false);

        // test
        callbacks.onBackwardOverflow(samplePair, THROW);

        // postcondition
        assertFlags(false, false, false, true);
    }

    @Test
    void testAllCallbacksWithoutReset() {
        // setup
        callbacks.setForwardUnderflowCallback((p, u) -> flags[FORWARD_UNDERFLOW] = true);
        callbacks.setForwardOverflowCallback((p, u) -> flags[FORWARD_OVERFLOW] = true);
        callbacks.setBackwardUnderflowCallback((p, u) -> flags[BACKWARD_UNDERFLOW] = true);
        callbacks.setBackwardOverflowCallback((p, u) -> flags[BACKWARD_OVERFLOW] = true);

        // precondition
        assertFlags(false, false, false, false);

        // test & postcondition
        callbacks.onForwardUnderflow(samplePair, IGNORE);
        assertFlags(true, false, false, false);

        callbacks.onForwardOverflow(samplePair, THROW);
        assertFlags(true, true, false, false);

        callbacks.onBackwardUnderflow(samplePair, IGNORE);
        assertFlags(true, true, true, false);

        callbacks.onBackwardOverflow(samplePair, THROW);
        assertFlags(true, true, true, true);
    }
}
