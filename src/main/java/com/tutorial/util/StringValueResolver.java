package com.tutorial.util;

/**
 * Simple strategy interface for resolving a String value.
 * Used by {@link com.tutorial.beans.factory.config.ConfigurableBeanFactory}.
 *
 * @author Juergen Hoeller
 * @since 2.5
 * @see com.tutorial.beans.factory.config.ConfigurableBeanFactory#resolveAliases
 * @see com.tutorial.beans.factory.config.BeanDefinitionVisitor#BeanDefinitionVisitor(StringValueResolver)
 * @see com.tutorial.beans.factory.config.PropertyPlaceholderConfigurer
 */
public interface StringValueResolver {

	/**
	 * Resolve the given String value, for example parsing placeholders.
	 * @param strVal the original String value
	 * @return the resolved String value
	 */
	String resolveStringValue(String strVal);
}
