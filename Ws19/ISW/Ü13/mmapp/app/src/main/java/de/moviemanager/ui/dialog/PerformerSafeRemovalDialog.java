package de.moviemanager.ui.dialog;

import androidx.fragment.app.FragmentActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import de.moviemanager.R;
import de.moviemanager.core.storage.RuntimeStorageAccess;
import de.moviemanager.data.Movie;
import de.moviemanager.data.Performer;
import de.moviemanager.data.proxy.PersistentProxy;
import de.moviemanager.data.proxy.PortrayableProxy;
import de.util.ObjectUtils;

import static de.moviemanager.ui.dialog.PortrayableListDialog.Mode.CONFIRMATION;

public class PerformerSafeRemovalDialog {
    private static final RuntimeStorageAccess STORAGE = RuntimeStorageAccess.getInstance();

    private PerformerSafeRemovalDialog() {
    }

    public static void showIfNecessary(final FragmentActivity context,
                                       final List<Performer> linkedPerformer,
                                       final Consumer<List<Performer>> positiveListener,
                                       final Runnable negativeListener,
                                       final Runnable defaultAction) {
        List<Performer> removed = shallBeRemoved(linkedPerformer);
        if (!removed.isEmpty()) {
            PortrayableListDialog.builder(context)
                    .setMode(CONFIRMATION)
                    .setTitle(R.string.warning)
                    .setMessage(R.string.warning_linked_performers_removal, removed.size())
                    .setData(removed)
                    .setPositive(R.string.yes, li -> positiveListener.accept(li.stream()
                            .filter(PortrayableProxy::isPersistent)
                            .map(PersistentProxy.class::cast)
                            .filter(ObjectUtils::nonNull)
                            .map(PersistentProxy::getSource)
                            .map(Performer.class::cast)
                            .collect(Collectors.toList())
                    ))
                    .setNegative(R.string.no, negativeListener)
                    .show();
        } else
            defaultAction.run();
    }

    private static List<Performer> shallBeRemoved(final List<Performer> linkedPerformer) {
        final List<Performer> removed = new ArrayList<>();
        for (Performer performer : linkedPerformer) {
            final List<Movie> movies = STORAGE.getLinkedMoviesOfPerformer(performer);
            if (movies.size() == 1 && STORAGE.isLinked(movies.get(0), performer)) {
                removed.add(performer);
            }
        }

        return removed;
    }
}
