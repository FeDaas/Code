package de.associations.shortcuts;

import java.util.function.BiConsumer;

import de.associations.BidirectionalAssociationSet.UnderflowPolicy;
import de.util.Pair;

@FunctionalInterface
public interface UnderflowCallback <A, B> extends BiConsumer<Pair<A, B>, UnderflowPolicy> {
}
