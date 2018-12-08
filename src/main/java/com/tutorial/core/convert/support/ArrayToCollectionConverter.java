package com.tutorial.core.convert.support;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import com.tutorial.core.CollectionFactory;
import com.tutorial.core.convert.ConversionService;
import com.tutorial.core.convert.TypeDescriptor;
import com.tutorial.core.convert.converter.ConditionalGenericConverter;

/**
 * Converts an Array to a Collection.
 *
 * <p>First, creates a new Collection of the requested targetType.
 * Then adds each array element to the target collection.
 * Will perform an element conversion from the source component type to the collection's parameterized type if necessary.
 * 
 * @author Keith Donald
 * @since 3.0
 */
final class ArrayToCollectionConverter implements ConditionalGenericConverter {

	private final ConversionService conversionService;
	
	public ArrayToCollectionConverter(ConversionService conversion) {
		this.conversionService = conversion;
	}
	
	public Set<ConvertiblePair> getConvertibleTypes() {
		return Collections.singleton(new ConvertiblePair(Object[].class, Collection.class));
	}

	public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
		if(source == null) {
			return null;
		}
		int length = Array.getLength(source);
		Collection<Object> target = CollectionFactory.createCollection(targetType.getType(), length);
		if(targetType.getElementTypeDescriptor() == null) {
			for(int i = 0; i < length; i++) {
				Object sourceElement = Array.get(source, i);
				target.add(sourceElement);
			}
		}
		else {
			for(int i = 0; i < length; i++) {
				Object sourceElement = Array.get(source, i);
				Object targetElement = this.conversionService.convert(sourceElement, 
						sourceType.elementTypeDescriptor(sourceElement), targetType.getElementTypeDescriptor());
				target.add(targetElement);
			}
		}
		return target;
	}

	public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
		return ConversionUtils.canConvertElements(
				sourceType.getElementTypeDescriptor(), targetType.getElementTypeDescriptor(), this.conversionService);
	}

}
