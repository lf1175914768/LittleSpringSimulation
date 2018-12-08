package com.tutorial.expression.spel.support;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.tutorial.core.MethodParameter;
import com.tutorial.core.convert.TypeDescriptor;
import com.tutorial.expression.AccessException;
import com.tutorial.expression.EvaluationContext;
import com.tutorial.expression.EvaluationException;
import com.tutorial.expression.MethodExecutor;
import com.tutorial.expression.MethodFilter;
import com.tutorial.expression.MethodResolver;
import com.tutorial.expression.TypeConverter;
import com.tutorial.expression.spel.SpelEvaluationException;
import com.tutorial.expression.spel.SpelMessage;
import com.tutorial.util.CollectionUtils;

/**
 * A method resolver that uses reflection to locate the method that should be invoked.
 *
 * @author Andy Clement
 * @author Juergen Hoeller
 * @since 3.0
 */
public class ReflectiveMethodResolver implements MethodResolver {
	
	private static final Method[] NO_METHODS = new Method[0]; 
	
	private Map<Class<?>, MethodFilter> filters = null;
	
	// Using distance will ensure a more accurate match is discovered, 
		// more closely following the Java rules.
	private boolean useDistance = false;
	
	public ReflectiveMethodResolver() {}
	
	/**
	 * This constructors allows the ReflectiveMethodResolver to be configured such that it will
	 * use a distance computation to check which is the better of two close matches (when there
	 * are multiple matches).  Using the distance computation is intended to ensure matches
	 * are more closely representative of what a Java compiler would do when taking into 
	 * account boxing/unboxing and whether the method candidates are declared to handle a
	 * supertype of the type (of the argument) being passed in.
	 * @param useDistance true if distance computation should be used when calculating matches
	 */
	public ReflectiveMethodResolver(boolean useDistance) {
		this.useDistance = useDistance;
	}

	/**
	 * Locate a method on a type. There are three kinds of match that might occur:
	 * <ol>
	 * <li>An exact match where the types of the arguments match the types of the constructor
	 * <li>An in-exact match where the types we are looking for are subtypes of those defined on the constructor
	 * <li>A match where we are able to convert the arguments into those expected by the constructor,
	 * according to the registered type converter.
	 * </ol>
	 */
	public MethodExecutor resolve(EvaluationContext context, Object targetObject, String name,
			List<TypeDescriptor> argumentTypes) throws AccessException {
		try {
			TypeConverter typeConverter = context.getTypeConverter();
			Class<?> type = (targetObject instanceof Class ? (Class<?>) targetObject : targetObject.getClass());
			Method[] methods = type.getMethods();
			
			// If a filter is registered for this type, call it.
			MethodFilter filter = (this.filters != null ? this.filters.get(type) : null);
			if(filter != null) {
				List<Method> methodsForFiltering = new ArrayList<Method>();
				for(Method method : methods) {
					methodsForFiltering.add(method);
				}
				List<Method> methodsFiltered = filter.filter(methodsForFiltering);
				if(CollectionUtils.isEmpty(methodsFiltered)) {
					methods = NO_METHODS;
				} else {
					methods = methodsFiltered.toArray(new Method[methodsFiltered.size()]);
				}
			}
			
			Arrays.sort(methods, new Comparator<Method>() {
				public int compare(Method m1, Method m2) {
					int m1p1 = m1.getParameterTypes().length;
					int m2p2 = m2.getParameterTypes().length;
					return (new Integer(m1p1)).compareTo(m2p2);
				}
			});
			
			Method closeMatch = null;
			int closeMatchDistance = Integer.MAX_VALUE;
			int[] argsToConvert = null;
			Method matchRequiringConversion = null;
			boolean multipleOptions = false;
			
			for(Method method : methods) {
				if(method.isBridge()) {
					continue;
				}
				if(method.getName().equals(name)) {
					Class<?>[] paramTypes = method.getParameterTypes();
					List<TypeDescriptor> paramDescriptors = new ArrayList<TypeDescriptor>(paramTypes.length);
					for(int i = 0; i < paramTypes.length; i++) {
						paramDescriptors.add(new TypeDescriptor(new MethodParameter(method, i)));
					}
					ReflectionHelper.ArgumentsMatchInfo matchInfo = null;
					if(method.isVarArgs() && argumentTypes.size() >= (paramTypes.length - 1)) {
						// *sigh* complicated
						matchInfo = ReflectionHelper.compareArguments(paramDescriptors, argumentTypes, typeConverter);
					} else if(paramTypes.length == argumentTypes.size()) {
						// name and parameter number match, check the arguments.
						matchInfo = ReflectionHelper.compareArguments(paramDescriptors, argumentTypes, typeConverter);
					}
					if(matchInfo != null) {
						if(matchInfo.kind == ReflectionHelper.ArgsMatchKind.EXACT) {
							return new ReflectiveMethodExecutor(method, null);
						} else if(matchInfo.kind == ReflectionHelper.ArgsMatchKind.CLOSE) {
							if(!useDistance) {
								closeMatch = method;
							} else {
								int matchDistance = ReflectionHelper.getTypeDifferenceWeight(paramDescriptors, argumentTypes);
								if(matchDistance < closeMatchDistance) {
									// this is a better match {
									closeMatchDistance = matchDistance;
									closeMatch = method;
								}
							}
						} else if(matchInfo.kind == ReflectionHelper.ArgsMatchKind.REQUIRES_CONVERSION) {
							if(matchRequiringConversion != null) {
								multipleOptions = true;
							}
							argsToConvert = matchInfo.argsRequiringConversion;
							matchRequiringConversion = method;
						}
					}
				}
			}
			if(closeMatch != null) {
				return new ReflectiveMethodExecutor(closeMatch, null);
			} else if(matchRequiringConversion != null) {
				if(multipleOptions) {
					throw new SpelEvaluationException(SpelMessage.MULTIPLE_POSSIBLE_METHODS, name);
				}
				return new ReflectiveMethodExecutor(matchRequiringConversion, argsToConvert);
			} else {
				return null;
			}
		} catch (EvaluationException e) {
			throw new AccessException("Failed to resolve method", e);
		}
	}

	public void registerMethodFilter(Class<?> type, MethodFilter filter) {
		if(this.filters == null) {
			this.filters = new HashMap<Class<?>, MethodFilter>();
		}
		if(filter == null) {
			this.filters.remove(type);
		} else {
			this.filters.put(type, filter);
		}
	}
	
}
 