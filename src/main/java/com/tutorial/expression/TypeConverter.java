package com.tutorial.expression;

import com.tutorial.core.convert.TypeDescriptor;

/**
 * A type converter can convert values between different types encountered
 * during expression evaluation. This is an SPI for the expression parser;
 * see {@link com.tutorial.core.convert.ConversionService} for the
 * primary user API to Spring's conversion facilities.
 *
 * @author Andy Clement
 * @author Juergen Hoeller
 * @since 3.0
 */
public interface TypeConverter {
	
	/**
	 * Return true if the type converter can convert the specified type to the desired target type.
	 * @param sourceType a type descriptor that describes the source type
	 * @param targetType a type descriptor that describes the requested result type
	 * @return true if that conversion can be performed
	 */
	boolean canConvert(TypeDescriptor sourceType, TypeDescriptor targetType);
	
	/**
	 * Convert (may coerce) a value from one type to another, for example from a boolean to a string.
	 * The typeDescriptor parameter enables support for typed collections - if the caller really wishes they
	 * can have a List&lt;Integer&gt; for example, rather than simply a List.
	 * @param value the value to be converted
	 * @param sourceType a type descriptor that supplies extra information about the source object
	 * @param targetType a type descriptor that supplies extra information about the requested result type
	 * @return the converted value
	 * @throws EvaluationException if conversion is not possible
	 */
	Object convertValue(Object value, TypeDescriptor sourceType, TypeDescriptor targetType);

}
