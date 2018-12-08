package com.tutorial.beans;

import java.beans.PropertyChangeEvent;

import com.tutorial.util.ClassUtils;

/**
 * Exception thrown on a type mismatch when trying to set a bean property.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
public class TypeMisMatchException extends PropertyAccessException {
	
	public static final String ERROR_CODE = "typeMisMatch";
	
	private transient Object value;
	private Class<?> requiredType;
	
	/**
	 * Create a new TypeMismatchException.
	 * @param propertyChangeEvent the PropertyChangeEvent that resulted in the problem
	 * @param requiredType the required target type
	 */
	public TypeMisMatchException(PropertyChangeEvent propertyChangeEvent, Class<?> requiredType) {
		this(propertyChangeEvent, requiredType, null);
	}

	/**
	 * Create a new TypeMismatchException.
	 * @param propertyChangeEvent the PropertyChangeEvent that resulted in the problem
	 * @param requiredType the required target type (or <code>null</code> if not known)
	 * @param cause the root cause (may be <code>null</code>)
	 */
	public TypeMisMatchException(PropertyChangeEvent propertyChangeEvent, Class<?> requiredType, Throwable cause) {
		super(propertyChangeEvent, "Failed to convert property value of type '" +
				ClassUtils.getDescriptiveType(propertyChangeEvent.getNewValue()) + "'" +
				(requiredType != null ?  " to required type '" + ClassUtils.getQualifiedName(requiredType) + "'" : "") +
				(propertyChangeEvent.getPropertyName() != null ?
						 " for property '" + propertyChangeEvent.getPropertyName() + "'" : ""), cause);
		this.value = propertyChangeEvent.getNewValue();
		this.requiredType = requiredType;
	}
	
	/**
	 * Create a new TypeMismatchException without PropertyChangeEvent.
	 * @param value the offending value that couldn't be converted (may be <code>null</code>)
	 * @param requiredType the required target type (or <code>null</code> if not known)
	 */
	public TypeMisMatchException(Object value, Class<?> requiredType) {
		this(value, requiredType, null);
	}

	/**
	 * Create a new TypeMismatchException without PropertyChangeEvent.
	 * @param value the offending value that couldn't be converted (may be <code>null</code>)
	 * @param requiredType the required target type (or <code>null</code> if not known)
	 * @param cause the root cause (may be <code>null</code>)
	 */
	public TypeMisMatchException(Object value, Class<?> requiredType, Throwable ex) {
		super("Failed to convert value of type '" + ClassUtils.getDescriptiveType(value) + "'" +
				(requiredType != null ? " to required type '" + ClassUtils.getQualifiedName(requiredType) + "'" : ""),
				ex);
		this.value = value;
		this.requiredType = requiredType;
	}

	private static final long serialVersionUID = 1L;

	/**
	 * Return the offending value (may be <code>null</code>)
	 */
	@Override
	public Object getValue() {
		return this.value;
	}

	/**
	 * Return the required target type, if any.
	 */
	public Class<?> getRequiredType() {
		return this.requiredType;
	}
	
	public String getErrorCode() {
		return ERROR_CODE;
	}
	
}
