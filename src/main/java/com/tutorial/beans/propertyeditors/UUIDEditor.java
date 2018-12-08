package com.tutorial.beans.propertyeditors;

import java.beans.PropertyEditorSupport;
import java.util.UUID;

import com.tutorial.util.StringUtils;

/**
 * Editor for <code>java.util.UUID</code>, translating UUID
 * String representations into UUID objects and back.
 *
 * @author Juergen Hoeller
 * @since 3.0.1
 * @see java.util.UUID
 */
public class UUIDEditor extends PropertyEditorSupport {

	@Override
	public void setAsText(String text) throws IllegalArgumentException {
		if(StringUtils.hasText(text)) {
			setValue(UUID.fromString(text));
		} else {
			setValue(null);
		}
	}
	
	@Override
	public String getAsText() {
		UUID uuid = (UUID) getValue();
		return uuid != null ? uuid.toString() : "";
	}
	
}
