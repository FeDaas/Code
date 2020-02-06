package de.storage;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.IntFunction;

import de.util.Identifiable;
import de.util.ObjectUtils;
import de.util.operationflow.ReversibleOperations.ReversibleConsumer;
import de.util.operationflow.ReversibleOperations.ReversibleSupplier;
import de.util.operationflow.ReversibleTransaction;

import static de.util.operationflow.Transaction.beginReversibleTransaction;
import static java.util.Collections.unmodifiableList;
import static java.util.Comparator.comparing;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;

public class Register<T extends Identifiable> {
    private final IntFunction<T> constructor;
    private final List<T> elements;
    private final List<Integer> freeIds;
    private Consumer<T> storageSave;
    private Consumer<T> storageDelete;

    public Register(final IntFunction<T> constructor) {
        this(constructor, new ArrayList<>());
    }

    public Register(final IntFunction<T> constructor, final List<T> data) {
        this.constructor = constructor;

        final List<T> sortedData = data.stream()
                .sorted(comparing(Identifiable::id))
                .collect(toList());
        int maxID = sortedData.stream()
                .map(Identifiable::id)
                .max(Integer::compare)
                .orElse(-1);
        int size = maxID + 1;

        elements = range(0, size)
                .mapToObj(ObjectUtils::<T>typeSafeNull)
                .collect(toList());
        sortedData.forEach(d -> elements.set(d.id(), d));
        freeIds = range(0, size)
                .boxed()
                .filter(i -> elements.get(i) == null)
                .collect(toList());

        storageSave = x -> {
        };
        storageDelete = x -> {
        };
    }

    public void setStorageSave(final Consumer<T> storageSave) {
        this.storageSave = requireNonNull(storageSave);
    }

    public void setStorageDelete(final Consumer<T> storageDelete) {
        this.storageDelete = requireNonNull(storageDelete);
    }

    public ReversibleTransaction<T> startCreationTransaction() {
        final ReversibleSupplier<T> begin = createCreationBegin();
        final ReversibleConsumer<T> end = createCreationEnd();
        return beginReversibleTransaction(begin, end);
    }

    private ReversibleSupplier<T> createCreationBegin() {
        return new ReversibleSupplier<T>() {
            @Override
            public T forward() {
                return constructor.apply(getNextId());
            }

            @Override
            public void backward(T obj) {
                freeId(obj.id());
            }

            @NonNull
            @Override
            public String toString() {
                return "Begin";
            }
        };
    }

    private ReversibleConsumer<T> createCreationEnd() {
        return new ReversibleConsumer<T>() {
            private T createdObject;

            @Override
            public void forward(T obj) {
                this.createdObject = obj;
                addAndSaveToStorage(createdObject);
            }

            @Override
            public T backward() {
                removeAndRemoveFromStorage(createdObject);
                return createdObject;
            }

            @NonNull
            @Override
            public String toString() {
                return "End";
            }
        };
    }

    private void addAndSaveToStorage(T obj) {
        int index = obj.id();
        if (index >= elements.size())
            elements.add(index, obj);
        else
            elements.set(index, obj);
        storageSave.accept(obj);
    }

    public ReversibleTransaction<T> startUpdateTransactionFor(final T obj) {
        final ReversibleSupplier<T> begin = createUpdateBegin(obj);
        final ReversibleConsumer<T> end = createUpdateEnd();
        return beginReversibleTransaction(begin, end);
    }

    private ReversibleSupplier<T> createUpdateBegin(final T obj) {
        return new ReversibleSupplier<T>() {
            @Override
            public T forward() {
                return obj;
            }

            @Override
            public void backward(T obj) {
                update(obj);
            }
        };
    }

    private ReversibleConsumer<T> createUpdateEnd() {
        return new ReversibleConsumer<T>() {
            private T obj;

            @Override
            public void forward(T obj) {
                update(obj);
                this.obj = obj;
            }

            @Override
            public T backward() {
                return obj;
            }
        };
    }

    private void update(T obj) {
        storageSave.accept(obj);
    }

    public ReversibleTransaction<T> startRemovalTransactionFor(T obj) {
        final ReversibleSupplier<T> begin = createRemovalBegin(obj);
        final ReversibleConsumer<T> end = createRemovalEnd();
        return beginReversibleTransaction(begin, end);
    }

    private ReversibleSupplier<T> createRemovalBegin(final T obj) {
        return new ReversibleSupplier<T>() {

            @Override
            public T forward() {
                freeId(obj.id());
                removeAndRemoveFromStorage(obj);
                return obj;
            }

            @Override
            public void backward(final T obj) {
                addAndSaveToStorage(obj);
                consumeId(obj.id());
            }
        };
    }

    private void removeAndRemoveFromStorage(final T obj) {
        int id = obj.id();
        elements.set(id, null);
        storageDelete.accept(obj);
    }

    private ReversibleConsumer<T> createRemovalEnd() {
        return new ReversibleConsumer<T>() {
            private T removedObj;

            @Override
            public void forward(final T obj) {
                this.removedObj = obj;
            }

            @Override
            public T backward() {
                return this.removedObj;
            }
        };
    }

    private int getNextId() {
        if (freeIds.isEmpty()) {
            return elements.size();
        }
        return freeIds.remove(0);
    }

    private void freeId(int id) {
        freeIds.add(id);
    }

    private void consumeId(int id) {
        if (elements.get(id) != null)
            freeIds.remove((Integer) id);
    }

    public int usedSpace() {
        return totalSpace() - freeSpace();
    }

    public int totalSpace() {
        return this.elements.size();
    }

    public int freeSpace() {
        return this.freeIds.size();
    }

    public List<T> getElements() {
        return unmodifiableList(this.elements.stream()
                .filter(Objects::nonNull)
                .collect(toList()));
    }

    public Optional<T> getElementById(int id) {
        Optional<T> result;
        if (id < 0 || id >= elements.size()) {
            result = Optional.empty();
        } else {
            result = ofNullable(elements.get(id));
        }
        return result;
    }
}
