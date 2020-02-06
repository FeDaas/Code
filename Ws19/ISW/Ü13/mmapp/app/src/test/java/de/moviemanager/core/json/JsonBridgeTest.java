package de.moviemanager.core.json;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import de.moviemanager.core.json.Mock.MockFromJsonObject;
import de.moviemanager.core.json.Mock.SubMock;
import de.moviemanager.core.json.Mock.SubMockFromJsonObject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonBridgeTest {

    @Test
    void testToJson() throws JSONException {
        // setup
        final String name = "mock";
        final int attribute = 2;
        final Mock mock = new Mock(name, attribute);
        final Optional<JSONObject> object = JsonBridge.toJson(mock);

        // precondition
        assertTrue(object.isPresent());
        JSONObject json = object.get();

        // test
        assertEquals(name, json.getString("name"));
        assertEquals(attribute, json.getInt("attribute"));
    }

    @Test
    void testMockFromJson() {
        // setup
        final String name = "mock";
        final int attribute = 2;
        final Mock mock = new Mock(name, attribute);
        final Optional<JSONObject> object = JsonBridge.toJson(mock);

        // precondition
        assertTrue(object.isPresent());
        JSONObject json = object.get();

        // test
        final Optional<Mock> optional = JsonBridge.fromJson(json, MockFromJsonObject::new);
        assertTrue(optional.isPresent());
        final Mock parsed = optional.get();
        assertEquals(name, parsed.name);
        assertEquals(attribute, parsed.attribute);
    }

    @Test
    void testToJsonWithSubclass() throws JSONException {
        // setup
        final String name = "mock";
        final int attribute = 2;
        final double subAttribute = 3.145;
        final SubMock mock = new SubMock(name, attribute, subAttribute);
        final Optional<JSONObject> object = JsonBridge.toJson(mock);

        // precondition
        assertTrue(object.isPresent());
        JSONObject json = object.get();

        // test
        assertEquals(name, json.getString("name"));
        assertEquals(attribute, json.getInt("attribute"));
        assertEquals(subAttribute, json.getDouble("subAttribute"));
    }

    @Test
    void testSubMockFromJson() {
        // setup
        final String name = "mock";
        final int attribute = 2;
        final double subAttribute = 3.145;
        final SubMock mock = new SubMock(name, attribute, subAttribute);
        final Optional<JSONObject> object = JsonBridge.toJson(mock);

        // precondition
        assertTrue(object.isPresent());
        JSONObject json = object.get();

        // test
        final Optional<SubMock> optional = JsonBridge.fromJson(json, SubMockFromJsonObject::new);
        assertTrue(optional.isPresent());
        final SubMock parsed = optional.get();
        assertEquals(name, parsed.name);
        assertEquals(attribute, parsed.attribute);
        assertEquals(subAttribute, parsed.subAttribute);
    }
}
