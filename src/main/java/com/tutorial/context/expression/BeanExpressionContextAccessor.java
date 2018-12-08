package com.tutorial.context.expression;

import com.tutorial.beans.factory.config.BeanExpressionContext;
import com.tutorial.expression.AccessException;
import com.tutorial.expression.EvaluationContext;
import com.tutorial.expression.PropertyAccessor;
import com.tutorial.expression.TypedValue;

/**
 * EL property accessor that knows how to traverse the beans and contextual objects
 * of a Spring {@link com.tutorial.beans.factory.config.BeanExpressionContext}.
 * 
 * @author Liufeng
 * Created on 2018年11月17日 下午4:37:53
 */
public class BeanExpressionContextAccessor implements PropertyAccessor {

	@Override
	public Class<?>[] getSpecificTargetClasses() {
		return new Class[] {BeanExpressionContext.class};
	}

	@Override
	public boolean canRead(EvaluationContext context, Object target, String name) throws AccessException {
		return ((BeanExpressionContext) target).containsObject(name);
	}

	@Override
	public TypedValue read(EvaluationContext context, Object target, String name) throws AccessException {
		return new TypedValue(((BeanExpressionContext) target).getObject(name));
	}

	@Override
	public boolean canWrite(EvaluationContext context, Object target, String name) throws AccessException {
		return false;
	}

	@Override
	public void write(EvaluationContext context, Object target, String name, Object newValue) throws AccessException {
		throw new AccessException("Bean in a BeanFactory are read-only");
	}

}
