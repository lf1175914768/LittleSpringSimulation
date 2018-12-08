package com.tutorial.core.convert.support;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import com.tutorial.core.convert.ConversionService;
import com.tutorial.core.convert.TypeDescriptor;
import com.tutorial.core.convert.converter.ConditionalGenericConverter;

/**
 * Converts a Collection to an Object by returning the first collection element after converting it to the desired targetType.
 *
 * @author Keith Donald
 * @since 3.0
 */
final class CollectionToObjectConverter implements ConditionalGenericConverter {

	private final ConversionService conversionService;

	public CollectionToObjectConverter(ConversionService conversionService) {
		this.conversionService = conversionService;
	}

	public Set<ConvertiblePair> getConvertibleTypes() {
		return Collections.singleton(new ConvertiblePair(Collection.class, Object.class));
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
		Collection<?> sourceCollection = (Collection<?>) source;
		if (sourceCollection.size() == 0) {
			return null;
		}
		Object firstElement = sourceCollection.iterator().next();
		return this.conversionService.convert(firstElement, sourceType.elementTypeDescriptor(firstElement), targetType);
	}

}
