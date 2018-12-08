package com.tutorial.expression.spel.support;

import com.tutorial.core.convert.ConversionException;
import com.tutorial.core.convert.ConversionService;
import com.tutorial.core.convert.ConverterNotFoundException;
import com.tutorial.core.convert.TypeDescriptor;
import com.tutorial.core.convert.support.DefaultConversionService;
import com.tutorial.expression.TypeConverter;
import com.tutorial.expression.spel.SpelEvaluationException;
import com.tutorial.expression.spel.SpelMessage;
import com.tutorial.util.Assert;

public class StandardTypeConverter implements TypeConverter {
	
	private static ConversionService defaultConversionService;
	
	private final ConversionService conversionService;
	
	public StandardTypeConverter() {
		synchronized(this) {
			if(defaultConversionService == null) {
				defaultConversionService = new DefaultConversionService();
			}
		}
		this.conversionService = defaultConversionService;
	}
	
	public StandardTypeConverter(ConversionService conversionService) {
		Assert.notNull(conversionService, "ConversionService must not be null");
		this.conversionService = conversionService;
	}

	public boolean canConvert(TypeDescriptor sourceType, TypeDescriptor targetType) {
		return this.conversionService.canConvert(sourceType, targetType);
	}

	public Object convertValue(Object value, TypeDescriptor sourceType, TypeDescriptor targetType) {
		try {
			return this.conversionService.convert(value, sourceType, targetType);
		} catch (ConverterNotFoundException e) {
			throw new SpelEvaluationException(e, SpelMessage.TYPE_CONVERSION_ERROR, 
					sourceType.toString(), targetType.toString());
		} catch(ConversionException e) {
			throw new SpelEvaluationException(e, SpelMessage.TYPE_CONVERSION_ERROR,
					sourceType.toString(), targetType.toString());
		}
	}

}
