package com.tutorial.expression;

/**
 * A bean resolver can be registered with the evaluation context
 * and will kick in for <code>@myBeanName</code> still expressions.
 *
 * @author Andy Clement
 * @since 3.0.3
 */
public interface BeanResolver {
	
	/**
	 * Look up the named bean and return it.
	 * @param context the current evaluation context
	 * @param beanName the name of the bean to lookup
	 * @return an object representing the bean
	 * @throws AccessException if there is an unexpected problem resolving the named bean
	 */
	Object resolve(EvaluationContext context, String beanName) throws AccessException;

}
