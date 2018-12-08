package com.tutorial.beans.propertyeditors;

import java.beans.PropertyEditorSupport;

import com.tutorial.util.StringUtils;

/**
 * Property editor that trims Strings.
 *
 * <p>Optionally allows transforming an empty string into a <code>null</code> value.
 * Needs to be explicitly registered, e.g. for command binding.
 *
 * @author Juergen Hoeller
 * @see com.tutorial.validation.DataBinder#registerCustomEditor
 * @see com.tutorial.web.servlet.mvc.BaseCommandController#initBinder
 */
public class StringTrimmerEditor extends PropertyEditorSupport {
	
	private final String charsToDelete;
	
	private final boolean emptyAsNull;

	/**
	 * Create a new StringTrimmerEditor.
	 * @param emptyAsNull <code>true</code> if an empty String is to be
	 * transformed into <code>null</code>
	 */
	public StringTrimmerEditor(boolean emptyAsNull) {
		this.charsToDelete = null;
		this.emptyAsNull = emptyAsNull;
	}
	
	/**
	 * Create a new StringTrimmerEditor.
	 * @param charsToDelete a set of characters to delete, in addition to
	 * trimming an input String. Useful for deleting unwanted line breaks:
	 * e.g. "\r\n\f" will delete all new lines and line feeds in a String.
	 * @param emptyAsNull <code>true</code> if an empty String is to be
	 * transformed into <code>null</code>
	 */
	public StringTrimmerEditor(String charsToDelete, boolean emptyAsNull) {
		this.charsToDelete = charsToDelete;
		this.emptyAsNull = emptyAsNull;
	}
	
	@Override
	public void setAsText(String text) throws IllegalArgumentException {
		if(text == null) {
			setValue(null);
		} else {
			String value = text.trim();
			if(this.charsToDelete != null) {
				value = StringUtils.deleteAny(value, this.charsToDelete);
			}
			if(this.emptyAsNull && "".equals(value)) {
				setValue(null);
			} else {
				setValue(value);
			}
		}
	}
	
	@Override
	public String getAsText() {
		Object value = getValue();
		return value != null ? value.toString() : "";
	}
	
}
