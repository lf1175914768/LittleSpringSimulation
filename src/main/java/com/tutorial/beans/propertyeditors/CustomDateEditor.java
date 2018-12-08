package com.tutorial.beans.propertyeditors;

import java.beans.PropertyEditorSupport;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

import com.tutorial.util.StringUtils;

public class CustomDateEditor extends PropertyEditorSupport {
	
	private final boolean allowEmpty;
	private final DateFormat dateFormat;
	
	public CustomDateEditor(DateFormat dateFormat, boolean allowEmpty) {
		this.dateFormat = dateFormat;
		this.allowEmpty = allowEmpty;
	}
	
	public void setAsText(String text) throws IllegalArgumentException {
		if(this.allowEmpty && !StringUtils.hasText(text)) {
			setValue(null);
		}
		else {
			try {
				setValue(this.dateFormat.parse(text));
			} catch (ParseException e) {
				throw new IllegalArgumentException("Could not parse date: " + e.getMessage()) ;
			}
		}
	}
	
	public String getAsText() {
		return (getValue() == null) ? "" : this.dateFormat.format((Date) getValue()); 
	}
	
}
