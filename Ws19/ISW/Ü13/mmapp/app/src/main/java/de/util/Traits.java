package de.util;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiPredicate;

import de.util.annotations.Trait;

import static de.util.ObjectUtils.getAllFields;
import static de.util.ObjectUtils.getAllMethods;
import static java.lang.String.format;
import static java.lang.reflect.Modifier.isFinal;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

/**
 * Utility class to enable the creation of a intuitive and consistent
 * equals and hashCode implementation of any given class.
 * This class process every attribute or method annotated with {@link Trait}.
 *
 * <p>
 * A trait is considered to be either a field or method, which represents
 * a property of an object containing information for its uniqueness.
 * The traits can then be used to determine if to objects are equal and
 * therefore share the same hash.
 * </p>
 *
 * <p>
 * Some could use {@link Traits} in the following manner:
 * </p>
 * <pre><code>
 * public class Foo {
 *      private static final Traits TRAITS = new Traits(Foo.class);
 *     {@literal @}Trait private String x;
 *     {@literal @}Trait private int y;
 *
 *      public Foo(String x, int y) {
 *          this.x = x;
 *          this.y = y;
 *      }
 *
 *     {@literal @}Override
 *      public boolean equals(Object other) {
 *          return TRAITS.testEqualityBetween(this, other);
 *      }
 *
 *     {@literal @}Override
 *      public int hashCode() {
 *          return TRAITS.createHashCodeFor(this);
 *      }
 * }
 * </code></pre>
 *
 * <br><b>Author</b>: <a href="mailto:schustrchr@gmail.com">Ctoffer</a><br>
 *
 * @see Trait
 */
public class Traits {
    private static final Traits TRAITS = new Traits(Traits.class);

    @Trait
    private final Class<?> cls;
    private List<Field> fields;
    private List<Method> methods;
    private List<TraitAccessPoint> traitFields;
    private List<TraitAccessPoint> immutableFields;
    private List<TraitAccessPoint> traitGetter;

    /**
     * Creates a new instance collecting all fields and methods via reflection,
     * which are annotated with {@link Trait}.<br>
     * Every field in a class can be used as a trait and may contain <i>null</i>.
     * <br>
     * Methods must be getters. As a getter is a method considered which:
     * <ul>
     * <li>has a non-<i>void</i> return type</li>
     * <li>has no parameters</li>
     * <li>may contain <i>null</i></li>
     * </ul>
     *
     * @param cls class which should be used for this Traits instance
     * @throws java.lang.RuntimeException if a field or method could not be accessed
     */
    public Traits(final Class<?> cls) {
        this.cls = cls;
        collectDeclaredImmutableFields();
        collectDeclaredFields();
        collectDeclaredGetters();
    }

    /**
     * Collects all declared fields of the stored class, which are annotated
     * with {@link Trait} and are declared <i>final</i>.
     * Stores a{@link TraitAccessPoint} for each of them.
     *
     * @throws java.lang.reflect.InaccessibleObjectException if access can not be enabled
     * @throws SecurityException                             if a {@link SecurityManager} blocks access
     */
    private void collectDeclaredImmutableFields() {
        fields = getAllFields(cls)
                .stream()
                .filter(Traits::isTraitPresent)
                .filter(f -> isFinal(f.getModifiers()))
                .collect(toList());
        immutableFields = fields.stream()
                .map(Traits::toAccessibleField)
                .collect(toList());
    }

    /**
     * Collects all declared fields of the stored class, which are annotated
     * with {@link Trait} and stores {@link TraitAccessPoint}s for them.
     *
     * @throws java.lang.reflect.InaccessibleObjectException if access can not be enabled
     * @throws SecurityException                             if a {@link SecurityManager} blocks access
     */
    private void collectDeclaredFields() {
        fields = getAllFields(cls).stream()
                .filter(Traits::isTraitPresent)
                .collect(toList());
        traitFields = fields.stream()
                .map(Traits::toAccessibleField)
                .collect(toList());
    }

