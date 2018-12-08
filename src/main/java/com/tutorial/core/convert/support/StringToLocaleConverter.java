package com.tutorial.core.convert.support;

import java.util.Locale;

import com.tutorial.core.convert.converter.Converter;
import com.tutorial.util.StringUtils;

/**
 * Converts a String to a Locale.
 *
 * @author Keith Donald
 * @since 3.0
 */
final class StringToLocaleConverter implements Converter<String, Locale>{

	public Locale convert(String source) {
		return StringUtils.parseLocaleString(source);
	}

}
