package com.tutorial.core.convert;

import java.lang.annotation.Annotation;

import com.tutorial.core.GenericCollectionTypeResolver;
import com.tutorial.core.MethodParameter;

public class ParameterDescriptor extends AbstractDescriptor {
	
	private final MethodParameter methodParameter;

	private ParameterDescriptor(Class<?> type, MethodParameter methodParameter) {
		super(type);
		this.methodParameter = methodParameter;
	}
	
	public ParameterDescriptor(MethodParameter methodParameter) {
		super(methodParameter.getParameterType());
		if(methodParameter.getNestingLevel() != 1) {
			throw new IllegalArgumentException("MethodParameter argument must have its nestingLevel set to 1");
		}
		this.methodParameter = methodParameter;
	}

	@Override
	public Annotation[] getAnnotations() {
		if(this.methodParameter.getParameterIndex() == -1) {
			return TypeDescriptor.nullSafeAnnotations(this.methodParameter.getMethodAnnotations());
		}
		else {
			return TypeDescriptor.nullSafeAnnotations(this.methodParameter.getParameterAnnotations());
		}
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
		return new ParameterDescriptor(type, methodParameter);
	}
	
	

}
