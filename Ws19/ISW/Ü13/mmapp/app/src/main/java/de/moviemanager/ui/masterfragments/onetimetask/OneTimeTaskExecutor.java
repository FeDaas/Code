package de.moviemanager.ui.masterfragments.onetimetask;

import java.util.ArrayList;
import java.util.List;

public class OneTimeTaskExecutor implements OneTimeTaskExecutorRudiment {
    private final List<OneTimeTask> oneTimeTasks = new ArrayList<>();

    @Override
    public void addOneTimeTask(final Runnable r) {
        oneTimeTasks.add(new OneTimeTask(r));
    }

    @Override
    public void runTasksOnceAndClear() {
        for(int i = oneTimeTasks.size() - 1; i >= 0; --i) {
            final OneTimeTask task = oneTimeTasks.get(i);
            task.decreaseProtectionTime();
            if(task.isReady()) {
                task.run();
                oneTimeTasks.remove(i);
            }
        }
    }
}
