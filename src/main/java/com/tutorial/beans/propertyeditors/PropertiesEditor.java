package com.tutorial.beans.propertyeditors;

import java.beans.PropertyEditorSupport;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

/**
 * Custom {@link java.beans.PropertyEditor} for {@link Properties} objects.
 *
 * <p>Handles conversion from content {@link String} to <code>Properties</code> object.
 * Also handles {@link Map} to <code>Properties</code> conversion, for populating
 * a <code>Properties</code> object via XML "map" entries.
 *
 * <p>The required format is defined in the standard <code>Properties</code>
 * documentation. Each property must be on a new line.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see java.util.Properties#load
 */
public class PropertiesEditor extends PropertyEditorSupport {
	
	/**
	 * Convert {@link String} into {@link Properties}, considering it as
	 * properties content.
	 * @param text the text to be so converted
	 */
	@Override
	public void setAsText(String text) throws IllegalArgumentException {
		Properties props = new Properties();
		if(text != null) {
			try {
				props.load(new ByteArrayInputStream(text.getBytes("ISO-8859-1")));
			} catch (IOException e) {
				throw new IllegalArgumentException("Failed to parse [" + text + "] into Properties");
			}
		}
		setValue(props);
	}
	
	/**
	 * Take {@link Properties} as-is; convert {@link Map} into <code>Properties</code>.
	 */
	@Override
	public void setValue(Object value) {
		if(!(value instanceof Properties) && value instanceof Map) {
			Properties props = new Properties();
			props.putAll((Map<?, ?>) value);
			super.setValue(props);
		}
		else {
			super.setValue(value);
		}
	}
}
