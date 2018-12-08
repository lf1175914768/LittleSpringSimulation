package com.tutorial.beans.factory.support;

/**
 * Programmatic means of constructing
 * {@link com.tutorial.beans.factory.config.BeanDefinition BeanDefinitions}
 * using the builder pattern. Intended primarily for use when implementing Spring 2.0
 * {@link com.tutorial.beans.factory.xml.NamespaceHandler NamespaceHandlers}.
 *
 * @author Rod Johnson
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 2.0
 */
public class BeanDefinitionBuilder {
	
	/**
	 * The <code>BeanDefinition</code> instance we are creating.
	 */
	private AbstractBeanDefinition beanDefinition;

	/**
	 * Create a new <code>BeanDefinitionBuilder</code> used to construct a {@link RootBeanDefinition}.
	 * @param beanClass the <code>Class</code> of the bean that the definition is being created for
	 */
	public static BeanDefinitionBuilder rootBeanDefinition(Class<?> beanClass) {
		return rootBeanDefinition(beanClass, null);
	}

	/**
	 * Create a new <code>BeanDefinitionBuilder</code> used to construct a {@link RootBeanDefinition}.
	 * @param beanClass the <code>Class</code> of the bean that the definition is being created for
	 * @param factoryMethodName the name of the method to use to construct the bean instance
	 */
	public static BeanDefinitionBuilder rootBeanDefinition(Class<?> beanClass, String factoryMethodName) {
		BeanDefinitionBuilder builder = new BeanDefinitionBuilder();
		builder.beanDefinition = new RootBeanDefinition();
		builder.beanDefinition.setBeanClass(beanClass);
		builder.beanDefinition.setFactoryMethodName(factoryMethodName);
		return builder;
	}
	
	/**
	 * Create a new <code>BeanDefinitionBuilder</code> used to construct a {@link ChildBeanDefinition}.
	 * @param parentName the name of the parent bean
	 */
	public static BeanDefinitionBuilder childBeanDefinition(String parentName) {
		BeanDefinitionBuilder builder = new BeanDefinitionBuilder();
		builder.beanDefinition = new ChildBeanDefinition(parentName);
		return builder;
	}

	/**
	 * Set whether or not this definition is abstract.
	 */
	public BeanDefinitionBuilder setAbstract(boolean flag)  {
		this.beanDefinition.setAbstract(flag);
		return this;
	}
	
	/**
	 * Return the current BeanDefinition object in its raw (unvalidated) form.
	 * @see #getBeanDefinition()
	 */
	public AbstractBeanDefinition getBeanDefinition() {
		return this.beanDefinition;
	}

}
