package de.associations;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

import de.associations.BidirectionalAssociationSet.OverflowPolicy;
import de.associations.BidirectionalAssociationSet.UnderflowPolicy;
import de.associations.shortcuts.IdMapper;
import de.associations.shortcuts.IdUnmapper;
import de.associations.shortcuts.OverflowCallback;
import de.associations.shortcuts.UnderflowCallback;
import de.util.Pair;
import de.util.Traits;
import de.util.annotations.Trait;
import de.util.operationflow.Transaction;

import static de.util.Pair.paired;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;


/**
 * This class stores bidirectional associations between objects of type T1 and T2.
 *
 * <p>
 * Because of the bidirectional association one could differentiate between
 * a forward (obj1 -> obj2) and a backward (obj1 <- obj2) direction.
 * For each direction a behaviour (see {@link AssociationBehaviour}) - a rule about
 * the minimum and maximum associated objects - must be defined.
 * </p>
 * <p>
 * <img
 * src="doc-files/bidirectional_association_set_scheme.png"
 * alt="scheme of bidirectional association inject"
 * width="800"
 * >
 *
 * <p>
 * These rules are then checked for each new insertion / deletion, if a rule is
 * violated the policy decides, what to do:
 * </p>
 * <ul>
 * <li>IGNORE<br>
 * the operation will be ignored, but callback is notified
 * </li>
 * <li>THROW<br>
 * a {@link RuntimeException} will be throwed (default)
 * </li>
 * <li>REMOVE_ASSOCIATION (underflow only)<br>
 * the assocation which can't have less objects associated will be deleted
 * and callback is notified
 * </li>
 * </ul>
 *
 * <p>
 * These operations only apply, if both directions can be executed, following chart
 * shows all combinations.
 * </p>
 * <p>
 * O: Both operations will be performed<br>
 * -: Both operations will canceled<br>
 * X: RuntimeExpection will be thrown<br>
 * <br>
 * <table style="text-align: center;">
 * <tr>
 * <th> forward \ backward </th>
 * <th>no problem</th>
 * <th>IGNORE</th>
 * <th>THROW</th>
 * <th>REMOVE_ASSOCIATION</th>
 * </tr>
 * <tr>
 * <td style="font-weight: bold;">no problem</td>
 * <td>O</td><td>-</td><td>X</td><td>O</td>
 * </tr>
 * <tr>
 * <td style="font-weight: bold;">IGNORE</td>
 * <td>-</td><td>-</td><td>X</td><td>-</td>
 * </tr>
 * <tr>
 * <td style="font-weight: bold;">THROW</td><td>X</td>
 * <td>X</td><td>X</td><td>X</td>
 * </tr>
 * <tr>
 * <td style="font-weight: bold;">REMOVE_ASSOCIATION</td>
 * <td>O</td><td>-</td><td>X</td><td>O</td>
 * </tr>
 * </table>
 *
 * <br><b>Author</b>: <a href="mailto:schustrchr@gmail.com">Ctoffer</a><br>
 *
 * @param <L> type of the left object in this association
 * @param <R> type of the right object in this association
 */
public class BidirectionalAssociationSet<L, R> {
    public enum UnderflowPolicy {
        IGNORE, THROW, REMOVE_ASSOCIATION
    }

    public enum OverflowPolicy {
        IGNORE, THROW
    }

    private static final Traits TRAITS = new Traits(BidirectionalAssociationSet.class);

    @Trait
    private final AssociationBehaviour<L, R> forwardRule;
    @Trait
    private final AssociationBehaviour<R, L> backwardRule;

    @Trait
    private final AssociationMatrix<L, R> associations;

    private final Class<L> leftType;
    private final Class<R> rightType;

    private List<LogicPartConcept<L, R>> associateLogic;
    private List<LogicPartConcept<L, R>> disassociateLogic;

    private OutOfBoundariesHandler<L, R> forwardHandler;
    private OutOfBoundariesHandler<R, L> backwardHander;

    private RuleViolationCallbacks<L, R> callback;

    private BidirectionalAssociationSet(Class<L> cls1, Class<R> cls2,
                                       String forwardRule, String backwardRule) {
        leftType = cls1;
        rightType = cls2;
        associations = new BiMap<>();

        this.forwardRule = new AssociationBehaviour<>(cls1, cls2,
                associations::sizeOfNonEmptyRows);
        this.backwardRule = new AssociationBehaviour<>(cls2, cls1,
                associations::sizeOfNonEmptyColumns);

        setBidirectionalRules(forwardRule, backwardRule);

        createForwardHandler();
        createBackwardHandler();

        createAssociateLogic();
        createDisassociateLogic();

        setCallback(new RuleViolationCallbacks<>());
    }

