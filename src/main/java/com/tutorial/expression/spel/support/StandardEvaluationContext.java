package com.tutorial.expression.spel.support;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.tutorial.core.convert.TypeDescriptor;
import com.tutorial.expression.BeanResolver;
import com.tutorial.expression.ConstructorResolver;
import com.tutorial.expression.EvaluationContext;
import com.tutorial.expression.MethodFilter;
import com.tutorial.expression.MethodResolver;
import com.tutorial.expression.OperatorOverloader;
import com.tutorial.expression.PropertyAccessor;
import com.tutorial.expression.TypeComparator;
import com.tutorial.expression.TypeConverter;
import com.tutorial.expression.TypeLocator;
import com.tutorial.expression.TypedValue;
import com.tutorial.util.Assert;

public class StandardEvaluationContext implements EvaluationContext {
	
	private TypedValue rootObject;
	
	private ReflectiveMethodResolver reflectiveMethodResolver;
	
	private List<ConstructorResolver> constructorResolvers;
	
	private List<MethodResolver> methodResolvers;
	
	private List<PropertyAccessor> propertyAccessors;
	
	private TypeLocator typeLocator;
	
	private BeanResolver beanResolver;
	
	private TypeConverter typeConverter;
	
	private TypeComparator typeComparator;
	
	private OperatorOverloader operatorOverloader;
	
	private final Map<String, Object> variable = new HashMap<String, Object>();
 	
	public StandardEvaluationContext() {
		setRootObject(null);
	}
	
	public StandardEvaluationContext(Object rootObject) {
		this();
		setRootObject(rootObject);
	}

	public void setRootObject(Object rootObject) {
		this.rootObject = (rootObject != null ? new TypedValue(rootObject) : TypedValue.NULL);
	}
	
	public void setRootObject(Object rootObject, TypeDescriptor typeDescriptor) {
		this.rootObject = new TypedValue(rootObject, typeDescriptor);
	}
	
	public TypedValue getRootObject() {
		return this.rootObject;
	}

	public void addConstructorResolver(ConstructorResolver resolver) {
		ensureConstructorResolversInitialized();
		this.constructorResolvers.add(this.constructorResolvers.size() - 1, resolver);
	}

	public boolean removeConstructorResolver(ConstructorResolver resolver) {
		ensureConstructorResolversInitialized();
		return this.constructorResolvers.remove(resolver);
	}
	
	public List<ConstructorResolver> getConstructorResolvers() {
		ensureConstructorResolversInitialized();
		return this.constructorResolvers;
	}
	
	public void setConstructorResolvers(List<ConstructorResolver> constructorResolvers) {
		this.constructorResolvers = constructorResolvers;
	}
	
	public void addMethodResolver(MethodResolver resolver) {
		ensureMethodResolverInitialized();
		this.methodResolvers.add(this.methodResolvers.size() - 1, resolver);
	}

	public boolean removeMethodResolver(MethodResolver methodResolver) {
		ensureMethodResolverInitialized();
		return this.methodResolvers.remove(methodResolver);
	}

	public List<MethodResolver> getMethodResolvers() {
		ensureMethodResolverInitialized();
		return this.methodResolvers;
	}
	
	public void setBeanResolver(BeanResolver beanResolver) {
		this.beanResolver = beanResolver;
	}
	
	public BeanResolver getBeanResolver() {
		return this.beanResolver;
	}
	
	public void setMethodResolvers(List<MethodResolver> methodResolvers) {
		this.methodResolvers = methodResolvers;
	}
	
	public void addPropertyAccessor(PropertyAccessor accessor) {
		ensurePropertyAccessorInitialized();
		this.propertyAccessors.add(this.propertyAccessors.size() - 1, accessor);
	}
	
	public boolean removePropertyAccessor(PropertyAccessor accessor) {
		return this.propertyAccessors.remove(accessor);
	}
	
	public List<PropertyAccessor> getPropertyAccessors() {
		ensurePropertyAccessorInitialized();
		return this.propertyAccessors;
	}
	
