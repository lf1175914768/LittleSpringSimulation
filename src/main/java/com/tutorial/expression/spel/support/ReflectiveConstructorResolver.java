package com.tutorial.expression.spel.support;

import java.lang.reflect.Constructor;
import java.rmi.AccessException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import com.tutorial.core.MethodParameter;
import com.tutorial.core.convert.TypeDescriptor;
import com.tutorial.expression.ConstructorExecutor;
import com.tutorial.expression.ConstructorResolver;
import com.tutorial.expression.EvaluationContext;
import com.tutorial.expression.EvaluationException;
import com.tutorial.expression.TypeConverter;

/**
 * A constructor resolver that uses reflection to locate the constructor that should be invoked
 * 
 * @author Andy Clement
 * @author Juergen Hoeller
 * @since 3.0
 */
public class ReflectiveConstructorResolver implements ConstructorResolver {

	/**
	 * Locate a constructor on the type. There are three kinds of match that might occur:
	 * <ol>
	 * <li>An exact match where the types of the arguments match the types of the constructor
	 * <li>An in-exact match where the types we are looking for are subtypes of those defined on the constructor
	 * <li>A match where we are able to convert the arguments into those expected by the constructor, according to the
	 * registered type converter.
	 * </ol>
	 */
	public ConstructorExecutor resolve(EvaluationContext context, String typeName, List<TypeDescriptor> argumentTypes)
			throws AccessException {
		try {
			TypeConverter typeConverter = context.getTypeConverter();
			Class<?> type = context.getTypeLocator().findType(typeName);
			Constructor<?>[] ctors = type.getConstructors();
			
			Arrays.sort(ctors, new Comparator<Constructor<?>>() {
				public int compare(Constructor<?> c1, Constructor<?> c2) {
					int c1p1 = c1.getParameterTypes().length;
					int c2p2 = c2.getParameterTypes().length;
					return (new Integer(c1p1)).compareTo(c2p2);
				}
			});
			
			Constructor<?> closeMatch = null;
			int[] argsToConvert = null;
			Constructor<?> matchRequiringConversion = null;
			
			for(Constructor<?> ctor : ctors) {
				Class<?>[] paramTypes = ctor.getParameterTypes();
				List<TypeDescriptor> paramDescriptors = new ArrayList<TypeDescriptor>(paramTypes.length);
				for(int i = 0; i < paramTypes.length; i++) {
					paramDescriptors.add(new TypeDescriptor(new MethodParameter(ctor, i)));
				}
				ReflectionHelper.ArgumentsMatchInfo matchInfo = null;
				if(ctor.isVarArgs() && argumentTypes.size() >= (paramTypes.length - 1)) {
					// *sigh* complicated.
					// Basically... we have to have all parameters match up until the varargs one, then the rest of what is
					// being provided should be
					// the same type whilst the final argument to the method must be an array of that (oh, how easy...not) -
					// or the final parameter
					// we are supplied does match exactly (it is an array already).
					matchInfo = ReflectionHelper.compareArgumentsVarargs(paramDescriptors, argumentTypes, typeConverter);
				} else if(paramTypes.length == argumentTypes.size()) {
					// worth a closer look
					matchInfo = ReflectionHelper.compareArguments(paramDescriptors, argumentTypes, typeConverter);
				}
				if(matchInfo != null) {
					if(matchInfo.kind == ReflectionHelper.ArgsMatchKind.EXACT) {
						return new ReflectiveConstructorExecutor(ctor, null);
					} else if(matchInfo.kind == ReflectionHelper.ArgsMatchKind.CLOSE) {
						closeMatch = ctor;
					} else if(matchInfo.kind == ReflectionHelper.ArgsMatchKind.REQUIRES_CONVERSION) {
						argsToConvert = matchInfo.argsRequiringConversion;
						matchRequiringConversion = ctor;
					}
				}
			}
			if(closeMatch != null) {
				return new ReflectiveConstructorExecutor(closeMatch, null);
			} else if(matchRequiringConversion != null) {
				return new ReflectiveConstructorExecutor(matchRequiringConversion, argsToConvert);
			} else {
				return null;
			}
		} catch (EvaluationException ex) {
			throw new AccessException("Failed to resolve constructor", ex);
		} 
	}

}
