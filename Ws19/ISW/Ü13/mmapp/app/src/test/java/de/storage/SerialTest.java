package de.storage;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import static java.util.stream.IntStream.range;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SerialTest {
    @AfterEach
    void tearDown() {
        range(-10, 10)
                .mapToObj(i -> new File("serobject_" + i + ".ser"))
                .forEach(File::delete);
    }

    @Test
    void testReadWrite() {
        // setup & precondition
        range(-10, 10).forEach(i -> {
            final File file = new File("serobject_" + i + ".ser");
            assertFalse(file.exists());
            Serial.write(file, i);
            assertTrue(file.exists());
        });

        // test
        range(-10, 10).forEach(i -> {
            File file = new File("serobject_" + i + ".ser");
            Integer object = Serial.read(file);
            assertEquals(i, object);
        });
    }

    @Test
    void testMultipleWrites() {
        // setup
        final File file = new File("serobject_" + 5 + ".ser");

        // precondition
        assertFalse(file.exists());

        // test
        Serial.write(file, 5);
        Serial.write(file, 7);
        Serial.write(file, 1023);
        Integer readObject = Serial.read(file);
        assertEquals(1023, readObject);
    }

    @Test
    void testReadWriteIOFailure() {
        // setup
        final Class<StorageException> expectedType = StorageException.class;
        final Executable writeExec = () -> Serial.write(new File("serobject_0.ser"), new WriteMock());
        final String writeMsg = "Forced write failure";

        // test
        assertThrows(expectedType, writeExec, writeMsg);
    }

    @Test
    void testReadIOFailure() {
        // setup
        final Class<StorageException> expectedType = StorageException.class;
        final File file2 = new File("serobject_1.ser");
        Serial.write(file2, new ReadMock());
        final Executable readExec = () -> Serial.read(file2);
        final String readMsg = "Forced read failure";

        // test
        assertThrows(expectedType, readExec, readMsg);
    }
}

class WriteMock implements Serializable {

    private static final long serialVersionUID = 1L;

    private void writeObject(ObjectOutputStream oos) throws IOException {
        throw new IOException("Forced write failure");
    }

}

class ReadMock implements Serializable {

    private static final long serialVersionUID = 1L;

    private void readObject(ObjectInputStream in) throws IOException {
        throw new IOException("Forced read failure");
    }
}