    /**
     * Tries to inject 'field' accessible and creates an {@link TraitAccessPoint}.
     *
     * @param field the field which should be transformed
     * @return the {@link TraitAccessPoint} for 'field'
     * @throws java.lang.reflect.InaccessibleObjectException if access can not be enabled
     * @throws SecurityException                             if a {@link SecurityManager} blocks access
     */
    private static TraitAccessPoint toAccessibleField(Field field) {
        field.setAccessible(true);
        return obj -> ofNullable(field.get(obj));
    }

    /**
     * Collects all declared methods of the stored class, which are annotated
     * with {@link Trait} and stores {@link TraitAccessPoint}s for them.
     * Only methods that fulfill following criterion's will be used:
     * <ul>
     * <li>The method's return type is non-<i>void</i></li>
     * <li>The method has no parameters</li>
     * </ul>
     *
     * @throws java.lang.reflect.InaccessibleObjectException if access can not be enabled
     * @throws SecurityException                             if a {@link SecurityManager} blocks access
     */
    private void collectDeclaredGetters() {
        methods = getAllMethods(cls)
                .stream()
                .filter(Traits::isTraitPresent)
                .filter(Traits::isNonVoid)
                .filter(Traits::hasNoParameters)
                .collect(toList());
        traitGetter = methods.stream()
                .map(Traits::toGetter)
                .collect(toList());
    }

    private static boolean isTraitPresent(AccessibleObject ao) {
        return ao.isAnnotationPresent(Trait.class);
    }

    private static boolean isNonVoid(Method m) {
        return !m.getReturnType().equals(Void.TYPE);
    }

    private static boolean hasNoParameters(Method m) {
        return m.getParameterTypes().length == 0;
    }

    private static TraitAccessPoint toGetter(final Method m) {
        return obj -> {
            m.setAccessible(true);
            final Optional<Object> result = ofNullable(m.invoke(obj));
            m.setAccessible(false);
            return result;
        };
    }

    /**
     * Test the equality between two given objects, where the first one ('dis') is assumed to be
     * <i>this</i> and the second one ('object') as other in the scheme:<br>
     * <pre><code>this.equals(other)</code></pre><br>
     * Equality is determined after following steps:
     * <ol>
     * <li>other == <i>null</i> &#8594; <i>false</i></li>
     * <li>other.<i>class</i> nequals <i>this</i>.<i>class</i> &#8594; <i>false</i></li>
     * <li>other == <i>this</i> &#8594; <i>true</i></li>
     * <li>
     * for all traits t: <i>this</i>.t equals other.t &#8594; <i>true</i> iff all
     * <i>true</i>, <i>false</i> otherwise
     * </li>
     * </ol>
     *
     * @param dis    'this'-object of the comparison
     * @param object 'other'-object of the comparison
     * @return <i>true</i> if given objects are equal, <i>false</i> otherwise
     * @throws NullPointerException if 'this' is <i>null</i>
     * @throws RuntimeException     if a field or method could not be accessed
     */
    public boolean testEqualityBetween(Object dis, Object object) {
        checkIfThisIsNotNull(dis);
        if (isNotCompatible(object)) {
            return false;
        }

        return compareObjects(dis, object);
    }

    private void checkIfThisIsNotNull(Object dis) {
        requireNonNull(dis);
    }

    private boolean isNotCompatible(Object obj) {
        return isNull(obj) || hasDifferentClass(obj);
    }

    private boolean isNull(Object obj) {
        return obj == null;
    }

    private boolean hasDifferentClass(Object obj) {
        return obj.getClass() != cls;
    }

    private boolean compareObjects(Object dis, Object object) {
        if (isReferentiallySame(dis, object))
            return true;
        return compareTraitsOfObjects(dis, object);
    }

    private boolean isReferentiallySame(Object a, Object b) {
        return a == b;
    }

    private boolean compareTraitsOfObjects(Object dis, Object object) {
        boolean sameFieldTraits = compareFieldTraits(dis, object);
        boolean sameMethodTrais = compareMethodTraits(dis, object);
        return sameFieldTraits && sameMethodTrais;
    }

    private boolean compareFieldTraits(Object dis, Object object) {
        return check(dis, object, traitFields);
    }

