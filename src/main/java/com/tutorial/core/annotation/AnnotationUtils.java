package com.tutorial.core.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import com.tutorial.util.Assert;

/**
 * General utility methods for working with annotations, handling bridge methods (which the compiler
 * generates for generic declarations) as well as super methods (for optional &quot;annotation inheritance&quot;).
 * Note that none of this is provided by the JDK's introspection facilities themselves.
 *
 * <p>As a general rule for runtime-retained annotations (e.g. for transaction control, authorization or service
 * exposure), always use the lookup methods on this class (e.g., {@link #findAnnotation(Method, Class)}, {@link
 * #getAnnotation(Method, Class)}, and {@link #getAnnotations(Method)}) instead of the plain annotation lookup
 * methods in the JDK. You can still explicitly choose between lookup on the given class level only ({@link
 * #getAnnotation(Method, Class)}) and lookup in the entire inheritance hierarchy of the given method ({@link
 * #findAnnotation(Method, Class)}).
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @author Mark Fisher
 * @author Chris Beams
 * @since 2.0
 * @see java.lang.reflect.Method#getAnnotations()
 * @see java.lang.reflect.Method#getAnnotation(Class)
 */
public abstract class AnnotationUtils {
	
	/** The attribute name for annotations with a single element */
	static final String VALUE = "value";

	/**
	 * Find a single {@link Annotation} of <code>annotationType</code> from the supplied {@link Class},
	 * traversing its interfaces and superclasses if no annotation can be found on the given class itself.
	 * <p>This method explicitly handles class-level annotations which are not declared as
	 * {@link java.lang.annotation.Inherited inherited} <i>as well as annotations on interfaces</i>.
	 * <p>The algorithm operates as follows: Searches for an annotation on the given class and returns
	 * it if found. Else searches all interfaces that the given class declares, returning the annotation
	 * from the first matching candidate, if any. Else proceeds with introspection of the superclass
	 * of the given class, checking the superclass itself; if no annotation found there, proceeds
	 * with the interfaces that the superclass declares. Recursing up through the entire superclass
	 * hierarchy if no match is found.
	 * @param clazz the class to look for annotations on
	 * @param annotationType the annotation class to look for
	 * @return the annotation found, or <code>null</code> if none found
	 */
	public static <A extends Annotation> A findAnnotation(Class<?> clazz, Class<A> annotationType) {
		Assert.notNull(clazz, "Class must not be null");
		A annotation = clazz.getAnnotation(annotationType);
		if(annotation != null) {
			return annotation;
		}
		for(Class<?> ifc : clazz.getInterfaces()) {
			annotation = findAnnotation(ifc, annotationType);
			if(annotation != null) {
				return annotation;
			}
		}
		if(!Annotation.class.isAssignableFrom(clazz)) {
			for(Annotation ann : clazz.getAnnotations())  {
				annotation = findAnnotation(ann.annotationType(), annotationType);
				if(annotation != null) {
					return annotation;
				}
			}
		}
		Class<?> superClass = clazz.getSuperclass();
		if(superClass == null || superClass == Object.class) {
			return null;
		}
		return findAnnotation(superClass, annotationType);
	}

	/**
	 * Retrieve the given annotation's attributes as a Map, preserving all attribute types as-is.
	 * @param annotation the annotation to retrieve the attributes for
	 * @return the Map of annotation attributes, with attribute names as keys and
	 * corresponding attribute values as values
	 */
	public static Map<String, Object> getAnnotationAttributes(Annotation annotation) {
		return getAnnotationAttributes(annotation, false);
	}

	/**
	 * Retrieve the given annotation's attributes as a Map.
	 * @param annotation the annotation to retrieve the attributes for
	 * @param classValuesAsString whether to turn Class references into Strings (for compatibility with
	 * {@link org.springframework.core.type.AnnotationMetadata} or to preserve them as Class references
	 * @return the Map of annotation attributes, with attribute names as keys and
	 * corresponding attribute values as values
	 */
	public static Map<String, Object> getAnnotationAttributes(Annotation annotation, boolean classValueAsString) {
		Map<String, Object> attrs = new HashMap<String, Object>();
		Method[] methods = annotation.annotationType().getDeclaredMethods();
		for(Method method : methods) {
			if(method.getParameterTypes().length == 0 && method.getReturnType() != void.class) {
				try {
					Object value = method.invoke(annotation);
					if(classValueAsString) {
						if(value instanceof Class) {
							value = ((Class<?>) value).getName();
						} else if(value instanceof Class[]) { 
							Class<?>[] clazzArray = (Class<?>[]) value;
							String[] newValue = new String[clazzArray.length];
							for(int i = 0; i < clazzArray.length; i++) {
								newValue[i] = clazzArray[i].getName();
							}
							value = newValue;
						}
					}
					attrs.put(method.getName(), value);
				} catch (Exception e) {
					throw new IllegalStateException("Could not obtain annotation attribute values", e);
				}
			}
		}
		return attrs;
	}

	/**
	 * Retrieve the <em>default value</em> of a named Annotation attribute, given an annotation instance.
	 * @param annotation the annotation instance from which to retrieve the default value
	 * @param attributeName the name of the attribute value to retrieve
	 * @return the default value of the named attribute, or <code>null</code> if not found
	 * @see #getDefaultValue(Class, String)
	 */
	public static Object getDefaultValue(Annotation annotation, String attributeName) {
		return getDefaultValue(annotation.annotationType(), attributeName);
	}

	/**
	 * Retrieve the <em>default value</em> of a named Annotation attribute, given the {@link Class annotation type}.
	 * @param annotationType the <em>annotation type</em> for which the default value should be retrieved
	 * @param attributeName the name of the attribute value to retrieve.
	 * @return the default value of the named attribute, or <code>null</code> if not found
	 * @see #getDefaultValue(Annotation, String)
	 */
	public static Object getDefaultValue(Class<? extends Annotation> annotationType, String attributeName) {
		try {
			Method method = annotationType.getDeclaredMethod(attributeName, new Class[0]);
			return method.getDefaultValue();
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Retrieve the <em>value</em> of the <code>&quot;value&quot;</code> attribute of a
	 * single-element Annotation, given an annotation instance.
	 * @param annotation the annotation instance from which to retrieve the value
	 * @return the attribute value, or <code>null</code> if not found
	 * @see #getValue(Annotation, String)
	 */
	public static Object getValue(Annotation annotation) {
		return getValue(annotation, VALUE);
	}

	/**
	 * Retrieve the <em>value</em> of a named Annotation attribute, given an annotation instance.
	 * @param annotation the annotation instance from which to retrieve the value
	 * @param attributeName the name of the attribute value to retrieve
	 * @return the attribute value, or <code>null</code> if not found
	 * @see #getValue(Annotation)
	 */
	public static Object getValue(Annotation annotation, String attributeName) {
		try {
			Method method = annotation.annotationType().getDeclaredMethod(attributeName, new Class[0]);
			return method.invoke(annotation);
		} catch (Exception e) {
			return null;
		}
	}
	
	

}
