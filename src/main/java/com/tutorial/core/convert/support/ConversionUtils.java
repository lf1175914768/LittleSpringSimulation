package com.tutorial.core.convert.support;

import com.tutorial.core.convert.ConversionFailedException;
import com.tutorial.core.convert.ConversionService;
import com.tutorial.core.convert.TypeDescriptor;
import com.tutorial.core.convert.converter.GenericConverter;

abstract class ConversionUtils {

	public static Object invokeConverter(GenericConverter converter, Object source, TypeDescriptor sourceType,
			TypeDescriptor targetType) {
		try {
			return converter.convert(source, sourceType, targetType);
		} catch (ConversionFailedException e) {
			throw e;
		} catch(Exception e) {
			throw new ConversionFailedException(sourceType, targetType, source, e);
		}
	}

	public static boolean canConvertElements(TypeDescriptor sourceElementType, TypeDescriptor targetElementType,
			ConversionService conversionService) {
		if(targetElementType == null || sourceElementType == null) {
			return true;
		}
		if(conversionService != null) {
			if(conversionService.canConvert(sourceElementType, targetElementType) ||
					sourceElementType.getType().isAssignableFrom(targetElementType.getType())) {
				return true;
			}
		}
		return false;
	}

}