	public void setPropertyAccessors(List<PropertyAccessor> propertyAccessors) {
		this.propertyAccessors = propertyAccessors;
	}
	
	public void setTypeLocator(TypeLocator typeLocator) {
		Assert.notNull(typeLocator, "TypeLocator must not be null");
		this.typeLocator = typeLocator;
	}

	public TypeLocator getTypeLocator() {
		if(this.typeLocator == null) {
			this.typeLocator = new StandardTypeLocator();
		}
		return this.typeLocator;
	}
	
	public void setTypeConverter(TypeConverter typeConverter) {
		Assert.notNull(typeConverter, "TypeConverter must not be null");
		this.typeConverter = typeConverter;
	}

	public TypeConverter getTypeConverter() {
		if(this.typeConverter == null) {
			this.typeConverter = new StandardTypeConverter();
		}
		return this.typeConverter;
	}
	
	public void setTypeComparator(TypeComparator typeComparator) {
		Assert.notNull(typeComparator, "TypeComparator must not be null");
		this.typeComparator = typeComparator;
	}

	public TypeComparator getTypeComparator() {
		if(this.typeComparator == null) {
			this.typeComparator = new StandardTypeComparator();
		}
		return this.typeComparator;
	}
	
	public void setOperatorOverloader(OperatorOverloader operatorOverloader) {
		Assert.notNull(operatorOverloader, "OperatorOverloader must not be null");
		this.operatorOverloader = operatorOverloader;
	}

	public OperatorOverloader getOperatorOverloader() {
		if(this.operatorOverloader == null) {
			this.operatorOverloader = new StandardOperatorOverloader();
		}
		return this.operatorOverloader;
	}

	public void setVariable(String name, Object value) {
		this.variable.put(name, value);
	}
	
	public void setVariables(Map<String, Object> variables) {
		this.variable.putAll(variables);
	}
	
	public void registerFunction(String name, Method method) {
		this.variable.put(name, method);
	}

	public Object lookupVariable(String name) {
		return this.variable.get(name);
	}
	
	/**
	 * Register a MethodFilter which will be called during method resolution for the
	 * specified type.  The MethodFilter may remove methods and/or sort the methods
	 * which will then be used by SpEL as the candidates to look through for a match.
	 * 
	 * @param type the type for which the filter should be called
	 * @param filter a MethodFilter, or NULL to deregister a filter for the type
	 */
	public void registerMethodFilter(Class<?> type, MethodFilter filter) {
		ensureMethodResolverInitialized();
		reflectiveMethodResolver.registerMethodFilter(type, filter);
	}

	private void ensurePropertyAccessorInitialized() {
		if(this.propertyAccessors == null) {
			initializePropertyAccessors();
		}
	}

	private synchronized void initializePropertyAccessors() {
		if(this.propertyAccessors == null) {
			List<PropertyAccessor> defaultAccessors = new ArrayList<PropertyAccessor>();
			defaultAccessors.add(new ReflectivePropertyAccessor());
			this.propertyAccessors = defaultAccessors;
		}
	}

	private void ensureMethodResolverInitialized() {
		if(this.methodResolvers == null) {
			initializeMethodResolvers();
		}
	}

	private synchronized void initializeMethodResolvers() {
		if(this.methodResolvers == null) {
			List<MethodResolver> defaultResolvers = new ArrayList<MethodResolver>();
			defaultResolvers.add(new ReflectiveMethodResolver());
			this.methodResolvers = defaultResolvers;
		}
	}

	private void ensureConstructorResolversInitialized() {
		if(this.constructorResolvers == null) {
			initializeConstructorResolvers();
		}
	}

	private synchronized void initializeConstructorResolvers() {
		if(this.constructorResolvers == null) {
			List<ConstructorResolver> defaultResolvers = new ArrayList<ConstructorResolver>();
			defaultResolvers.add(new ReflectiveConstructorResolver());
			this.constructorResolvers = defaultResolvers;
		}
	}

}
