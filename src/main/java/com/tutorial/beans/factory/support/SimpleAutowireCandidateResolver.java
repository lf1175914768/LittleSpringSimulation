package com.tutorial.beans.factory.support;

import com.tutorial.beans.factory.config.BeanDefinitionHolder;
import com.tutorial.beans.factory.config.DependencyDescriptor;

/**
 * {@link AutowireCandidateResolver} implementation to use when no annotation
 * support is available. This implementation checks the bean definition only.
 *
 * @author Mark Fisher
 * @author Juergen Hoeller
 * @since 2.5
 * @see BeanDefinition#isAutowireCandidate()
 */
public class SimpleAutowireCandidateResolver implements AutowireCandidateResolver {

	/**
	 * Determine if the provided bean definition is an autowire candidate.
	 * <p>To be considered a candidate the bean's <em>autowire-candidate</em>
	 * attribute must not have been set to 'false'.
	 */
	public boolean isAutowireCandidate(BeanDefinitionHolder bdHolder, DependencyDescriptor descriptor) {
		return bdHolder.getBeanDefinition().isAutowireCandidate();
	}

	public Object getSuggestedValue(DependencyDescriptor descriptor) {
		return null;
	}

}
