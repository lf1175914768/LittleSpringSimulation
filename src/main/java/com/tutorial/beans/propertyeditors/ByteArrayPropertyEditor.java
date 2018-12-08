package com.tutorial.beans.propertyeditors;

import java.beans.PropertyEditorSupport;

/**
 * Editor for byte arrays. Strings will simply be converted to
 * their corresponding byte representations.
 *
 * @author Juergen Hoeller
 * @since 1.0.1
 * @see java.lang.String#getBytes
 */
public class ByteArrayPropertyEditor extends PropertyEditorSupport {
	
	@Override
	public void setAsText(String text) {
		setValue(text != null ? text.getBytes() : null);
	}
	
	@Override
	public String getAsText() {
		byte[] value = (byte[]) getValue();
		return (value != null ) ? new String(value) : "";
	}
}
