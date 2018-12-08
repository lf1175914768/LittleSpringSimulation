package com.tutorial.context.expression;

import com.tutorial.beans.BeansException;
import com.tutorial.beans.factory.BeanFactory;
import com.tutorial.expression.AccessException;
import com.tutorial.expression.BeanResolver;
import com.tutorial.expression.EvaluationContext;
import com.tutorial.util.Assert;

/**
 * EL bean resolver that operates against a Spring
 * {@link com.tutorial.beans.factory.BeanFactory}.
 * 
 * 
 * @author Liufeng
 * Created on 2018年11月17日 下午5:03:52
 */
public class BeanFactoryResolver implements BeanResolver {
	
	private final BeanFactory beanFactory;
	
	public BeanFactoryResolver(BeanFactory beanFactory) {
		Assert.notNull(beanFactory, "BeanFactory must not be null");
		this.beanFactory = beanFactory;
	}

	@Override
	public Object resolve(EvaluationContext context, String beanName) throws AccessException {
		try {
			return this.beanFactory.getBean(beanName);
		} catch(BeansException ex) {
			throw new AccessException("Could not resolve bean reference against BeanFactory", ex);
		}
	}

}
