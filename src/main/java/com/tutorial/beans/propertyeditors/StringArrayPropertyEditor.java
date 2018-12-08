package com.tutorial.beans.propertyeditors;

import java.beans.PropertyEditorSupport;

import com.tutorial.util.ObjectUtils;
import com.tutorial.util.StringUtils;

/**
 * Custom {@link java.beans.PropertyEditor} for String arrays.
 *
 * <p>Strings must be in CSV format, with a customizable separator.
 * By default values in the result are trimmed of whitespace.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Dave Syer
 * @see org.springframework.util.StringUtils#delimitedListToStringArray
 * @see org.springframework.util.StringUtils#arrayToDelimitedString
 */
public class StringArrayPropertyEditor extends PropertyEditorSupport {
	
	/**
	 * Default separator for splitting a String: a comma (",")
	 */
	public static final String DEFAULT_SEPARATOR = ",";
	
	private final String separator;
	
	private final String charsToDelete;
	
	private final boolean emptyArrayAsNull;
	
	private final boolean trimValues;
	
	/**
	 * Create a new StringArrayPropertyEditor with the default separator
	 * (a comma).
	 * <p>An empty text (without elements) will be turned into an empty array.
	 */
	public StringArrayPropertyEditor() {
		this(DEFAULT_SEPARATOR, null, false);
	}
	
	/**
	 * Create a new StringArrayPropertyEditor with the given separator.
	 * <p>An empty text (without elements) will be turned into an empty array.
	 * @param separator the separator to use for splitting a {@link String}
	 */
	public StringArrayPropertyEditor(String separator) {
		this(separator, null, false);
	}
	
	/**
	 * Create a new StringArrayPropertyEditor with the given separator.
	 * @param separator the separator to use for splitting a {@link String}
	 * @param emptyArrayAsNull <code>true</code> if an empty String array
	 * is to be transformed into <code>null</code>
	 */
	public StringArrayPropertyEditor(String separator, boolean emptyArrayAsNull) {
		this(separator, null, emptyArrayAsNull);
	}
	
	/**
	 * Create a new StringArrayPropertyEditor with the given separator.
	 * @param separator the separator to use for splitting a {@link String}
	 * @param emptyArrayAsNull <code>true</code> if an empty String array
	 * is to be transformed into <code>null</code>
	 * @param trimValues <code>true</code> if the values in the parsed arrays 
	 * are to be be trimmed of whitespace (default is true).
	 */
	public StringArrayPropertyEditor(String separator, boolean emptyArrayAsNull, boolean trimValues) {
		this(separator, null, emptyArrayAsNull, trimValues);
	}
	
	/**
	 * Create a new StringArrayPropertyEditor with the given separator.
	 * @param separator the separator to use for splitting a {@link String}
	 * @param charsToDelete a set of characters to delete, in addition to
	 * trimming an input String. Useful for deleting unwanted line breaks:
	 * e.g. "\r\n\f" will delete all new lines and line feeds in a String.
	 * @param emptyArrayAsNull <code>true</code> if an empty String array
	 * is to be transformed into <code>null</code>
	 */
	public StringArrayPropertyEditor(String separator, String charsToDelete, boolean emptyArrayAsNull) {
		this(separator, charsToDelete, emptyArrayAsNull, true);
	}
	
	/**
	 * Create a new StringArrayPropertyEditor with the given separator.
	 * @param separator the separator to use for splitting a {@link String}
	 * @param charsToDelete a set of characters to delete, in addition to
	 * trimming an input String. Useful for deleting unwanted line breaks:
	 * e.g. "\r\n\f" will delete all new lines and line feeds in a String.
	 * @param emptyArrayAsNull <code>true</code> if an empty String array
	 * is to be transformed into <code>null</code>
	 * @param trimValues <code>true</code> if the values in the parsed arrays 
	 * are to be be trimmed of whitespace (default is true).
	 */
	public StringArrayPropertyEditor(String separator, String charsToDelete, boolean emptyArrayAsNull, boolean trimValues) {
		this.separator = separator;
		this.charsToDelete = charsToDelete;
		this.emptyArrayAsNull = emptyArrayAsNull;
		this.trimValues = trimValues;
	}
	
	@Override
	public void setAsText(String text) throws IllegalArgumentException {
		String[] array = StringUtils.delimitedListToStringArray(text, this.separator, this.charsToDelete);
		if(trimValues) {
			array = StringUtils.trimArrayElements(array);
		} 
		if(this.emptyArrayAsNull && array.length == 0) {
			setValue(null);
		}
		setValue(array);
	}
	
	@Override
	public String getAsText() {
		return StringUtils.arrayToDelimitedString(ObjectUtils.toObjectArray(getValue()), this.separator);
	}
	
}
