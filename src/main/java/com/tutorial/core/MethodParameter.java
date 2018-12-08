package com.tutorial.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.Map;

import com.tutorial.util.Assert;

/**
 * Helper class that encapsulates the specification of a method parameter, i.e.
 * a Method or Constructor plus a parameter index and a nested type index for
 * a declared generic type. Useful as a specification object to pass along.
 *
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @author Andy Clement
 * @since 2.0
 * @see GenericCollectionTypeResolver
 */
public class MethodParameter {
	
	private final Method method;
	
	private final Constructor<?> constructor;
	
	private Class<?> parameterType;
	
	private Type genericParameterType;
	
	private Annotation[] parameterAnnotations;
	
	private final int parameterIndex;
	
	private ParameterNameDiscoverer parameterNameDiscoverer;
	
	private String parameterName;
	
	Map<Integer, Integer> typeIndexesPerLevel;
	
	Map<TypeVariable<?>, Type> typeVariableMap;
	
	private int nestingLevel = 1;
	
	private int hash = 0;
	
	/**
	 * Create a new MethodParameter for the given method, with nesting level 1.
	 * @param method the Method to specify a parameter for
	 * @param parameterIndex the index of the parameter
	 */
	public MethodParameter(Method method, int parameterIndex) {
		this(method, parameterIndex, 1);
	}

	/**
	 * Create a new MethodParameter for the given method.
	 * @param method the Method to specify a parameter for
	 * @param parameterIndex the index of the parameter
	 * (-1 for the method return type; 0 for the first method parameter,
	 * 1 for the second method parameter, etc)
	 * @param nestingLevel the nesting level of the target type
	 * (typically 1; e.g. in case of a List of Lists, 1 would indicate the
	 * nested List, whereas 2 would indicate the element of the nested List)
	 */
	public MethodParameter(Method method, int parameterIndex, int nestingLevel) {
		Assert.notNull(method, "Method must not be null");
		this.method = method;
		this.parameterIndex = parameterIndex;
		this.nestingLevel = nestingLevel;
		this.constructor = null;
	}
	
	/**
	 * Create a new MethodParameter for the given constructor, with nesting level 1.
	 * @param constructor the Constructor to specify a parameter for
	 * @param parameterIndex the index of the parameter
	 */
	@SuppressWarnings("rawtypes")
	public MethodParameter(Constructor constructor, int parameterIndex) {
		this(constructor, parameterIndex, 1);
	}

	/**
	 * Create a new MethodParameter for the given constructor.
	 * @param constructor the Constructor to specify a parameter for
	 * @param parameterIndex the index of the parameter
	 * @param nestingLevel the nesting level of the target type
	 * (typically 1; e.g. in case of a List of Lists, 1 would indicate the
	 * nested List, whereas 2 would indicate the element of the nested List)
	 */
	@SuppressWarnings("rawtypes")
	public MethodParameter(Constructor constructor, int parameterIndex, int nestingLevel) {
		this.constructor = constructor;
		this.parameterIndex = parameterIndex;
		this.nestingLevel = nestingLevel;
		this.method = null;		
	}
	
	public MethodParameter(MethodParameter original) {
		Assert.notNull(original, "Original must not be null");
		this.method = original.getMethod();
		this.constructor = original.getConstructor();
		this.nestingLevel = original.getNestingLevel();
		this.parameterIndex = original.getParameterIndex();
		this.parameterType = original.getParameterType();
		this.genericParameterType = original.getGenericParameterType();
		this.parameterAnnotations = original.getParameterAnnotations();
		this.parameterNameDiscoverer = original.getParameterNameDiscoverer();
		this.parameterName = original.getParameterName();
		this.typeIndexesPerLevel = original.getTypeIndexesPerLevel();
		this.typeVariableMap = original.getTypeVariableMap();
		this.hash = original.getHash();
	}
	
	/**
	 * Return the class that declares the underlying Method or Constructor.
	 */
	public Class<?> getDeclaringClass() {
		return getMember().getDeclaringClass();
	}

