package de.wiki.query;

enum QueryResultFormat {
    XML, JSON;

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
