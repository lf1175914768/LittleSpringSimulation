package com.tutorial.expression.spel.ast;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import com.tutorial.core.convert.TypeDescriptor;
import com.tutorial.expression.AccessException;
import com.tutorial.expression.EvaluationContext;
import com.tutorial.expression.EvaluationException;
import com.tutorial.expression.ExpressionInvocationTargetException;
import com.tutorial.expression.MethodExecutor;
import com.tutorial.expression.MethodResolver;
import com.tutorial.expression.TypedValue;
import com.tutorial.expression.spel.ExpressionState;
import com.tutorial.expression.spel.SpelEvaluationException;
import com.tutorial.expression.spel.SpelMessage;

/**
 * @author Liufeng
 * Created on 2018年11月17日 上午11:33:27
 */
public class MethodReference extends SpelNodeImpl {

	private final String name;

	private final boolean nullSafe;

	private volatile MethodExecutor cachedExecutor;


	public MethodReference(boolean nullSafe, String methodName, int pos, SpelNodeImpl... arguments) {
		super(pos,arguments);
		this.name = methodName;
		this.nullSafe = nullSafe;
	}


	@Override
	public TypedValue getValueInternal(ExpressionState state) throws EvaluationException {
		TypedValue currentContext = state.getActiveContextObject();
		Object[] arguments = new Object[getChildCount()];
		for (int i = 0; i < arguments.length; i++) {
			// Make the root object the active context again for evaluating the parameter
			// expressions
			try {
				state.pushActiveContextObject(state.getRootContextObject());
				arguments[i] = children[i].getValueInternal(state).getValue();
			}
			finally {
				state.popActiveContextObject();	
			}
		}
		if (currentContext.getValue() == null) {
			if (this.nullSafe) {
				return TypedValue.NULL;
			}
			else {
				throw new SpelEvaluationException(getStartPosition(), SpelMessage.METHOD_CALL_ON_NULL_OBJECT_NOT_ALLOWED,
						FormatHelper.formatMethodForMessage(name, getTypes(arguments)));
			}
		}

		MethodExecutor executorToUse = this.cachedExecutor;
		if (executorToUse != null) {
			try {
				return executorToUse.execute(
						state.getEvaluationContext(), state.getActiveContextObject().getValue(), arguments);
			}
			catch (AccessException ae) {
				// Two reasons this can occur:
				// 1. the method invoked actually threw a real exception
				// 2. the method invoked was not passed the arguments it expected and has become 'stale'
				
				// In the first case we should not retry, in the second case we should see if there is a 
				// better suited method.
				
				// To determine which situation it is, the AccessException will contain a cause.
				// If the cause is an InvocationTargetException, a user exception was thrown inside the method.
				// Otherwise the method could not be invoked.
				throwSimpleExceptionIfPossible(state, ae);
				
				// at this point we know it wasn't a user problem so worth a retry if a better candidate can be found
				this.cachedExecutor = null;
			}
		}

		// either there was no accessor or it no longer existed
		executorToUse = findAccessorForMethod(this.name, getTypes(arguments), state);
		this.cachedExecutor = executorToUse;
		try {
			return executorToUse.execute(
					state.getEvaluationContext(), state.getActiveContextObject().getValue(), arguments);
		} catch (AccessException ae) {
			// Same unwrapping exception handling as above in above catch block
			throwSimpleExceptionIfPossible(state, ae);
			throw new SpelEvaluationException( getStartPosition(), ae, SpelMessage.EXCEPTION_DURING_METHOD_INVOCATION,
					this.name, state.getActiveContextObject().getValue().getClass().getName(), ae.getMessage());
		}
	}


	/**
	 * Decode the AccessException, throwing a lightweight evaluation exception or, if the cause was a RuntimeException, 
	 * throw the RuntimeException directly.
	 */
	private void throwSimpleExceptionIfPossible(ExpressionState state, AccessException ae) {
		if (ae.getCause() instanceof InvocationTargetException) {
			Throwable rootCause = ae.getCause().getCause();
			if (rootCause instanceof RuntimeException) {
				throw (RuntimeException) rootCause;
			}
			else {
				throw new ExpressionInvocationTargetException(getStartPosition(),
						"A problem occurred when trying to execute method '" + this.name +
						"' on object of type '" + state.getActiveContextObject().getValue().getClass().getName() + "'",
						rootCause);
			}			
		}
	}

	private List<TypeDescriptor> getTypes(Object... arguments) {
		List<TypeDescriptor> descriptors = new ArrayList<TypeDescriptor>(arguments.length);
		for (Object argument : arguments) {
			descriptors.add(TypeDescriptor.forObject(argument));
		}
		return descriptors;
	}

	@Override
	public String toStringAST() {
		StringBuilder sb = new StringBuilder();
		sb.append(name).append("(");
		for (int i = 0; i < getChildCount(); i++) {
			if (i > 0)
				sb.append(",");
			sb.append(getChild(i).toStringAST());
		}
		sb.append(")");
		return sb.toString();
	}

	private MethodExecutor findAccessorForMethod(String name, List<TypeDescriptor> argumentTypes, ExpressionState state)
			throws SpelEvaluationException {

		TypedValue context = state.getActiveContextObject();
		Object contextObject = context.getValue();
		EvaluationContext eContext = state.getEvaluationContext();

		List<MethodResolver> mResolvers = eContext.getMethodResolvers();
		if (mResolvers != null) {
			for (MethodResolver methodResolver : mResolvers) {
				try {
					MethodExecutor cEx = methodResolver.resolve(
							state.getEvaluationContext(), contextObject, name, argumentTypes);
					if (cEx != null) {
						return cEx;
					}
				}
				catch (AccessException ex) {
					throw new SpelEvaluationException(getStartPosition(),ex, SpelMessage.PROBLEM_LOCATING_METHOD, name, contextObject.getClass());
				}
			}
		}
		throw new SpelEvaluationException(getStartPosition(),SpelMessage.METHOD_NOT_FOUND, FormatHelper.formatMethodForMessage(name, argumentTypes),
				FormatHelper.formatClassNameForMessage(contextObject instanceof Class ? ((Class<?>) contextObject) : contextObject.getClass()));
	}

}