	private Member getMember() {
		return this.method != null ? this.method : this.constructor;
	}

	public Method getMethod() {
		return method;
	}

	public Constructor<?> getConstructor() {
		return constructor;
	}

	public int getParameterIndex() {
		return parameterIndex;
	}

	public int getNestingLevel() {
		return nestingLevel;
	}

	public Class<?> getParameterType() {
		if(parameterType == null) {
			if(this.parameterIndex < 0) {
				this.parameterType = (this.method != null ? this.method.getReturnType() : null);
			}
			else {
				this.parameterType = (this.method != null ? 
						this.method.getParameterTypes()[parameterIndex] :
							this.constructor.getParameterTypes()[parameterIndex]);
			}
		}
		return this.parameterType;
	}

	public void setParameterType(Class<?> parameterType) {
		this.parameterType = parameterType;
	}

	/**
	 * Return the generic type of the method/constructor parameter.
	 * @return the parameter type (never <code>null</code>)
	 */
	public Type getGenericParameterType() {
		if(this.genericParameterType == null) {
			if(this.parameterIndex < 0) {
				this.genericParameterType = (this.method != null ? this.method.getGenericReturnType() : null);
			}
			else {
				this.genericParameterType = (this.method != null ? 
						this.method.getGenericParameterTypes()[parameterIndex] :
							this.constructor.getGenericParameterTypes()[parameterIndex]);
			}
		}
		return this.genericParameterType;
	}
	
	/**
	 * Return the annotations associated with the target method/constructor itself.
	 */
	public Annotation[] getMethodAnnotations() {
		return getAnnotatedElement().getAnnotations();
	}

	/**
	 * Returns the wrapped annotated element.
	 * @return the annotated element
	 */
	private AnnotatedElement getAnnotatedElement() {
		return this.method != null ? this.method : this.constructor;
	}
	
	/**
	 * Return the method/constructor annotation of the given type, if available.
	 * @param annotationType the annotation type to look for
	 * @return the annotation object, or <code>null</code> if not found
	 */
	public <T extends Annotation> T getMethodAnnotation(Class<T> annotationType) {
		return getAnnotatedElement().getAnnotation(annotationType);
	}

	/**
	 * Return the annotations associated with the specific method/constructor parameter.
	 */
	public Annotation[] getParameterAnnotations() {
		if(parameterAnnotations == null) {
			Annotation[][] annotationArray = (this.method != null ?
					this.method.getParameterAnnotations() : this.constructor.getParameterAnnotations());
			if(this.parameterIndex >= 0 && this.parameterIndex < annotationArray.length) {
				this.parameterAnnotations = annotationArray[parameterIndex];
			}
			else {
				this.parameterAnnotations = new Annotation[0];
			}
		}
		return this.parameterAnnotations;
	}
	
	/**
	 * Return the parameter annotation of the given type, if available.
	 * @param annotationType the annotation type to look for
	 * @return the annotation object, or <code>null</code> if not found
	 */
	@SuppressWarnings("unchecked")
	public <T extends Annotation> T getParameterAnnotation(Class<T> annotationType) {
		Annotation[] anns = getParameterAnnotations();
		for(Annotation ann : anns) {
			if(annotationType.isInstance(ann)) {
				return (T) ann;
			}
		}
		return null;
	}
	
	public boolean hasParameterAnnotations() {
		return getParameterAnnotations().length > 0;
	}
	
	public <T extends Annotation> boolean hasParameterAnnotation(Class<T> annotationType) {
		return getParameterAnnotation(annotationType) != null;
	}
	
	/**
	 * Initialize parameter name discovery for this method parameter.
	 * <p>This method does not actually try to retrieve the parameter name at
	 * this point; it just allows discovery to happen when the application calls
	 * {@link #getParameterName()} (if ever).
	 */
	public void initParameterNameDiscovery(ParameterNameDiscoverer parameterNameDiscoverer) {
		this.parameterNameDiscoverer = parameterNameDiscoverer;
	}
	
