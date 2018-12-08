package com.tutorial.core.convert.support;

import com.tutorial.core.convert.converter.Converter;

/**
 * Simply calls {@link Object#toString()} to convert a source Object to a String.
 * @author Keith Donald
 * @since 3.0
 */
public class ObjectToStringConverter implements Converter<Object, String>{

	public String convert(Object source) {
		return source.toString();
	}

}
