package de.moviemanager.ui.masterlist;

public enum OrderState {
    DESCENDING, NEUTRAL, ASCENDING;

    public OrderState swap() {
        if(this == DESCENDING) {
            return ASCENDING;
        } else if(this == NEUTRAL) {
            return NEUTRAL;
        } else {
            return DESCENDING;
        }
    }
}
