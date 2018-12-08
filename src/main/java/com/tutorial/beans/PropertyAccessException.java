package com.tutorial.beans;

import java.beans.PropertyChangeEvent;

import com.tutorial.core.ErrorCoded;

/**
 * Superclass for exceptions related to a property access,
 * such as type mismatch or invocation target exception.
 *
 * @author liufeng
 * @author Juergen Hoeller
 */
public abstract class PropertyAccessException extends BeansException implements ErrorCoded {

	private static final long serialVersionUID = 1L;
	private transient PropertyChangeEvent propertyChangeEvent;
	
	/**
	 * Create a new PropertyAccessException.
	 * @param propertyChangeEvent the PropertyChangeEvent that resulted in the problem
	 * @param msg the detail message
	 * @param cause the root cause
	 */
	public PropertyAccessException(PropertyChangeEvent propertyChangeEvent, String msg, Throwable cause) {
		super(msg, cause);
		this.propertyChangeEvent = propertyChangeEvent;
	}
	
	/**
	 * Create a new PropertyAccessException without PropertyChangeEvent.
	 * @param msg the detail message
	 * @param cause the root cause
	 */
	public PropertyAccessException(String msg, Throwable cause) {
		super(msg, cause);
	}
	
	/**
	 * Return the PropertyChangeEvent that resulted in the problem.
	 * <p>May be <code>null</code>; only available if an actual bean property
	 * was affected.
	 */
	public PropertyChangeEvent getPropertyChangeEvent() {
		return this.propertyChangeEvent;
	}
	
	public String getPropertyName() {
		return (this.propertyChangeEvent != null ? this.propertyChangeEvent.getPropertyName() : null);
	}
	
	public Object getValue() {
		return (this.propertyChangeEvent != null ? this.propertyChangeEvent.getNewValue() : null);
	}
}
