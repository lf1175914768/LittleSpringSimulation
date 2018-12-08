package com.tutorial.core.convert.support;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Properties;

import com.tutorial.core.convert.converter.Converter;

/**
 * Converts a String to a Properties by calling Properties#load(java.io.InputStream).
 * Uses ISO-8559-1 encoding required by Properties.
 * 
 * @author Keith Donald
 * @since 3.0
 */
final class StringToPropertiesConverter implements Converter<String, Properties> {

	public Properties convert(String source) {
		try {
			Properties props = new Properties();
			props.load(new ByteArrayInputStream(source.getBytes("ISO-8859-1")));
			return props;
		}  catch (IOException e) {
			throw new IllegalArgumentException("Failed to parse [" + source + "] into Properties", e);
		}
	}

}
