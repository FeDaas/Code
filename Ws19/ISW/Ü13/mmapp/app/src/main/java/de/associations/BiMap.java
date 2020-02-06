package de.associations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import de.associations.shortcuts.IdMapper;
import de.util.Pair;
import de.util.Traits;
import de.util.annotations.Trait;

import static de.util.Pair.paired;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class BiMap<K1, K2> implements AssociationMatrix<K1, K2> {
    private static final Traits TRAITS = new Traits(BiMap.class);
    private final Map<K1, List<K2>> columns;
    private final Map<K2, List<K1>> rows;

    BiMap() {
        this.columns = new HashMap<>();
        this.rows = new HashMap<>();
    }

    @Override
    public void add(K1 k1, K2 k2) {
        safeAdd(accessColumnFor(k1), k2);
        safeAdd(accessRowFor(k2), k1);
    }

    @Override
    public int sizeOfNonEmptyRows(K1 col) {
        return accessColumnFor(col).size();
    }

    @Override
    public int sizeOfNonEmptyColumns(K2 row) {
        return accessRowFor(row).size();
    }

    private List<K2> accessColumnFor(K1 k) {
        columns.computeIfAbsent(k, x -> new ArrayList<>());
        return columns.get(k);
    }

    private List<K1> accessRowFor(K2 k) {
        rows.computeIfAbsent(k, x -> new ArrayList<>());
        return rows.get(k);
    }

    private <S> void safeAdd(List<S> li, S elem) {
        if (li.contains(elem))
            return;
        li.add(elem);
    }

    @Override
    public void remove(K1 k1, K2 k2) {
        ofNullable(columns.get(k1)).ifPresent(li -> li.remove(k2));
        ofNullable(rows.get(k2)).ifPresent(li -> li.remove(k1));
    }

    @Override
    public void removeColumn(K1 k) {
        columns.remove(k);
    }

    @Override
    public void removeRow(K2 k) {
        rows.remove(k);
    }

    public Optional<List<K1>> getRow(final K2 row) {
        return ofNullable(rows.get(row)).map(Collections::unmodifiableList);
    }

    public Optional<List<K2>> getColumn(K1 column) {
        return ofNullable(columns.get(column)).map(Collections::unmodifiableList);
    }

    public List<Pair<Integer, Integer>> getIdPairs(IdMapper<K1> m1, IdMapper<K2> m2) {
        return columns.entrySet()
                .stream()
                .map(Pair::paired)
                .map(p -> paired(m1.apply(p.first), p.second))
                .flatMap(p -> createIdPairs(p, m2))
                .collect(toList());
    }

    private Stream<Pair<Integer, Integer>> createIdPairs(final Pair<Integer, List<K2>> pair,
                                                         final IdMapper<K2> toId) {
        return pair.getSecond()
                .stream()
                .map(toId)
                .map(id -> paired(pair.first, id));
    }

    public void clear() {
        columns.clear();
        rows.clear();
    }

    @Override
    public boolean equals(Object obj) {
        return TRAITS.testEqualityBetween(this, obj);
    }

    @Trait
    private Map<K1, Set<K2>> getTransformedColumns() {
        return transformMap(columns);
    }

    @Trait
    private Map<K2, Set<K1>> getTransformedRows() {
        return transformMap(rows);
    }

    private static <A, B> Map<A, Set<B>> transformMap(Map<A, List<B>> m) {
        final Function<Map.Entry<A, List<B>>, A> keyIdentity = Map.Entry::getKey;
        final Function<Map.Entry<A, List<B>>, Set<B>> toSet = e -> new HashSet<>(e.getValue());
        return m.entrySet()
                .stream()
                .collect(toMap(keyIdentity, toSet));

    }

    @Override
    public int hashCode() {
        return TRAITS.createHashCodeFor(this);
    }
}