	/**
	 * Return the name of the method/constructor parameter.
	 * @return the parameter name (may be <code>null</code> if no
	 * parameter name metadata is contained in the class file or no
	 * {@link #initParameterNameDiscovery ParameterNameDiscoverer}
	 * has been set to begin with)
	 */
	public String getParameterName() {
		if(this.parameterNameDiscoverer != null) {
			String[] parameterNames = (this.method != null) ? 
					this.parameterNameDiscoverer.getParameterNames(this.method) :
						this.parameterNameDiscoverer.getParameterNames(this.constructor);
			if(parameterNames != null) {
				this.parameterName = parameterNames[parameterIndex];
			}
			this.parameterNameDiscoverer = null;
		}
		return this.parameterName;
	}
	
	/**
	 * Increase this parameter's nesting level.
	 * @see #getNestingLevel()
	 */
	public void increaseNestingLevel() {
		this.nestingLevel++;
	}
	
	public ParameterNameDiscoverer getParameterNameDiscoverer() {
		return parameterNameDiscoverer;
	}

	/**
	 * Decrease this parameter's nesting level.
	 * @see #getNestingLevel()
	 */
	public void decreaseNestingLevel() {
		getTypeIndexesPerLevel().remove(this.nestingLevel);
		this.nestingLevel--;
	}

	public Map<Integer, Integer> getTypeIndexesPerLevel() {
		if(this.typeIndexesPerLevel == null) {
			this.typeIndexesPerLevel = new HashMap<Integer, Integer>(4);
		}
		return this.typeIndexesPerLevel;
	}
	
	/**
	 * Set the type index for the current nesting level.
	 * @param typeIndex the corresponding type index
	 * (or <code>null</code> for the default type index)
	 * @see #getNestingLevel()
	 */
	public void setTypeIndexForCurrentLevel(int typeIndex) {
		getTypeIndexesPerLevel().put(this.nestingLevel, typeIndex);
	}
	
	public Map<TypeVariable<?>, Type> getTypeVariableMap() {
		return typeVariableMap;
	}

	public int getHash() {
		return hash;
	}

	/**
	 * Return the type index for the current nesting level.
	 * @return the corresponding type index, or <code>null</code>
	 * if none specified (indicating the default type index)
	 * @see #getNestingLevel()
	 */
	public Integer 	getTypeIndexForCurrentLevel() {
		return getTypeIndexForLevel(this.nestingLevel);
	}

	/**
	 * Return the type index for the specified nesting level.
	 * @param nestingLevel the nesting level to check
	 * @return the corresponding type index, or <code>null</code>
	 * if none specified (indicating the default type index)
	 */
	private Integer getTypeIndexForLevel(int nestingLevel) {
		return getTypeIndexesPerLevel().get(nestingLevel);
	}
	
	/**
	 * Create a new MethodParameter for the given method or constructor.
	 * <p>This is a convenience constructor for scenarios where a
	 * Method or Constructor reference is treated in a generic fashion.
	 * @param methodOrConstructor the Method or Constructor to specify a parameter for
	 * @param parameterIndex the index of the parameter
	 * @return the corresponding MethodParameter instance
	 */
	public static MethodParameter forMethodOrConstructor(Object methodOrConstructor, int parameterIndex) {
		if(methodOrConstructor instanceof Method) {
			return new MethodParameter((Method) methodOrConstructor, parameterIndex);
		}
		else if(methodOrConstructor instanceof Constructor) {
			return new MethodParameter((Constructor<?>) methodOrConstructor, parameterIndex);
		}
		else {
			throw new IllegalArgumentException(
					"Given object [" + methodOrConstructor + "] is neither a Method nor a Constructor");
		}
	}
	
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		else if(obj != null && obj instanceof MethodParameter) {
			MethodParameter other = (MethodParameter) obj;
			return this.getParameterIndex() == other.getParameterIndex() &&
					this.getMember().equals(other.getMember());
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		int result = this.hash;
		if(result == 0) {
			result = getMember().hashCode();
			result = 31 * result + this.parameterIndex;
			this.hash = result;
		}
		return result;
	}
	
}
