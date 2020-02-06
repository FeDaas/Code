package de.util.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import de.util.Traits;

/**
 * An interface to mark fields or getters in a class as trait.
 * Traits are used by {@link Traits} to check equality between
 * to objects or calculate a hashCode for a given object.
 * 
 * <p>
 *     Every field of a class can be used as a trait. Only as trait marked fields
 *     will be used to determine equality between two objects of this class. If single
 *     fields don't reflect a real trait, but the result of a computation, then one
 *     could annotate a method as well.
 * </p>
 * This method fulfill this constraints:<br>
 * <ul>
 *   <li> must be non-void </li>
 *   <li> may return null </li>
 *   <li> has no arguments </li>
 * </ul>
 * 
 * <br><b>Author</b>: <a href="mailto:schustrchr@gmail.com">Ctoffer</a><br>
 * @see Traits
 * 
 */
@Retention(RUNTIME)
@Target({FIELD, METHOD})
public @interface Trait {

}
