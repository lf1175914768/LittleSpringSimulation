package com.tutorial.beans.factory.support;

import com.tutorial.beans.factory.BeanDefinitionStoreException;
import com.tutorial.beans.factory.NoSuchBeanDefinitionException;
import com.tutorial.beans.factory.config.BeanDefinition;
import com.tutorial.core.AliasRegistry;
/**
 * Interface for registries that hold bean definitions, for example RootBeanDefinition
 * and ChildBeanDefinition instances. Typically implemented by BeanFactories that
 * internally work with the AbstractBeanDefinition hierarchy.
 *
 * <p>This is the only interface in Spring's bean factory packages that encapsulates
 * <i>registration</i> of bean definitions. The standard BeanFactory interfaces
 * only cover access to a <i>fully configured factory instance</i>.
 *
 * <p>Spring's bean definition readers expect to work on an implementation of this
 * interface. Known implementors within the Spring core are DefaultListableBeanFactory
 * and GenericApplicationContext.
 *
 * @author Juergen Hoeller
 * @since 26.11.2003
 * @see org.springframework.beans.factory.config.BeanDefinition
 * @see AbstractBeanDefinition
 * @see RootBeanDefinition
 * @see ChildBeanDefinition
 * @see DefaultListableBeanFactory
 * @see org.springframework.context.support.GenericApplicationContext
 * @see org.springframework.beans.factory.xml.XmlBeanDefinitionReader
 * @see PropertiesBeanDefinitionReader
 */
public interface BeanDefinitionRegistry extends AliasRegistry {
	
	/**
	 * Return the number of beans defined in the registry.
	 * @return the number of beans defined in the registry
	 */
	int getBeanDefinitionCount();

	/**
	 * Return the names of all beans defined in this registry.
	 * @return the names of all beans defined in this registry,
	 * or an empty array if none defined
	 */
	String[] getBeanDefinitionNames();

	/**
	 * Check if this registry contains a bean definition with the given name.
	 * @param beanName the name of the bean to look for
	 * @return if this registry contains a bean definition with the given name
	 */
	boolean containsBeanDefinition(String name);
	
	/**
	 * Return the BeanDefinition for the given bean name.
	 * @param beanName name of the bean to find a definition for
	 * @return the BeanDefinition for the given name (never <code>null</code>)
	 * @throws NoSuchBeanDefinitionException if there is no such bean definition
	 */
	BeanDefinition getBeanDefinition(String name) throws NoSuchBeanDefinitionException;
	
	/**
	 * Register a new bean definition with this registry.
	 * Must support RootBeanDefinition and ChildBeanDefinition.
	 * @param beanName the name of the bean instance to register
	 * @param beanDefinition definition of the bean instance to register
	 * @throws BeanDefinitionStoreException if the BeanDefinition is invalid
	 * or if there is already a BeanDefinition for the specified bean name
	 * (and we are not allowed to override it)
	 * @see RootBeanDefinition
	 * @see ChildBeanDefinition
	 */
	void registerBeanDefinition(String name, BeanDefinition beanDefinition)
			throws BeanDefinitionStoreException;
	
	/**
	 * Remove the BeanDefinition for the given name.
	 * @param beanName the name of the bean instance to register
	 * @throws NoSuchBeanDefinitionException if there is no such bean definition
	 */
	void removeBeanDefinition(String beanName) throws NoSuchBeanDefinitionException;
	
	/**
	 * Determine whether the given bean name is already in use within this registry,
	 * i.e. whether there is a local bean or alias registered under this name.
	 * @param beanName the name to check
	 * @return whether the given bean name is already in use
	 */
	boolean isBeanNameInUse(String beanName);
}