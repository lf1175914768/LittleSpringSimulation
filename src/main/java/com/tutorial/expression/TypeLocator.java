package com.tutorial.expression;

/**
 * Implementors of this interface are expected to be able to locate types. They may use custom classloaders
 * or the and deal with common package prefixes (java.lang, etc) however they wish. See
 * {@link com.tutorial.expression.spel.support.StandardTypeLocator} for an example implementation.
 *
 * @author Andy Clement
 * @since 3.0
 */
public interface TypeLocator {
	
	/**
	 * Find a type by name. The name may or may not be fully qualified (eg. String or java.lang.String)
	 * @param typename the type to be located
	 * @return the class object representing that type
	 * @throws EvaluationException if there is a problem finding it
	 */
	Class<?> findType(String typeName) throws EvaluationException;

}
