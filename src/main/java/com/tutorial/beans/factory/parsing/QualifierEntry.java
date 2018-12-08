package com.tutorial.beans.factory.parsing;

import com.tutorial.util.StringUtils;

/**
 * {@link ParseState} entry representing an autowire candidate qualifier.
 * 
 * @author Mark Fisher
 * @since 2.5
 */
public class QualifierEntry implements ParseState.Entry {
	
	private String typeName;
	
	public QualifierEntry(String typeName) {
		if(!StringUtils.hasText(typeName)) {
			throw new IllegalArgumentException("Invalid qualifier type '" + typeName + "'");
		}
		this.typeName = typeName;
	}
	
	@Override
	public String toString() {
		return "Qualifier '" + this.typeName + "'";
	}
	
}