    private boolean compareMethodTraits(Object dis, Object object) {
        return check(dis, object, traitGetter);
    }

    private static boolean check(Object a, Object b, List<TraitAccessPoint> li) {
        return li.stream()
                .map(Traits::createComparison)
                .allMatch(comp -> comp.test(a, b));
    }

    private static BiPredicate<Object, Object> createComparison(TraitAccessPoint field) {
        return (obj1, obj2) -> {
            Optional<?> field1 = field.tryEnter(obj1);
            Optional<?> field2 = field.tryEnter(obj2);

            return field1.equals(field2);
        };
    }

    /**
     * Creates a hash code for the given object with the formula:<br>
     * <p><br>
     * hash(obj) := 11 * hashOfFields(obj) + 31 * hashForGetters(obj)
     * </p><br>
     * Where hashOfFields and hashOfGetter using {@link Objects#hash(Object...)}
     * to determine the value.
     *
     * @param obj instance for which the hashCode should be calculated
     * @return hash for 'obj'
     * @throws NullPointerException     if the given object is <i>null</i>
     * @throws IllegalArgumentException if type of given object doesn't match
     * @throws RuntimeException         if access was denied or a called getter has thrown exception
     */
    public int createHashCodeFor(Object obj) {
        return 11 * createHashForFields(obj) + 31 * createHashForGetters(obj);
    }

    private int createHashForFields(Object obj) {
        return createHash(obj, traitFields);
    }

    private int createHashForGetters(Object obj) {
        return createHash(obj, traitGetter);
    }

    private static int createHash(Object a, List<TraitAccessPoint> li) {
        Object[] traits = li.stream()
                .map(func -> func.tryEnter(a))
                .filter(Optional::isPresent)
                .toArray();
        return Objects.hash(traits);
    }


    public int createImmutableHashFor(Object obj) {
        return createHash(obj, immutableFields);
    }

    /**
     * @param obj
     * @return <i>true</i> if the given object if it equals <i>this</i>
     * @see Traits#testEqualityBetween(Object, Object)
     */
    @Override
    public boolean equals(Object obj) {
        return TRAITS.testEqualityBetween(this, obj);
    }

    /**
     * @return hashCode of this object
     * @see Traits#createHashCodeFor(Object)
     */
    @Override
    public int hashCode() {
        return TRAITS.createHashCodeFor(this);
    }

    /**
     * <p>
     * Creates a string representation.<br>
     * "Traits{cls=<name>, fields=[<fnames>], methods=[<mnames>]}"<br>
     * </p>
     *
     * <ul>
     * <li>name: canonical name of the class (see {@link Class#getCanonicalName()})</li>
     * <li>fnames: comma separated names of the fields used as trait</li>
     * <li>mnames: comma separated names of the methods used as trait</li>
     * </ul>
     *
     * @return string representation of this instance
     */
    @Override
    public String toString() {
        String format = "Traits{cls=%s, fields=[%s], methods=[%s]}";
        String clsName = cls.getCanonicalName();
        String fieldStr = fields.stream()
                .map(Field::getName)
                .sorted(String::compareTo)
                .collect(joining(", "));
        String methodStr = methods.stream()
                .map(Method::getName)
                .sorted(String::compareTo)
                .collect(joining(", "));
        return format(format, clsName, fieldStr, methodStr);
    }
}

@FunctionalInterface
interface TraitAccessPoint {
    /**
     * Enters the trait and receives its value.
     *
     * @param obj object which trait should be accessed
     * @return Optional containing the retrieved trait or empty if trait was <i>null</i>
     * @throws IllegalAccessException    if access was denied
     * @throws InvocationTargetException if underlying model is throws an exception
     */
    Optional<Object> enter(Object obj) throws IllegalAccessException, InvocationTargetException;

    /**
     * Tries to call the {@link TraitAccessPoint#enter(Object)}.
     *
     * @param obj object which trait should be accessed
     * @return Optional containing the retrieved trait or empty if trait was <i>null</i>
     * @throws RuntimeException if enter throws an exception
     */
    default Optional<Object> tryEnter(Object obj) {
        try {
            return ofNullable(enter(obj));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