    private void setBidirectionalRules(String forwardRule, String backwardRule) {
        this.forwardRule.setBoundaries(forwardRule);
        this.backwardRule.setBoundaries(backwardRule);
    }

    private void createForwardHandler() {
        forwardHandler = new OutOfBoundariesHandler<>();
        forwardHandler.setPolicies(UnderflowPolicy.THROW, OverflowPolicy.THROW);
        forwardHandler.setCallback((p, e) -> { }, (p, e) -> { });
        forwardHandler.setOperation(associations::removeColumn);
    }

    private void createBackwardHandler() {
        backwardHander = new OutOfBoundariesHandler<>();
        backwardHander.setPolicies(UnderflowPolicy.THROW, OverflowPolicy.THROW);
        backwardHander.setCallback((p, e) -> {}, (p, e) -> { });
        backwardHander.setOperation(associations::removeRow);
    }

    private void createDisassociateLogic() {
        final LogicPart<L, R> d1 = new LogicPart<>();
        d1.setRuleEvaluation(AssociationBehaviour::canRemove);
        d1.setRule(forwardRule);
        d1.setHandler(forwardHandler::onUnderflow);

        final LogicPart<R, L> d2 = new LogicPart<>();
        d2.setRuleEvaluation(AssociationBehaviour::canRemove);
        d2.setRule(backwardRule);
        d2.setHandler(backwardHander::onUnderflow);

        disassociateLogic = asList(d1, d2.swapEntities());
    }

    private void createAssociateLogic() {
        final LogicPart<L, R> d1 = new LogicPart<>();
        d1.setRuleEvaluation(AssociationBehaviour::canAppend);
        d1.setRule(forwardRule);
        d1.setHandler(forwardHandler::onOverflow);

        final LogicPart<R, L> d2 = new LogicPart<>();
        d2.setRuleEvaluation(AssociationBehaviour::canAppend);
        d2.setRule(backwardRule);
        d2.setHandler(backwardHander::onOverflow);

        associateLogic = asList(d1, d2.swapEntities());
    }

    private Pair<UnderflowPolicy, OverflowPolicy> getPolicies() {
        return forwardHandler.getPolicies();
    }

    void setPolicy(UnderflowPolicy u) {
        forwardHandler.setPolicy(u);
        backwardHander.setPolicy(u);
    }

    void setPolicy(OverflowPolicy o) {
        forwardHandler.setPolicy(o);
        backwardHander.setPolicy(o);
    }

    private void setPolicies(Pair<UnderflowPolicy, OverflowPolicy> p) {
        setPolicies(p.getFirst(), p.getSecond());
    }

    public void setPolicies(UnderflowPolicy uPolicy, OverflowPolicy oPolicy) {
        forwardHandler.setPolicies(uPolicy, oPolicy);
        backwardHander.setPolicies(uPolicy, oPolicy);
    }

    public void setCallback(RuleViolationCallbacks<L, R> callback) {
        this.callback = callback;
        forwardHandler.setCallback(callback::onForwardUnderflow, callback::onForwardOverflow);
        backwardHander.setCallback(callback::onBackwardUnderflow, callback::onBackwardOverflow);
    }

    public void associate(final L obj1, final R obj2) {
        if (executeLogic(associateLogic, obj1, obj2))
            associations.add(obj1, obj2);
    }

    public void disassociate(final L obj1, final R obj2) {
        if (executeLogic(disassociateLogic, obj1, obj2))
            associations.remove(obj1, obj2);
    }

    private boolean executeLogic(final List<LogicPartConcept<L, R>> logic, final L obj1, final R obj2) {
        final Transaction<Void, Runnable> transaction = Transaction.beginSimpleTransaction();

        for (final LogicPartConcept<L, R> logicPart : logic) {
            logicPart.executeWith(paired(obj1, obj2), transaction);

            if (transaction.wasAborted()) {
                return false;
            }
        }
        transaction.commit();
        return true;
    }

    public Optional<List<R>> getAssociatedObjectsOfT1(L obj) {
        return associations.getColumn(obj);
    }

    public Optional<List<L>> getAssociatedObjectsOfT2(R obj) {
        return associations.getRow(obj);
    }

    /**
     * See {@link BiMap#getIdPairs(IdMapper, IdMapper)}
     *
     * @param m1 id mapper for left objects
     * @param m2 id mapper for right objects
     * @return
     */
    public List<Pair<Integer, Integer>> getMappedAssociations(IdMapper<L> m1, IdMapper<R> m2) {
        return associations.getIdPairs(m1, m2);
    }

