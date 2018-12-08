package com.tutorial.beans;

import com.tutorial.core.MethodParameter;
import com.tutorial.core.convert.ConversionException;
import com.tutorial.core.convert.ConverterNotFoundException;

/**
 * Simple implementation of the TypeConverter interface that does not operate
 * on any specific target object. This is an alternative to using a full-blown
 * BeanWrapperImpl instance for arbitrary type conversion needs.
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see BeanWrapperImpl
 */
public class SimpleTypeConverter extends PropertyEditorRegistrySupport implements TypeConverter {
	
	private final TypeConverterDelegate typeConverterDelegate = new  TypeConverterDelegate(this);
	
	public SimpleTypeConverter() {
		registerDefaultEditors();
	}

	public <T> T convertIfNecessary(Object value, Class<T> requiredType) throws TypeMisMatchException {
		return convertIfNecessary(value, requiredType, null);
	}

	public <T> T convertIfNecessary(Object value, Class<T> requiredType, MethodParameter methodParameter)
			throws TypeMisMatchException {
		try {
			return this.typeConverterDelegate.convertIfNecessary(value, requiredType, methodParameter);
		} catch (ConverterNotFoundException ex) {
			throw new ConversionNotSupportedException(value, requiredType, ex);
		} catch (ConversionException ex) {
			throw new TypeMisMatchException(value, requiredType, ex);
		} catch (IllegalStateException ex) {
			throw new ConversionNotSupportedException(value, requiredType, ex);
		} catch (IllegalArgumentException ex) {
			throw new TypeMisMatchException(value, requiredType, ex);
		}
	}

}
