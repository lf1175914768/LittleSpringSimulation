package com.tutorial.beans.factory;

import com.tutorial.beans.BeansException;

/**
 * Thrown when a bean doesn't match the expected type.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
@SuppressWarnings("serial")
public class BeanNotOfRequiredTypeException extends BeansException {

	private String name;
	
	private Class<?> requiredType;
	
	private Class<?> actualType;
	
	/**
	 * Create a new BeanNotOfRequiredTypeException.
	 * @param beanName the name of the bean requested
	 * @param requiredType the required type
	 * @param actualType the actual type returned, which did not match
	 * the expected type
	 */
	public BeanNotOfRequiredTypeException(String name, Class<?> requiredType, Class<?> actualInstance) {
		super("Bean named '" + name + "' must be of type [" + requiredType.getName() + "], but was actually of "
				+ "type [" + actualInstance.getClass().getName() + "]");
		this.name = name;
		this.requiredType = requiredType;
		this.actualType = actualInstance;
	}

	/**
	 * Return the name of the instance that was of the wrong type.
	 */
	public String getBeanName() {
		return this.name;
	}

	/**
	 * Return the expected type for the bean.
	 */
	public Class<?> getRequiredType() {
		return this.requiredType;
	}

	/**
	 * Return the actual type of the instance found.
	 */
	public Class<?> getActualType() {
		return this.actualType;
	}

}
