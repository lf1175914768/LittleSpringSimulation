package com.tutorial.beans.factory.support;

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;

import com.tutorial.beans.factory.ObjectFactory;
import com.tutorial.util.ClassUtils;

/**
 * Utility class that contains various methods useful for
 * the implementation of autowire-capable bean factories.
 *
 * @author Juergen Hoeller
 * @author Mark Fisher
 * @since 1.1.2
 * @see AbstractAutowireCapableBeanFactory
 */
abstract class AutowireUtils {

	/**
	 * Sort the given constructors, preferring public constructors and "greedy" ones with
	 * a maximum number of arguments. The result will contain public constructors first,
	 * with decreasing number of arguments, then non-public constructors, again with
	 * decreasing number of arguments.
	 * @param constructors the constructor array to sort
	 */
	@SuppressWarnings("rawtypes")
	public static void sortConstructors(Constructor[] constructors) {
		Arrays.sort(constructors, new Comparator<Constructor>() {
			public int compare(Constructor o1, Constructor o2) {
				boolean p1 = Modifier.isPublic(o1.getModifiers());
				boolean p2 = Modifier.isPublic(o2.getModifiers());
				if(p1 != p2) {
					return p1 ? -1 : 1;
				}
				int op1 = o1.getParameterTypes().length;
				int op2 = o2.getParameterTypes().length;
				return (new Integer(op1)).compareTo(op2) * -1;
			}
		});
	}

	/**
	 * Sort the given factory methods, preferring public methods and "greedy" ones
	 * with a maximum of arguments. The result will contain public methods first,
	 * with decreasing number of arguments, then non-public methods, again with
	 * decreasing number of arguments.
	 * @param factoryMethods the factory method array to sort
	 */
	public static void sortFactoryMethods(Method[] factoryMethods) {
		Arrays.sort(factoryMethods, new Comparator<Method>() {
			public int compare(Method o1, Method o2) {
				boolean p1 = Modifier.isPublic(o1.getModifiers());
				boolean p2 = Modifier.isPublic(o2.getModifiers());
				if(p1 != p2) {
					return p1 ? -1 : 1;
				}
				int cp1 = o1.getParameterTypes().length;
				int cp2 = o2.getParameterTypes().length;
				return (new Integer(cp1)).compareTo(cp2) * -1;
			}
		});
	}

	/**
	 * Determine whether the given bean property is excluded from dependency checks.
	 * <p>This implementation excludes properties defined by CGLIB.
	 * @param pd the PropertyDescriptor of the bean property
	 * @return whether the bean property is excluded
	 */
	public static boolean isExcludedFromDependencyCheck(PropertyDescriptor pd) {
		Method wm = pd.getWriteMethod();
		if(wm == null) {
			return false;
		}
		if(!wm.getDeclaringClass().getName().contains("$$")) {
			// Not a CGLIB method so it's OK.
			return false;
		}
		
		// It was declared by CGLIB, but we might still want to autowire it 
		// if it was actually declared by the superclass.
		Class<?> superClass = wm.getDeclaringClass().getSuperclass();
		return !ClassUtils.hasMethod(superClass, wm.getName(), wm.getParameterTypes());
	}

	/**
	 * Return whether the setter method of the given bean property is defined
	 * in any of the given interfaces.
	 * @param pd the PropertyDescriptor of the bean property
	 * @param interfaces the Set of interfaces (Class objects)
	 * @return whether the setter method is defined by an interface
	 */
	public static boolean isSetterDefinedInInterface(
			PropertyDescriptor pd, Set<Class<?>> interfaces) {
		Method setter = pd.getWriteMethod();
		if(setter != null) {
			Class<?> targetClass = setter.getDeclaringClass();
			for(Class<?> ifc : interfaces) {
				if(ifc.isAssignableFrom(targetClass) &&
						ClassUtils.hasMethod(ifc, setter.getName(), setter.getParameterTypes())) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Resolve the given autowiring value against the given required type,
	 * e.g. an {@link ObjectFactory} value to its actual object result.
	 * @param autowiringValue the value to resolve
	 * @param requiredType the type to assign the result to
	 * @return the resolved value
	 */
	@SuppressWarnings("rawtypes")
	public static Object resolveAutowiringValue(Object autowiringValue, Class<?> requiredType) {
		if(autowiringValue instanceof ObjectFactory && !requiredType.isInstance(autowiringValue)) {
			ObjectFactory factory = (ObjectFactory) autowiringValue;
			if(autowiringValue instanceof Serializable && requiredType.isInterface()) {
				autowiringValue = Proxy.newProxyInstance(requiredType.getClassLoader(), 
						new Class<?>[] {requiredType}, new ObjectFactoryDelegatingInvocationHandler(factory));
			} else {
				return factory.getObject();
			}
		}
		return autowiringValue;
	}
	
	/**
	 * Reflective InvocationHandler for lazy access to the current target object.
	 */
	@SuppressWarnings({"serial", "rawtypes"})
	private static final class ObjectFactoryDelegatingInvocationHandler implements InvocationHandler, Serializable {

		private final ObjectFactory objectFactory;
		
		public ObjectFactoryDelegatingInvocationHandler(ObjectFactory objectFactory) {
			this.objectFactory = objectFactory;
		}
		
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			String methodName = method.getName();
			if(methodName.equals("equals")) {
				// Only consider equal when proxies are identical.
				return (proxy == args[0]);
			} else if(methodName.equals("hashCode")) {
				// use hashCode of proxy.
				return System.identityHashCode(proxy);
			} else if(methodName.equals("toString")) {
				return this.objectFactory.toString();
			} 
			try {
				return method.invoke(this.objectFactory.getObject(), args);
			} catch (InvocationTargetException ex) {
				throw ex.getTargetException();
			}
		}
		
	}
}
