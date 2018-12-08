package com.tutorial.beans.propertyeditors;

import java.beans.PropertyEditorSupport;

/**
 * Editor for char arrays. Strings will simply be converted to
 * their corresponding char representations.
 *
 * @author Juergen Hoeller
 * @since 1.2.8
 * @see String#toCharArray()
 */
public class CharArrayPropertyEditor extends PropertyEditorSupport {

	@Override
	public void setAsText(String text) throws IllegalArgumentException {
		setValue(text != null ? text.toCharArray() : null);
	}
	
	@Override
	public String getAsText() {
		char[] value = (char[]) getValue();
		return value != null ? new String(value) : "";
	}
	
}
