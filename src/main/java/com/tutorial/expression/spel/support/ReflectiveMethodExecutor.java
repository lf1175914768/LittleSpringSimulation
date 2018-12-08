package com.tutorial.expression.spel.support;

import java.lang.reflect.Method;

import com.tutorial.core.MethodParameter;
import com.tutorial.core.convert.TypeDescriptor;
import com.tutorial.expression.AccessException;
import com.tutorial.expression.EvaluationContext;
import com.tutorial.expression.MethodExecutor;
import com.tutorial.expression.TypedValue;
import com.tutorial.util.ReflectionUtils;

/**
 * @author Andy Clement
 * @author Juergen Hoeller
 * @since 3.0
 */
public class ReflectiveMethodExecutor implements MethodExecutor {
	
	private final Method method;
	
	private final Integer varargsPosition;
	
	// When the method was found, we will have determined if arguments need to be converted for it
	// to be invoked. Conversion won't be cheap so let's only do it if necessary.
	private final int[] argsRequiringConversion;
	
	public ReflectiveMethodExecutor(Method method, int[] argumentsRequiringConversion) {
		this.method = method;
		if(method.isVarArgs()) {
			Class<?>[] paramTypes = method.getParameterTypes();
			this.varargsPosition = paramTypes.length - 1;
		} else {
			this.varargsPosition = null;
		}
		this.argsRequiringConversion = argumentsRequiringConversion;
	}

	public TypedValue execute(EvaluationContext context, Object target, Object... arguments) throws AccessException {
		try {
			if(arguments != null) {
				ReflectionHelper.convertArguments(context.getTypeConverter(), arguments, 
						this.method, this.argsRequiringConversion, this.varargsPosition);
			}
			if(this.method.isVarArgs()) {
				arguments = ReflectionHelper.setupArgumentsForVarargsInvocation(
						this.method.getParameterTypes(), arguments);
			}
			ReflectionUtils.makeAccessible(this.method);
			Object value = this.method.invoke(target, arguments);
			return new TypedValue(value, new TypeDescriptor(new MethodParameter(this.method, -1)).narrow(value));
		} catch (Exception e) {
			throw new AccessException("Problem invoking method: " + this.method, e);
		}
	}

}
