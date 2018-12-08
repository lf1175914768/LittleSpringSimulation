package com.tutorial.beans.factory.support;

import com.tutorial.beans.MutablePropertyValues;
import com.tutorial.beans.factory.BeanDefinitionStoreException;
import com.tutorial.beans.factory.BeanFactoryUtils;
import com.tutorial.beans.factory.config.BeanDefinition;
import com.tutorial.beans.factory.config.BeanDefinitionHolder;
import com.tutorial.beans.factory.config.ConstructorArgumentValues;
import com.tutorial.util.ClassUtils;
import com.tutorial.util.ObjectUtils;
import com.tutorial.util.StringUtils;

public class BeanDefinitionReaderUtils {
	
	public static final String GENERATED_BEAN_NAME_SEPARATOR = BeanFactoryUtils.GENERATED_BEAN_NAME_SEPARATOR;

	/**
	 * Create a new RootBeanDefinition or ChildBeanDefinition for the given
	 * class name, parent, constructor arguments, and property values.
	 * @param className the name of the bean class, if any
	 * @param parent the name of the parent bean, if any
	 * @param cargs the constructor arguments, if any
	 * @param pvs the property values, if any
	 * @param classLoader the ClassLoader to use for loading bean classes
	 * (can be null to just register bean classes by name)
	 * @return the bean definition
	 * @throws ClassNotFoundException if the bean class could not be loaded
	 */
	public static AbstractBeanDefinition createBeanDefinition(String className, String parent,
			ConstructorArgumentValues cargs, MutablePropertyValues pvs, ClassLoader classLoader) 
				throws ClassNotFoundException {
		Class<?> beanClass = null;
		if(className != null && classLoader != null) {
			beanClass = Class.forName(className, true, classLoader);
		}
		if(parent == null) {
			if(beanClass != null) {
				return new RootBeanDefinition(beanClass, cargs, pvs);
			}
			else {
				return new RootBeanDefinition(className, cargs, pvs);
			}
		}
		else {
			if(beanClass != null) {
				return new ChildBeanDefinition(parent, beanClass, cargs, pvs);
			}
			else {
				return new ChildBeanDefinition(parent, className, cargs, pvs);
			}
		}
	}

	/**
	 * Create a new GenericBeanDefinition for the given parent name and class name,
	 * eagerly loading the bean class if a ClassLoader has been specified.
	 * @param parentName the name of the parent bean, if any
	 * @param className the name of the bean class, if any
	 * @param classLoader the ClassLoader to use for loading bean classes
	 * (can be <code>null</code> to just register bean classes by name)
	 * @return the bean definition
	 * @throws ClassNotFoundException if the bean class could not be loaded
	 */
	public static AbstractBeanDefinition createBeanDefinition(String parentName, String className,
			ClassLoader classLoader) throws ClassNotFoundException {
		GenericBeanDefinition bd = new GenericBeanDefinition();
		bd.setParentName(parentName);
		if(className != null) {
			if(classLoader != null) {
				bd.setBeanClass(ClassUtils.forName(className, classLoader));
			} else {
				bd.setBeanClassName(className);
			}
		}
		return bd;
	}

	/**
	 * Generate a bean name for the given top-level bean definition,
	 * unique within the given bean factory.
	 * @param beanDefinition the bean definition to generate a bean name for
	 * @param registry the bean factory that the definition is going to be
	 * registered with (to check for existing bean names)
	 * @return the generated bean name
	 * @throws BeanDefinitionStoreException if no unique name can be generated
	 * for the given bean definition
	 */
	public static String generateBeanName(BeanDefinition definition, BeanDefinitionRegistry registry) 
				throws BeanDefinitionStoreException {
		return generateBeanName(definition, registry, false);
	}

	/**
	 * Generate a bean name for the given bean definition, unique within the
	 * given bean factory.
	 * @param definition the bean definition to generate a bean name for
	 * @param registry the bean factory that the definition is going to be
	 * registered with (to check for existing bean names)
	 * @param isInnerBean whether the given bean definition will be registered
	 * as inner bean or as top-level bean (allowing for special name generation
	 * for inner beans versus top-level beans)
	 * @return the generated bean name
	 * @throws BeanDefinitionStoreException if no unique name can be generated
	 * for the given bean definition
	 */
	public static String generateBeanName(BeanDefinition definition, 
			BeanDefinitionRegistry registry, boolean isInnerBean) throws BeanDefinitionStoreException {
		String generatedBeanName = definition.getBeanClassName();
		if(generatedBeanName == null) {
			if(definition.getParentName() != null) {
				generatedBeanName = definition.getParentName() + "$child";
			} else if(definition.getFactoryBeanName() != null) {
				generatedBeanName = definition.getFactoryBeanName() + "$created";
			} 
		} 
		if(!StringUtils.hasText(generatedBeanName)) {
			throw new BeanDefinitionStoreException("Unnamed bean definition specifies neither " +
					"'class' nor 'parent' nor 'factory-bean' - can't generate bean name");
		} 
		
		String id = generatedBeanName;
		if(isInnerBean) {
			// Inner bean : generate identity hashcode suffix.
			id = generatedBeanName + GENERATED_BEAN_NAME_SEPARATOR + ObjectUtils.getIdentityHexString(definition);
		} else {
			// Top-level bean: use plain class name.
			// Increase counter until the id is unique.
			int counter = -1;
			while(counter == -1 || registry.containsBeanDefinition(id)) {
				counter++;
				id = generatedBeanName + GENERATED_BEAN_NAME_SEPARATOR + counter;
			}
		}
		return id;
	}

	/**
	 * Register the given bean definition with the given bean factory.
	 * @param definitionHolder the bean definition including name and aliases
	 * @param registry the bean factory to register with
	 * @throws BeanDefinitionStoreException if registration failed
	 */
	public static void registerBeanDefinition(BeanDefinitionHolder bdHolder, 
			BeanDefinitionRegistry registry) throws BeanDefinitionStoreException {
		
		// Register bean definition under primary name.
		String beanName = bdHolder.getBeanName();
		registry.registerBeanDefinition(beanName, bdHolder.getBeanDefinition());
		
		// Register alias for bean name, if any.
		String[] aliases = bdHolder.getAliases();
		if(aliases != null) {
			for(String alias : aliases) {
				registry.registerAlias(beanName, alias);
			}
		}
	}

}
