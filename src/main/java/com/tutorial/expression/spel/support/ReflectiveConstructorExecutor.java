package com.tutorial.expression.spel.support;

import java.lang.reflect.Constructor;

import com.tutorial.expression.AccessException;
import com.tutorial.expression.ConstructorExecutor;
import com.tutorial.expression.EvaluationContext;
import com.tutorial.expression.TypedValue;
import com.tutorial.util.ReflectionUtils;

/**
 * A simple ConstructorExecutor implementation that runs a constructor using reflective invocation.
 *
 * @author Andy Clement
 * @author Juergen Hoeller
 * @since 3.0
 */
public class ReflectiveConstructorExecutor implements ConstructorExecutor {
	
	private final Constructor<?> ctor;
	
	private final Integer varargsPosition;
	
	// When the constructor was found, we will have determined if arguments need to be converted for it
	// to be invoked. Conversion won't be cheap so let's only do it if necessary.
	private final int[] argsRequiringConversion;
	
	public ReflectiveConstructorExecutor(Constructor<?> ctor, int[] argsRequiringConversion) {
		this.ctor = ctor;
		if(ctor.isVarArgs()) {
			Class<?>[] paramTypes = ctor.getParameterTypes();
			this.varargsPosition = paramTypes.length - 1; 
		} else {
			this.varargsPosition = null;
		}
		this.argsRequiringConversion = argsRequiringConversion;
	}

	public TypedValue execute(EvaluationContext context, Object... arguments) throws AccessException {
		try {
			if(arguments != null) {
				ReflectionHelper.convertArguments(context.getTypeConverter(), arguments, 
						this.ctor, this.argsRequiringConversion, this.varargsPosition);
			} 
			if(this.ctor.isVarArgs()) {
				arguments = ReflectionHelper.setupArgumentsForVarargsInvocation(this.ctor.getParameterTypes(), arguments);
			}
			ReflectionUtils.makeAccessible(this.ctor);
			return new TypedValue(this.ctor.newInstance(arguments));
		} catch (Exception ex) {
			throw new AccessException("Problem invoking constructor: " + this.ctor, ex);
		}
	}

}
