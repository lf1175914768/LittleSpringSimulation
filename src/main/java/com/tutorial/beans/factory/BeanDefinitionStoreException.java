package com.tutorial.beans.factory;

import com.tutorial.beans.FatalBeanException;
import com.tutorial.core.io.Resource;

/**
 * Exception thrown when a BeanFactory encounters an invalid bean definition:
 * e.g. in case of incomplete or contradictory bean metadata.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Rob Harrop
 */
@SuppressWarnings("serial")
public class BeanDefinitionStoreException extends FatalBeanException {

	private String resourceDescription;
	
	private String beanName;
	
	/**
	 * Create a new BeanDefinitionStoreException.
	 * @param msg the detail message (used as exception message as-is)
	 */
	public BeanDefinitionStoreException(String msg) {
		super(msg);
	}
	
	/**
	 * Create a new BeanDefinitionStoreException.
	 * @param msg the detail message (used as exception message as-is)
	 * @param cause the root cause (may be <code>null</code>)
	 */
	public BeanDefinitionStoreException(String msg, Throwable e) {
		super(msg, e);
	}
	
	/**
	 * Create a new BeanDefinitionStoreException.
	 * @param resourceDescription description of the resource that the bean definition came from
	 * @param msg the detail message (used as exception message as-is)
	 */
	public BeanDefinitionStoreException(String resourceDescription, String msg) {
		super(msg);
		this.resourceDescription = resourceDescription;
	}

	/**
	 * Create a new BeanDefinitionStoreException.
	 * @param resourceDescription description of the resource that the bean definition came from
	 * @param msg the detail message (used as exception message as-is)
	 * @param cause the root cause (may be <code>null</code>)
	 */
	public BeanDefinitionStoreException(String resourceDescription, String msg, Throwable cause) {
		super(msg, cause);
		this.resourceDescription = resourceDescription;
	}
	
	/**
	 * Create a new BeanDefinitionStoreException.
	 * @param resourceDescription description of the resource that the bean definition came from
	 * @param beanName the name of the bean requested
	 * @param msg the detail message (appended to an introductory message that indicates
	 * the resource and the name of the bean)
	 */
	public BeanDefinitionStoreException(String resourceDescription, String beanName, String msg) {
		this(resourceDescription, beanName, msg, null);
	}

	public BeanDefinitionStoreException(String resourceDescription, String beanName, String msg, Throwable object) {
		super("Error registering bean with name '" + beanName + "' defined in " + resourceDescription + ": " + msg, object);
		this.resourceDescription = resourceDescription;
		this.beanName = beanName;
	}

	public BeanDefinitionStoreException(Resource documentLocation, String beanName, String msg) {
		this(documentLocation.getDescription(), beanName, msg, null);
	}

	public BeanDefinitionStoreException(Resource documentLocation, String beanName, String msg, Throwable ex) {
		this(documentLocation.getDescription(), beanName, msg, ex);
	}
	
	/**
	 * Return the description of the resource that the bean
	 * definition came from, if any.
	 */
	public String getResourceDescription() {
		return this.resourceDescription;
	}

	/**
	 * Return the name of the bean requested, if any.
	 */
	public String getBeanName() {
		return this.beanName;
	}

}

	
