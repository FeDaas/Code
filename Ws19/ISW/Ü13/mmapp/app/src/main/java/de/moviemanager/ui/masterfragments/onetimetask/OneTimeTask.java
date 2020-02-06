package de.moviemanager.ui.masterfragments.onetimetask;

public class OneTimeTask {
    private static final int DEFAULT_PROTECTION_TIME = 1;
    private final Runnable action;
    private int protectionTime;

    OneTimeTask(final Runnable action) {
        this(action, DEFAULT_PROTECTION_TIME);
    }

    private OneTimeTask(final Runnable action, final int protectionTime) {
        this.action = action;
        this.protectionTime = protectionTime;
    }

    boolean isReady() {
        return protectionTime <= 0;
    }

    void decreaseProtectionTime() {
        --protectionTime;
    }

    public void run() {
        action.run();
    }
}
