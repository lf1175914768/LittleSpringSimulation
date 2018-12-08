package com.tutorial.core.convert.support;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import com.tutorial.core.convert.ConversionService;
import com.tutorial.core.convert.TypeDescriptor;
import com.tutorial.core.convert.converter.ConditionalGenericConverter;
import com.tutorial.util.ObjectUtils;

/**
 * Converts an Array to another Array.
 * First adapts the source array to a List, then delegates to {@link CollectionToArrayConverter} to perform the target array conversion. 
 * 
 * @author Keith Donald
 * @since 3.0
 */
public class ArrayToArrayConverter implements ConditionalGenericConverter{

	private final CollectionToArrayConverter helperConverter;
	
	public ArrayToArrayConverter(ConversionService conversionService) {
		this.helperConverter = new CollectionToArrayConverter(conversionService);
	}
	
	public Set<ConvertiblePair> getConvertibleTypes() {
		return Collections.singleton(new ConvertiblePair(Object[].class, Object[].class));
	}

	public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
		return this.helperConverter.convert(Arrays.asList(ObjectUtils.toObjectArray(source)), sourceType, targetType);
	}

	public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
		return this.helperConverter.matches(sourceType, targetType);
	}

}
