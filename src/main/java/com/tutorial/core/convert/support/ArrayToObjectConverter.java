package com.tutorial.core.convert.support;

import java.lang.reflect.Array;
import java.util.Collections;
import java.util.Set;

import com.tutorial.core.convert.ConversionService;
import com.tutorial.core.convert.TypeDescriptor;
import com.tutorial.core.convert.converter.ConditionalGenericConverter;

/**
 * Converts an Array to an Object by returning the first array element after converting it to the desired targetType.
 * 
 * @author Keith Donald
 * @since 3.0
 */
final class ArrayToObjectConverter implements ConditionalGenericConverter {

	private final ConversionService conversionService;

	public ArrayToObjectConverter(ConversionService conversionService) {
		this.conversionService = conversionService;
	}

	public Set<ConvertiblePair> getConvertibleTypes() {
		return Collections.singleton(new ConvertiblePair(Object[].class, Object.class));
	}

	public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
		return ConversionUtils.canConvertElements(sourceType.getElementTypeDescriptor(), targetType, this.conversionService);
	}

	public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
		if (source == null) {
			return null;
		}
		if (sourceType.isAssignableTo(targetType)) {
			return source;
		}
		if (Array.getLength(source) == 0) {
			return null;
		}
		Object firstElement = Array.get(source, 0);
		return this.conversionService.convert(firstElement, sourceType.elementTypeDescriptor(firstElement), targetType);
	}
}
