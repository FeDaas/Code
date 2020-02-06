package de.wiki.query;

import java.util.HashMap;
import java.util.Map;

class URLParameterBuilder {
    private final String name;
    private final Map<String, String> attributes;

    URLParameterBuilder(String name) {
        this.name = name;
        this.attributes = new HashMap<>();
    }

    URLParameterBuilder addAttribute(String key, String value) {
        this.attributes.put(key, value);
        return this;
    }

    URLParameter build(){
        URLParameter res = new URLParameter(name);
        attributes.forEach(res::putAttribute);
        return res;
    }
}
