package de.associations;

import de.associations.BidirectionalAssociationSet.OverflowPolicy;
import de.associations.BidirectionalAssociationSet.UnderflowPolicy;
import de.associations.shortcuts.OverflowCallback;
import de.associations.shortcuts.UnderflowCallback;
import de.util.Pair;

import static java.util.Optional.ofNullable;

public class RuleViolationCallbacks<L, R> {
    private UnderflowCallback<L, R> forwardUnderflowCallback;
    private OverflowCallback<L, R> forwardOverflowCallback;
    private UnderflowCallback<R, L> backwardUnderflowCallback;
    private OverflowCallback<R, L> backwardOverflowCallback;

    public RuleViolationCallbacks() {
        forwardUnderflowCallback = (entities, policy) -> {
        };
        forwardOverflowCallback = (entities, policy) -> {
        };
        backwardUnderflowCallback = (entities, policy) -> {
        };
        backwardOverflowCallback = (entities, policy) -> {
        };
    }

    public void setForwardUnderflowCallback(UnderflowCallback<L, R> c) {
        setForwardCallbacks(c, null);
    }

    public void setForwardOverflowCallback(OverflowCallback<L, R> c) {
        setForwardCallbacks(null, c);
    }

    private void setForwardCallbacks(UnderflowCallback<L, R> underflow,
                                     OverflowCallback<L, R> overflow) {
        ofNullable(underflow).ifPresent(u -> this.forwardUnderflowCallback = u);
        ofNullable(overflow).ifPresent(o -> this.forwardOverflowCallback = o);
    }

    public void setBackwardUnderflowCallback(UnderflowCallback<R, L> c) {
        setBackwardCallbacks(c, null);
    }

    public void setBackwardOverflowCallback(OverflowCallback<R, L> c) {
        setBackwardCallbacks(null, c);
    }

    private void setBackwardCallbacks(UnderflowCallback<R, L> underflow, OverflowCallback<R, L> overflow) {
        ofNullable(underflow).ifPresent(u -> this.backwardUnderflowCallback = u);
        ofNullable(overflow).ifPresent(o -> this.backwardOverflowCallback = o);
    }

    public void onForwardUnderflow(Pair<L, R> p, UnderflowPolicy e) {
        forwardUnderflowCallback.accept(p, e);
    }

    public void onForwardOverflow(Pair<L, R> p, OverflowPolicy e) {
        forwardOverflowCallback.accept(p, e);
    }

    public void onBackwardUnderflow(Pair<R, L> p, UnderflowPolicy e) {
        backwardUnderflowCallback.accept(p, e);
    }

    public void onBackwardOverflow(Pair<R, L> p, OverflowPolicy e) {
        backwardOverflowCallback.accept(p, e);
    }
}
