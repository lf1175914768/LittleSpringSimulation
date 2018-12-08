package com.tutorial.context.expression;

import com.tutorial.core.env.Environment;
import com.tutorial.expression.AccessException;
import com.tutorial.expression.EvaluationContext;
import com.tutorial.expression.PropertyAccessor;
import com.tutorial.expression.TypedValue;

/**
 * Read-only EL property accessor that knows how to retrieve keys
 * of a Spring {@link Environment} instance.
 * 
 * @author Liufeng
 * Created on 2018年11月17日 下午5:00:58
 */
public class EnvironmentAccessor implements PropertyAccessor {

	@Override
	public Class<?>[] getSpecificTargetClasses() {
		return new Class[] {Environment.class};
	}

	@Override
	public boolean canRead(EvaluationContext context, Object target, String name) throws AccessException {
		return true;
	}

	@Override
	public TypedValue read(EvaluationContext context, Object target, String name) throws AccessException {
		return new TypedValue(((Environment)target).getProperty(name));
	}

	@Override
	public boolean canWrite(EvaluationContext context, Object target, String name) throws AccessException {
		return false;
	}

	@Override
	public void write(EvaluationContext context, Object target, String name, Object newValue) throws AccessException {
		
	}

}