    /**
     * This function bidirectionally associates for each given Pair (id(obj1), id(obj2))
     * the underlying objects. For this method the under-/ overflow policies and there
     * respective callbacks are stored, inject to THROW and after the insertion process
     * restored. So this method will always throw a {@link RuntimeException} if the data
     * violates the internal rules.
     *
     * @param data    - data which should be stored in the pool
     * @param mapper1 - maps an id to an object of type T1
     * @param mapper2 - maps an id to an object of type T2
     */
    public void insertMappedAssociations(final List<Pair<Integer, Integer>> data,
                                         final IdUnmapper<L> mapper1, final IdUnmapper<R> mapper2) {
        Pair<UnderflowPolicy, OverflowPolicy> oldPolicies = getPolicies();
        RuleViolationCallbacks<L, R> oldCallback = this.callback;

        setCallback(new RuleViolationCallbacks<>());
        setPolicies(UnderflowPolicy.THROW, OverflowPolicy.THROW);

        rawInsertAssociations(data, mapper1, mapper2);

        setPolicies(oldPolicies);
        setCallback(oldCallback);
    }

    private void rawInsertAssociations(final List<Pair<Integer, Integer>> data,
                                       final IdUnmapper<L> mapper1, final IdUnmapper<R> mapper2) {
        data.forEach(pair -> {
            final L obj1 = mapper1.apply(pair.getFirst());
            final R obj2 = mapper2.apply(pair.getSecond());
            associate(obj1, obj2);
        });
    }

    public Class<L> getLeftType() {
        return leftType;
    }

    public Class<R> getRightType() {
        return rightType;
    }

    @Override
    public String toString() {
        return "AssociationPool{" + forwardRule + ", " + backwardRule + "}";
    }

    @Override
    public boolean equals(Object obj) {
        return TRAITS.testEqualityBetween(this, obj);
    }

    @Override
    public int hashCode() {
        return TRAITS.createHashCodeFor(this);
    }

    public static <T1, T2> BidirectionalAssociationSet<T1, T2> create(Class<T1> cls1,
                                                                      Class<T2> cls2,
                                                                      String b1, String b2) {
        return new BidirectionalAssociationSet<>(cls1, cls2, b1, b2);
    }
}

/**
 * This class handles the case, if the rule between source and target
 * through an operation like {@link BidirectionalAssociationSet#associate(Object, Object)}
 * or {@link BidirectionalAssociationSet#disassociate(Object, Object)} is not satisfied.
 *
 * <p> This class could be easily seperated into two classes:
 * <ul>
 * <li>handling of underflow</li>
 * <li>handling of overflow</li>
 * </ul>
 * But to reduce the class hierarchy and number of attributes in {@link BidirectionalAssociationSet}
 * this seperation was not done. So this class handles under- and overflow
 * for a single direction.
 *
 * <br><b>Author</b>: <a href="mailto:schustrchr@gmail.com">Ctoffer</a><br>
 *
 * @param <S> - source type of the association
 * @param <T> - target type of the association
 */
class OutOfBoundariesHandler<S, T> {
    private UnderflowPolicy uPolicy;
    private OverflowPolicy oPolicy;
    private UnderflowCallback<S, T> uCallback;
    private OverflowCallback<S, T> oCallback;
    private BiConsumer<S, T> uOperation;

    /**
     * Initializes a new handler with following defaults:
     * <ul>
     * <li><tt>uPolicy</tt>: {@link UnderflowPolicy#THROW}</li>
     * <li><tt>oPolicy</tt>: {@link OverflowPolicy#THROW}</li>
     * <li><tt>uCallback</tt>: (s, t) -> {}</tt></li>
     * <li><tt>oCallback</tt>: (s, t) -> {}</tt></li>
     * <li><tt>uOperation</tt>: (s, t) -> {}</tt></li>
     * </ul>
     */
    OutOfBoundariesHandler() {
        uPolicy = UnderflowPolicy.THROW;
        oPolicy = OverflowPolicy.THROW;
        uCallback = (s, t) -> {};
        oCallback = (s, t) -> {};
        uOperation = (s, t) -> {};
    }

    /**
     * Sets the given {@link UnderflowPolicy} to the current one.
     *
     * @param u - new policy must be non-<tt>null</tt>
     */
    void setPolicy(UnderflowPolicy u) {
        this.uPolicy = requireNonNull(u);
    }

    void setPolicy(OverflowPolicy o) {
        this.oPolicy = o;
    }

    void setPolicies(UnderflowPolicy u, OverflowPolicy o) {
        this.uPolicy = u;
        this.oPolicy = o;
    }

