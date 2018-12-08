package com.tutorial.core.convert.support;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Properties;

import com.tutorial.core.convert.converter.Converter;

/**
 * Converts from a Properties to a String by calling {@link Properties#store(java.io.OutputStream, String)}.
 * Decodes with the ISO-8859-1 charset before returning the String.
 *
 * @author Keith Donald
 * @since 3.0
 */
final class PropertiesToStringConverter implements Converter<Properties, String>{

	public String convert(Properties source) {
		try {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			source.store(os, null);
			return os.toString("ISO-8859-1");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			throw new IllegalArgumentException("Failed to store [" + source + "] into String", e);
		}
	}

}
