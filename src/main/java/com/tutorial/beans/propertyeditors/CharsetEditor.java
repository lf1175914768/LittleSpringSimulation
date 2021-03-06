package com.tutorial.beans.propertyeditors;

import java.beans.PropertyEditorSupport;
import java.nio.charset.Charset;

import com.tutorial.util.StringUtils;

/**
 * Editor for <code>java.nio.charset.Charset<code>, translating charset
 * String representations into Charset objects and back.
 *
 * <p>Expects the same syntax as Charset's {@link java.nio.charset.Charset#name()},
 * e.g. <code>UTF-8</code>, <code>ISO-8859-16</code>, etc.
 *
 * @author Arjen Poutsma
 * @since 2.5.4
 * @see Charset
 */
public class CharsetEditor extends PropertyEditorSupport {
	
	@Override
	public void setAsText(String text) throws IllegalArgumentException {
		if(StringUtils.hasText(text)) {
			setValue(Charset.forName(text));
		} else {
			setValue(null);
		}
	}
	
	@Override
	public String getAsText() {
		Charset value = (Charset) getValue();
		return value != null ? value.name() : "";
	}

}
