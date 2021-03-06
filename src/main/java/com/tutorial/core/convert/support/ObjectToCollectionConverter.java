package com.tutorial.core.convert.support;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import com.tutorial.core.CollectionFactory;
import com.tutorial.core.convert.ConversionService;
import com.tutorial.core.convert.TypeDescriptor;
import com.tutorial.core.convert.converter.ConditionalGenericConverter;

/**
 * Converts an Object to a single-element Collection containing the Object.
 * Will convert the Object to the target Collection's parameterized type if necessary.
 *
 * @author Keith Donald
 * @author Juergen Hoeller
 * @since 3.0
 */
final class ObjectToCollectionConverter implements ConditionalGenericConverter {

	private final ConversionService conversionService;

	public ObjectToCollectionConverter(ConversionService conversionService) {
		this.conversionService = conversionService;
	}

	public Set<ConvertiblePair> getConvertibleTypes() {
		return Collections.singleton(new ConvertiblePair(Object.class, Collection.class));
	}

	public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
		return ConversionUtils.canConvertElements(sourceType, targetType.getElementTypeDescriptor(), this.conversionService);
	}

	@SuppressWarnings("unchecked")
	public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
		if (source == null) {
			return null;
		}
		Collection<Object> target = CollectionFactory.createCollection(targetType.getType(), 1);
		if (targetType.getElementTypeDescriptor() == null || targetType.getElementTypeDescriptor().isCollection()) {
			target.add(source);
		}
		else {
			Object singleElement = this.conversionService.convert(source, sourceType, targetType.getElementTypeDescriptor());
			target.add(singleElement);
		}
		return target;
	}

}
