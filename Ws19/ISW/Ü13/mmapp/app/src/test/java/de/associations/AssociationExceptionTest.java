package de.associations;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class AssociationExceptionTest {
    @Test
    void testMessageConstructor() {
        // setup
        final String message = "This is a sample message!";
        final AssociationException exception = new AssociationException(message);

        // test
        assertNull(exception.getCause());
        assertEquals(message, exception.getMessage());
    }

    @Test
    void testThrowableConstructor() {
        // setup
        final String causeMessage = "This is a cause sample message!";
        final IllegalStateException cause = new IllegalStateException(causeMessage);
        final AssociationException exception = new AssociationException(cause);

        // test
        assertNotNull(exception.getCause());
        assertEquals(cause, exception.getCause());
        assertEquals(causeMessage, exception.getCause().getMessage());
    }

    @Test
    void testMessageAndThrowableConstructor() {
        // setup
        final String message = "This is a sample message!";
        final String causeMessage = "This is a cause sample message!";
        final IllegalStateException cause = new IllegalStateException(causeMessage);
        final AssociationException exception = new AssociationException(message, cause);

        // test
        assertNotNull(exception.getCause());
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
        assertEquals(causeMessage, exception.getCause().getMessage());
    }
}
