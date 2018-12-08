package com.tutorial.core.convert.support;

import java.util.HashSet;
import java.util.Set;

import com.tutorial.core.convert.converter.Converter;

public class StringToBooleanConverter implements Converter<String, Boolean> {

	private static final Set<String> trueValues = new HashSet<String>(4);
	
	private static final Set<String> falseValues = new HashSet<String>(4);
	
	static {
		trueValues.add("true");
		trueValues.add("on");
		trueValues.add("yes");
		trueValues.add("1");
		
		falseValues.add("false");
		falseValues.add("off");
		falseValues.add("no");
		falseValues.add("0");
	}
	
	public Boolean convert(String source) {
		String value = source.trim();
		if("".equals(value)) {
			return null;
		}
		value = value.toLowerCase();
		if(trueValues.contains(value)) {
			return Boolean.TRUE;
		}
		else if(falseValues.contains(value)) {
			return Boolean.FALSE;
		}
		throw new IllegalArgumentException("Invalid boolean value '" + source + "'");
	}

}
