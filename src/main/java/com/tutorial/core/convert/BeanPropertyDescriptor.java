package com.tutorial.core.convert;

import java.lang.annotation.Annotation;

import com.tutorial.core.GenericCollectionTypeResolver;
import com.tutorial.core.MethodParameter;

public class BeanPropertyDescriptor extends AbstractDescriptor {
	
	private final Property property;
	
	private final MethodParameter methodParameter;
	
	private final Annotation[] annotations;

	public BeanPropertyDescriptor(Property property) {
		super(property.getType());
		this.property = property;
		this.methodParameter = property.getMethodParameter();
		this.annotations = property.getAnnotations();
	}

	private BeanPropertyDescriptor(Class<?> type, Property property, MethodParameter methodParameter,
			Annotation[] annotations) {
		super(type);
		this.property = property;
		this.methodParameter = methodParameter;
		this.annotations = annotations;
	}

	@Override
	public Annotation[] getAnnotations() {
		return this.annotations;
	}

	@Override
	protected Class<?> resolveCollectionElementType() {
		return GenericCollectionTypeResolver.getCollectionParameterType(this.methodParameter);
	}

	@Override
	protected Class<?> resolveMapKeyType() {
		return GenericCollectionTypeResolver.getMapKeyParameterType(this.methodParameter);
	}

	@Override
	protected Class<?> resolveMapValueType() {
		return GenericCollectionTypeResolver.getMapValueParameterType(this.methodParameter);
	}

	@Override
	protected AbstractDescriptor nested(Class<?> type, int typeIndex) {
		MethodParameter methodParameter = new MethodParameter(this.methodParameter);
		methodParameter.increaseNestingLevel();
		methodParameter.setTypeIndexForCurrentLevel(typeIndex);
		return new BeanPropertyDescriptor(type, this.property, methodParameter, this.annotations); 
	}

}
