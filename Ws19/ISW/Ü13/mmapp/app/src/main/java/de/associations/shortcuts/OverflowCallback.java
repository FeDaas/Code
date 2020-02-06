package de.associations.shortcuts;

import java.util.function.BiConsumer;

import de.associations.BidirectionalAssociationSet.OverflowPolicy;
import de.util.Pair;

@FunctionalInterface
public interface OverflowCallback <A, B> extends BiConsumer<Pair<A, B>, OverflowPolicy> {
}
