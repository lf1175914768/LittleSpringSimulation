package com.tutorial.core.convert.support;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import com.tutorial.core.CollectionFactory;
import com.tutorial.core.convert.ConversionService;
import com.tutorial.core.convert.TypeDescriptor;
import com.tutorial.core.convert.converter.ConditionalGenericConverter;

/**
 * Converts from a Collection to another Collection.
 *
 * <p>First, creates a new Collection of the requested targetType with a size equal to the
 * size of the source Collection. Then copies each element in the source collection to the
 * target collection. Will perform an element conversion from the source collection's
 * parameterized type to the target collection's parameterized type if necessary.
 *
 * @author Keith Donald
 * @since 3.0
 */
final class CollectionToCollectionConverter implements ConditionalGenericConverter {

	private final ConversionService conversionService;

	public CollectionToCollectionConverter(ConversionService conversionService) {
		this.conversionService = conversionService;
	}

	public Set<ConvertiblePair> getConvertibleTypes() {
		return Collections.singleton(new ConvertiblePair(Collection.class, Collection.class));
	}

	public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
		return ConversionUtils.canConvertElements(
				sourceType.getElementTypeDescriptor(), targetType.getElementTypeDescriptor(), this.conversionService);
	}

	public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
		if(source == null) {
			return null;
		}
		boolean copyRequired = !targetType.getType().isInstance(source);
		Collection<?> sourceCollection = (Collection<?>) source;
		if(!copyRequired && sourceCollection.isEmpty()) {
			return sourceCollection;
		}
		Collection<Object> target = CollectionFactory.createCollection(targetType.getType(), sourceCollection.size());
		if(targetType.getElementTypeDescriptor() == null) {
			for(Object element : sourceCollection) {
				target.add(element);
			}
		} else {
			for(Object element : sourceCollection) {
				Object targetElement = this.conversionService.convert(element, 
						sourceType.elementTypeDescriptor(element), targetType.getElementTypeDescriptor());
				target.add(targetElement);
				if(element != targetElement) {
					copyRequired = true;
				}
			}
		}
		return (copyRequired ? target : source);
	}

}
