package com.tutorial.beans.factory;

import com.tutorial.beans.BeansException;
import com.tutorial.util.StringUtils;

/**
 * Exception thrown when a BeanFactory is asked for a bean
 * instance name for which it cannot find a definition.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
public class NoSuchBeanDefinitionException extends BeansException {

	private static final long serialVersionUID = 1L;
	
	private String beanName;
	
	private Class<?> beanType;
	
	/**
	 * Create a new NoSuchBeanDefinitionException.
	 * @param name the name of the missing bean
	 */
	public NoSuchBeanDefinitionException(String name) {
		super("No bean named '" + name + "' is defined");
		this.beanName = name;
	}

	/**
	 * Create a new NoSuchBeanDefinitionException.
	 * @param name the name of the missing bean
	 * @param message detailed message describing the problem
	 */
	public NoSuchBeanDefinitionException(String name, String msg) {
		super("No bean named '" + name + "' is defined:" + msg);
		this.beanName = name;
	}
	
	/**
	 * Create a new NoSuchBeanDefinitionException.
	 * @param type required type of bean
	 */
	public NoSuchBeanDefinitionException(Class<?> type) {
		super("No unique bean of type [" + type.getName() + "] is defined");
		this.beanType = type;
	}

	/**
	 * Create a new NoSuchBeanDefinitionException.
	 * @param type required type of bean
	 * @param message detailed message describing the problem
	 */
	public NoSuchBeanDefinitionException(Class<?> type, String message) {
		super("No unique bean of type [" + type.getName() + "] is defined: " + message);
		this.beanType = type;
	}
	
	/**
	 * Create a new NoSuchBeanDefinitionException.
	 * @param type required type of bean
	 * @param dependencyDescription a description of the originating dependency
	 * @param message detailed message describing the problem
	 */
	public NoSuchBeanDefinitionException(Class<?> type, String dependencyDescription, String message) {
		super("No matching bean of type [" + type.getName() + "] found for dependency" +
				(StringUtils.hasLength(dependencyDescription) ? " [" + dependencyDescription + "]" : "") +
				": " + message);
		this.beanType = type;
	}
	
	/**
	 * Return the name of the missing bean,
	 * if it was a lookup by name that failed.
	 */
	public String getBeanName() {
		return this.beanName;
	}

	/**
	 * Return the required type of bean,
	 * if it was a lookup by type that failed.
	 */
	public Class<?> getBeanType() {
		return this.beanType;
	}

}
