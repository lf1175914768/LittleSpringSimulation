package com.tutorial.core.convert.support;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import com.tutorial.core.CollectionFactory;
import com.tutorial.core.convert.ConversionService;
import com.tutorial.core.convert.TypeDescriptor;
import com.tutorial.core.convert.converter.ConditionalGenericConverter;
import com.tutorial.util.StringUtils;

/**
 * Converts a comma-delimited String to a Collection.
 * If the target collection element type is declared, only matches if String.class can be converted to it.
 *
 * @author Keith Donald
 * @since 3.0
 */
final class StringToCollectionConverter implements ConditionalGenericConverter {

	private final ConversionService conversionService;

	public StringToCollectionConverter(ConversionService conversionService) {
		this.conversionService = conversionService;
	}

	public Set<ConvertiblePair> getConvertibleTypes() {
		return Collections.singleton(new ConvertiblePair(String.class, Collection.class));
	}

	public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
		if (targetType.getElementTypeDescriptor() != null) {
			return this.conversionService.canConvert(sourceType, targetType.getElementTypeDescriptor());
		} else {
			return true;
		}
	}

	@SuppressWarnings("unchecked")
	public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {		
		if (source == null) {
			return null;
		}
		String string = (String) source;
		String[] fields = StringUtils.commaDelimitedListToStringArray(string);
		Collection<Object> target = CollectionFactory.createCollection(targetType.getType(), fields.length);
		if (targetType.getElementTypeDescriptor() == null) {
			for (String field : fields) {
				target.add(field.trim());
			}						
		} else {
			for (String field : fields) {
				Object targetElement = this.conversionService.convert(field.trim(), sourceType, targetType.getElementTypeDescriptor());
				target.add(targetElement);
			}			
		}
		return target;
	}

}
