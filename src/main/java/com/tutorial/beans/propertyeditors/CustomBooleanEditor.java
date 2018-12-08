package com.tutorial.beans.propertyeditors;

import java.beans.PropertyEditorSupport;

import com.tutorial.util.StringUtils;

public class CustomBooleanEditor extends PropertyEditorSupport {
	
	public static final String VALUE_TRUE = "true";
	public static final String VALUE_FALSE = "false";
	
	public static final String VALUE_ON = "on";
	public static final String VALUE_OFF = "off";

	public static final String VALUE_YES = "yes";
	public static final String VALUE_NO = "no";

	public static final String VALUE_1 = "1";
	public static final String VALUE_0 = "0";
	
	private final String trueString;
	
	private final String falseString;
	
	private final boolean allowEmpty;

	/**
	 * Create a new CustomBooleanEditor instance, with "true"/"on"/"yes"
	 * and "false"/"off"/"no" as recognized String values.
	 * <p>The "allowEmpty" parameter states if an empty String should
	 * be allowed for parsing, i.e. get interpreted as null value.
	 * Else, an IllegalArgumentException gets thrown in that case.
	 * @param allowEmpty if empty strings should be allowed
	 */
	public CustomBooleanEditor(boolean allowEmpty) {
		this(null, null, allowEmpty);
	}
	
	/**
	 * Create a new CustomBooleanEditor instance,
	 * with configurable String values for true and false.
	 * <p>The "allowEmpty" parameter states if an empty String should
	 * be allowed for parsing, i.e. get interpreted as null value.
	 * Else, an IllegalArgumentException gets thrown in that case.
	 * @param trueString the String value that represents true:
	 * for example, "true" (VALUE_TRUE), "on" (VALUE_ON),
	 * "yes" (VALUE_YES) or some custom value
	 * @param falseString the String value that represents false:
	 * for example, "false" (VALUE_FALSE), "off" (VALUE_OFF),
	 * "no" (VALUE_NO) or some custom value
	 * @param allowEmpty if empty strings should be allowed
	 * @see #VALUE_TRUE
	 * @see #VALUE_FALSE
	 * @see #VALUE_ON
	 * @see #VALUE_OFF
	 * @see #VALUE_YES
	 * @see #VALUE_NO
	 */
	public CustomBooleanEditor(String trueString, String falseString, boolean allowEmpty) {
		this.trueString = trueString;
		this.falseString = falseString;
		this.allowEmpty = allowEmpty;
	}
	
	@Override
	public void setAsText(String text) throws IllegalArgumentException {
		String input = (text != null ? text.trim() : null);
		if(this.allowEmpty && !StringUtils.hasText(input)) {
			setValue(null);
		} else if(this.trueString != null && input.equalsIgnoreCase(this.trueString)) {
			setValue(Boolean.TRUE);
		} else if(this.falseString != null && input.equalsIgnoreCase(this.falseString)) {
			setValue(Boolean.FALSE);
		} else if(this.trueString == null && (input.equalsIgnoreCase(VALUE_TRUE) || input.equalsIgnoreCase(VALUE_ON) ||
				input.equalsIgnoreCase(VALUE_YES) || input.equalsIgnoreCase(VALUE_1))) {
			setValue(Boolean.TRUE);
		} else if(this.falseString == null && (input.equalsIgnoreCase(VALUE_FALSE) || input.equalsIgnoreCase(VALUE_OFF) ||
				input.equalsIgnoreCase(VALUE_NO) || input.equalsIgnoreCase(VALUE_0))) {
			setValue(Boolean.FALSE);
		} else {
			throw new IllegalArgumentException("Invalid boolean value [" + text + "]");
		}
	}
	
	@Override
	public String getAsText() {
		if(Boolean.TRUE.equals(getValue())) {
			return (this.trueString != null ? this.trueString : VALUE_TRUE);
		} else if(Boolean.FALSE.equals(getValue())) {
			return (this.falseString != null ? this.falseString : VALUE_FALSE);
		} else {
			return "";
		}
	}
}
