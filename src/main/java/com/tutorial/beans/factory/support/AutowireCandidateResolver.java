package com.tutorial.beans.factory.support;

import com.tutorial.beans.factory.config.BeanDefinitionHolder;
import com.tutorial.beans.factory.config.DependencyDescriptor;

/**
 * Strategy interface for determining whether a specific bean definition
 * qualifies as an autowire candidate for a specific dependency.
 *
 * @author Mark Fisher
 * @author Juergen Hoeller
 * @since 2.5
 */
public interface AutowireCandidateResolver {
	
	/**
	 * Determine whether the given bean definition qualifies as an
	 * autowire candidate for the given dependency.
	 * @param bdHolder the bean definition including bean name and aliases
	 * @param descriptor the descriptor for the target method parameter or field
	 * @return whether the bean definition qualifies as autowire candidate
	 */
	boolean isAutowireCandidate(BeanDefinitionHolder bdHolder, DependencyDescriptor descriptor);
	
	/**
	 * Determine whether a default value is suggested for the given dependency.
	 * @param descriptor the descriptor for the target method parameter or field
	 * @return the value suggested (typically an expression String),
	 * or <code>null</code> if none found
	 * @since 3.0
	 */
	Object getSuggestedValue(DependencyDescriptor descriptor);

}
