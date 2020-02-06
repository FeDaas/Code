package de.moviemanager.core.json;

import java.util.Map;

import de.moviemanager.core.storage.JsonAttr;

class Mock {
    @JsonAttr final String name;
    @JsonAttr final int attribute;

    Mock(final String name, final int attribute) {
        this.name = name;
        this.attribute = attribute;
    }

    static class SubMock extends Mock {
        @JsonAttr final double subAttribute;

        SubMock(final String name,
                       final int attribute,
                       final double subAttribute) {
            super(name, attribute);
            this.subAttribute = subAttribute;
        }
    }

    static class MockFromJsonObject extends FromJsonObject<Mock> {
        MockFromJsonObject() {
            super(MockFromJsonObject::fromMap);
        }

        private static Mock fromMap(Map<String, Object> map) {
            String name = (String) map.get("name");
            Integer attribute = (Integer) map.get("attribute");
            return new Mock(name, attribute);
        }
    }

    static class SubMockFromJsonObject extends FromJsonObject<SubMock> {
        SubMockFromJsonObject() {
            super(SubMockFromJsonObject::fromMap);
        }

        private static SubMock fromMap(Map<String, Object> map) {
            String name = (String) map.get("name");
            Integer attribute = (Integer) map.get("attribute");
            Double subAttribute = (Double) map.get("subAttribute");
            return new SubMock(name, attribute, subAttribute);
        }
    }
}
