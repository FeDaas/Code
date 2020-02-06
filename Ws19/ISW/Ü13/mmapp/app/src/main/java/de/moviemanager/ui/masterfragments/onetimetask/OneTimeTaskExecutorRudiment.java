package de.moviemanager.ui.masterfragments.onetimetask;

public interface OneTimeTaskExecutorRudiment {
    void addOneTimeTask(final Runnable r);
    void runTasksOnceAndClear();
}
