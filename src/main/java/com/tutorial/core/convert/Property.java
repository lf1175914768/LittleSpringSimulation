package com.tutorial.core.convert;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

import com.tutorial.core.GenericTypeResolver;
import com.tutorial.core.MethodParameter;
import com.tutorial.util.ReflectionUtils;
import com.tutorial.util.StringUtils;

/**
 * A description of a JavaBeans Property that allows us to avoid a dependency on
 * <code>java.beans.PropertyDescriptor</code>. The <code>java.beans</code> package
 * is not available in a number of environments (e.g. Android, Java ME), so this is
 * desirable for portability of Spring's core conversion facility.
 *
 * <p>Used to build a TypeDescriptor from a property location.
 * The built TypeDescriptor can then be used to convert from/to the property type.
 *
 * @author Keith Donald
 * @since 3.1
 * @see TypeDescriptor#TypeDescriptor(Property)
 * @see TypeDescriptor#nested(Property, int)
 */
public final class Property {
	
	private final Class<?> objectType;
	private final String name;
	private final Method readMethod;
	private final Method writeMethod;
	private final MethodParameter methodParameter;
	private final Annotation[] annotations;
	
	public Property(Class<?> objectType, Method readMethod, Method writeMethod) {
		this.objectType = objectType;
		this.readMethod = readMethod;
		this.writeMethod = writeMethod;
		this.name = resolveName();
		this.methodParameter = resolveMethodParameter();
		this.annotations = resolveAnnotations();
	}

	public Method getReadMethod() {
		return readMethod;
	}

	public Method getWriteMethod() {
		return writeMethod;
	}
	
	public String getName() {
		return this.name;
	}
	
	public Class<?> getObjectType() {
		return objectType;
	}
	
	public Class<?> getType() {
		return this.methodParameter.getParameterType();
	}
	
	// package private
	
	MethodParameter getMethodParameter() {
		return this.methodParameter;
	}

	Annotation[] getAnnotations() {
		return this.annotations;
	}


	private Annotation[] resolveAnnotations() {
		Map<Class<?>, Annotation> annoMap = new LinkedHashMap<Class<?>, Annotation>();
		Method readMethod = getReadMethod();
		if(readMethod != null) {
			for(Annotation ann : readMethod.getAnnotations()) {
				annoMap.put(ann.annotationType(), ann);
			}
		}
		Method writeMethod = getWriteMethod();
		if(writeMethod != null) {
			for(Annotation ann : writeMethod.getAnnotations()) {
				annoMap.put(ann.annotationType(), ann);
			}
		}
		Field field = getField();
		if(field != null) {
			for(Annotation ann : field.getAnnotations()) {
				annoMap.put(ann.annotationType(), ann);
			}
		}
		return annoMap.values().toArray(new Annotation[annoMap.size()]);
	}
	
	private Field getField() {
		String name = getName();
		if(!StringUtils.hasLength(name)) {
			return null;
		}
		Class<?> declaringClass = declaringClass();
		Field field = ReflectionUtils.findField(declaringClass, name);
		if(field == null) {
			// same lenient fallback checking as in Cached Introspection Results...
			field = ReflectionUtils.findField(declaringClass, 
					   name.substring(0,1).toLowerCase() + name.substring(1));
			if(field == null) {
				field = ReflectionUtils.findField(declaringClass, 
						name.substring(0, 1).toUpperCase() + name.substring(1));
			}
		}
		return field;
	}

	private Class<?> declaringClass() {
		Method method;
		if((method = getReadMethod()) != null) {
			return method.getDeclaringClass();
		}
		else if((method = getWriteMethod()) != null){
			return method.getDeclaringClass();
		}
		throw new IllegalStateException("Neither ReadMethod nor WriteMethod has value");
	}

	private String resolveName() {
		if(this.readMethod != null) {
			int index = this.readMethod.getName().indexOf("get");
			if(index != -1) {
				index += 3;
			}
			else {
				index = this.readMethod.getName().indexOf("is");
				if(index == -1) {
					throw new IllegalArgumentException("Not a getter method");
				}
				index += 2;
			}
			return StringUtils.uncapitalize(this.readMethod.getName().substring(index));
		}
		else {
			int index = this.writeMethod.getName().indexOf("set");
			if(index == -1) {
				throw new IllegalArgumentException("Not a setter method");
			}
			index += 3;
			return StringUtils.uncapitalize(this.writeMethod.getName().substring(index));
		}
	}

	private MethodParameter resolveMethodParameter() {
		MethodParameter read = resolveReadMethodParameter();
		MethodParameter write = resolveWriteMethodParameter();
		if(read == null && write == null) {
			throw new IllegalStateException("Property is neither readable nor writable");
		}
		if(read != null && write != null &&
				!write.getParameterType().isAssignableFrom(read.getParameterType())) {
			throw new IllegalStateException("Write parameter is not assignable from read parameter");
		}
		return read != null ? read : write;
	}

	private MethodParameter resolveWriteMethodParameter() {
		if(getWriteMethod() == null) {
			return null;
		}
		return resolveParameterType(new MethodParameter(getWriteMethod(), 0));
	}

	private MethodParameter resolveReadMethodParameter() {
		if(getReadMethod() == null) {
			return null;
		}
		return resolveParameterType(new MethodParameter(getReadMethod(), -1));
	}

	private MethodParameter resolveParameterType(MethodParameter parameter) {
		GenericTypeResolver.resolveParameterType(parameter, getObjectType());
		return parameter;
	}

}
