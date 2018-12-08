package com.tutorial.beans.factory;

/**
 * Exception thrown when a bean instance has been requested for
 * a bean definition which has been marked as abstract.
 *
 * @author Juergen Hoeller
 * @since 1.1
 * @see com.tutorial.beans.factory.support.AbstractBeanDefinition#setAbstract
 */
@SuppressWarnings("serial")
public class BeanIsAbstractException extends BeanCreationException {
		
	/**
	 * Create a new BeanIsAbstractException.
	 * @param beanName the name of the bean requested
	 */
	public BeanIsAbstractException(String name) {
		super(name, "Bean defintion is abstract.");
	}
}
