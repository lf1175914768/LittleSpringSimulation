package com.tutorial.beans.factory.config;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Type;

import com.tutorial.core.GenericCollectionTypeResolver;
import com.tutorial.core.MethodParameter;
import com.tutorial.core.ParameterNameDiscoverer;
import com.tutorial.util.Assert;

/**
 * Descriptor for a specific dependency that is about to be injected.
 * Wraps a constructor parameter, a method parameter or a field,
 * allowing unified access to their metadata.
 *
 * @author Juergen Hoeller
 * @since 2.5
 */
public class DependencyDescriptor implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private transient MethodParameter methodParameter;
	
	private transient Field field;
	
	private transient Annotation[] fieldAnnotations;
	
	private Class<?> declaringClass;
	
	private String methodName;
	
	private Class<?>[] parameterTypes;
	
	private int parameterIndex;
	
	private final boolean required;
	
	private final boolean eager;

	private String fieldName;
	
	/**
	 * Create a new descriptor for a method or constructor parameter.
	 * Considers the dependency as 'eager'.
	 * @param methodParameter the MethodParameter to wrap
	 * @param required whether the dependency is required
	 */
	public DependencyDescriptor (MethodParameter methodParameter, boolean isRequired) {
		this(methodParameter, isRequired, true);
	}

	/**
	 * Create a new descriptor for a method or constructor parameter.
	 * @param methodParameter the MethodParameter to wrap
	 * @param required whether the dependency is required
	 * @param eager whether this dependency is 'eager' in the sense of
	 * eagerly resolving potential target beans for type matching
	 */
	public DependencyDescriptor(MethodParameter methodParameter, boolean required, boolean eager) {
		Assert.notNull(methodParameter, "MethodParameter must not be null");
		this.methodParameter = methodParameter;
		this.declaringClass = methodParameter.getDeclaringClass();
		if(this.methodParameter.getMethod() != null) {
			this.methodName = this.methodParameter.getMethod().getName();
			this.parameterTypes = this.methodParameter.getMethod().getParameterTypes();
		}
		else {
			this.parameterTypes = this.methodParameter.getConstructor().getParameterTypes();
		}
		this.parameterIndex = methodParameter.getParameterIndex();
		this.required = required;
		this.eager = eager;
	}
	
	public DependencyDescriptor(Field field, boolean required, boolean eager) {
		Assert.notNull(field, "Field must not be null");
		this.field = field;
		this.declaringClass = field.getDeclaringClass();
		this.fieldName = field.getName();
		this.required = required;
		this.eager = eager;
	}
	
	/**
	 * Return the wrapped MethodParameter, if any.
	 * <p>Note: Either MethodParameter or Field is available.
	 * @return the MethodParameter, or <code>null</code> if none
	 */
	public MethodParameter getMethodParameter() {
		return this.methodParameter;
	}

	/**
	 * Return the wrapped Field, if any.
	 * <p>Note: Either MethodParameter or Field is available.
	 * @return the Field, or <code>null</code> if none
	 */
	public Field getField() {
		return this.field;
	}
	
	/**
	 * Return whether this dependency is required.
	 */
	public boolean isRequired() {
		return this.required;
	}

	/**
	 * Return whether this dependency is 'eager' in the sense of
	 * eagerly resolving potential target beans for type matching.
	 */
	public boolean isEager() {
		return this.eager;
	}
	
	/**
	 * Initialize parameter name discovery for the underlying method parameter, if any.
	 * <p>This method does not actually try to retrieve the parameter name at
	 * this point; it just allows discovery to happen when the application calls
	 * {@link #getDependencyName()} (if ever).
	 */
	public void initParameterNameDiscovery(ParameterNameDiscoverer parameterNameDiscoverer) {
		if(this.methodParameter != null) {
			this.methodParameter.initParameterNameDiscovery(parameterNameDiscoverer);
		}
	}
	
	/**
	 * Determine the name of the wrapped parameter/field.
	 * @return the declared name (never <code>null</code>)
	 */
	public String getDependencyName() {
		return (this.field != null ? field.getName() : this.methodParameter.getParameterName());
	}
	
	/**
	 * Determine the declared (non-generic) type of the wrapped parameter/field.
	 * @return the declared type (never <code>null</code>)
	 */
	public Class<?> getDependencyType() {
		return (this.field != null ? this.field.getType() : this.methodParameter.getParameterType());
	}
	
	/**
	 * Determine the generic type of the wrapped parameter/field.
	 * @return the generic type (never <code>null</code>)
	 */
	public Type getGenericDependencyType() {
		return (this.field != null ? this.field.getGenericType() : this.methodParameter.getGenericParameterType());
	}	

	/**
	 * Determine the generic element type of the wrapped Collection parameter/field, if any.
	 * @return the generic type, or <code>null</code> if none
	 */
	public Class<?> getCollectionType() {
		return (this.field != null ? 
				GenericCollectionTypeResolver.getCollectionFieldType(this.field) :
				GenericCollectionTypeResolver.getCollectionParameterType(this.methodParameter));
	}
	
	/**
	 * Determine the generic key type of the wrapped Map parameter/field, if any.
	 * @return the generic type, or <code>null</code> if none
	 */
	public Class<?> getMapKeyType() {
		return (this.field != null ?
				GenericCollectionTypeResolver.getMapKeyFieldType(this.field) :
				GenericCollectionTypeResolver.getMapKeyParameterType(this.methodParameter));
	}
	
	/**
	 * Determine the generic value type of the wrapped Map parameter/field, if any.
	 * @return the generic type, or <code>null</code> if none
	 */
	public Class<?> getMapValueType() {
		return (this.field != null ?
				GenericCollectionTypeResolver.getMapValueFieldType(this.field) :
				GenericCollectionTypeResolver.getMapValueParameterType(this.methodParameter));
	}
	
	/**
	 * Obtain the annotations associated with the wrapped parameter/field, if any.
	 */
	public Annotation[] getAnnotations() {
		if (this.field != null) {
			if (this.fieldAnnotations == null) {
				this.fieldAnnotations = this.field.getAnnotations();
			}
			return this.fieldAnnotations;
		}
		else {
			return this.methodParameter.getParameterAnnotations();
		}
	}
	
	//---------------------------------------------------------------------
	// Serialization support
	//---------------------------------------------------------------------

	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
		ois.defaultReadObject();
		try {
			if(this.fieldName != null) {
				this.field = this.declaringClass.getDeclaredField(this.fieldName);
			}
			else if(this.methodName != null) {
				this.methodParameter = new MethodParameter(
						this.declaringClass.getDeclaredMethod(this.methodName, this.parameterTypes), this.parameterIndex);
			}
			else {
				this.methodParameter = new MethodParameter(
						this.declaringClass.getDeclaredConstructor(this.parameterTypes), this.parameterIndex);
			}

		} catch (Throwable e) {
			throw new IllegalStateException("Could not find original class structure", e);
		}
	}
	
}
