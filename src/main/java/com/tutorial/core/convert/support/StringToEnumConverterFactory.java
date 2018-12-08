package com.tutorial.core.convert.support;

import com.tutorial.core.convert.converter.Converter;
import com.tutorial.core.convert.converter.ConverterFactory;

/**
 * Converts from a String to a java.lang.Enum by calling {@link Enum#valueOf(Class, String)}.
 *
 * @author Keith Donald
 * @since 3.0
 */
@SuppressWarnings("rawtypes")
final class StringToEnumConverterFactory implements ConverterFactory<String, Enum> {

	public <T extends Enum> Converter<String, T> getConverter(Class<T> targetType) {
		return new StringToEnum<T>(targetType);
	}
	
	private static final class StringToEnum<T extends Enum> implements Converter<String, T> {

		private final Class<T> targetType;
		
		public StringToEnum(Class<T> targetType) {
			this.targetType = targetType;
		}
		
		@SuppressWarnings("unchecked")
		public T convert(String source) {
			if(source.length() == 0) {
				//It's an empty enum identifier : reset the enum value to null;
				return null;
			}
			return (T) Enum.valueOf(targetType, source.trim()); 
		}
		
	}

}