    Pair<UnderflowPolicy, OverflowPolicy> getPolicies() {
        return paired(uPolicy, oPolicy);
    }

    void setCallback(UnderflowCallback<S, T> uCallback, OverflowCallback<S, T> oCallback) {
        this.uCallback = uCallback;
        this.oCallback = oCallback;
    }

    void setOperation(Consumer<S> uOperation) {
        setOperation((a, b) -> uOperation.accept(a));
    }

    private void setOperation(BiConsumer<S, T> uOperation) {
        this.uOperation = uOperation;
    }

    /**
     * Models the procedure which should be executed, if less
     * objects than allowed should be associated.
     * This option can be triggered during the removal of an existing
     * association.
     *
     * @param p
     * @param t
     */
    void onUnderflow(Pair<S, T> p, Transaction<Void, Runnable> t) {
        final S obj1 = p.getFirst();
        final T obj2 = p.getSecond();

        switch (uPolicy) {
            case REMOVE_ASSOCIATION:
                t.addOperation(() -> {
                    uOperation.accept(obj1, obj2);
                    uCallback.accept(paired(obj1, obj2), uPolicy);
                });
                break;
            case IGNORE:
                t.abort();
                uCallback.accept(paired(obj1, obj2), uPolicy);
                break;
            default:
                throw new AssociationException("Underflow occurred with " + p);
        }
    }

    /**
     * Models the procedure which should be executed, if more
     * objects than allowed should be associated.
     * This option can be triggered during the creation of an existing
     * association.
     *
     * @param p
     * @param t
     */
    void onOverflow(Pair<S, T> p, Transaction<Void, Runnable> t) {
        final S obj1 = p.getFirst();
        final T obj2 = p.getSecond();

        if (oPolicy == OverflowPolicy.IGNORE) {
            t.abort();
            oCallback.accept(paired(obj1, obj2), oPolicy);
            return;
        }
        throw new AssociationException("Overflow occurred with " + p);
    }
}

/**
 * Basic interface for modeling a part of the association / disassociation logic.
 *
 * <br><b>Author</b>: <a href="mailto:schustrchr@gmail.com">Ctoffer</a><br>
 *
 * @param <S>
 * @param <T>
 */
@FunctionalInterface
interface LogicPartConcept<S, T> {
    /**
     * Executes the logic with a concrete entity pair. The given Transaction
     * is still under construction and should be modified by the algorithm.
     * If a critical error occurs the Transaction should be aborted, new operations
     * should be added.
     *
     * @param entities
     * @param transaction
     */
    void executeWith(Pair<S, T> entities, Transaction<Void, Runnable> transaction);

    default LogicPartConcept<T, S> swapEntities() {
        return (p, t) -> this.executeWith(p.swapArgs(), t);
    }
}

/**
 * This class models a procedure with following properties:
 * Given obj1, obj2
 * if obj1 supports the binding action:
 * put binding action with obj1 & obj2 in transaction
 * else:
 * let handler decide what to do
 * <p>
 * A binding action could be removal of an existing association or
 * the creation of a new one.
 * All attributes must be inject via dependency injection before
 * this class can be used.
 *
 * <br><b>Author</b>: <a href="mailto:schustrchr@gmail.com">Ctoffer</a><br>
 *
 * @param <S> type of source object
 * @param <T> type of target object
 */
class LogicPart<S, T> implements LogicPartConcept<S, T> {

    private AssociationBehaviour<S, T> rule;
    private BiConsumer<Pair<S, T>, Transaction<Void, Runnable>> handler;
    private BiPredicate<AssociationBehaviour<S, T>, S> ruleEvaluation;

    void setRuleEvaluation(final BiPredicate<AssociationBehaviour<S, T>, S> eval) {
        this.ruleEvaluation = requireNonNull(eval);
    }

    void setRule(final AssociationBehaviour<S, T> rule) {
        this.rule = requireNonNull(rule);
    }

    void setHandler(final BiConsumer<Pair<S, T>, Transaction<Void, Runnable>> handler) {
        this.handler = requireNonNull(handler);
    }

    @Override
    public void executeWith(final Pair<S, T> entities, final Transaction<Void, Runnable> transaction) {
        if (checkIfRuleDoesNotApply(entities.getFirst()))
            handleOutOfBoundaries(entities, transaction);
    }

    private boolean checkIfRuleDoesNotApply(final S src) {
        return !ruleEvaluation.test(rule, src);
    }

    private void handleOutOfBoundaries(final Pair<S, T> e, final Transaction<Void, Runnable> t) {
        handler.accept(e, t);
    }
}