package com.tutorial.expression.spel.ast;

import com.tutorial.expression.TypedValue;

/**
 * @author Liufeng
 * Created on 2018年11月11日 下午9:30:09
 */
public class StringLiteral extends Literal {
	
	private final TypedValue value;

	public StringLiteral(String payLoad, int pos, String value) {
		super(payLoad, pos);
		value = value.substring(1, value.length() - 1);
		this.value = new TypedValue(value.replaceAll("''", "'"));
	}

	@Override
	public TypedValue getLiteralValue() {
		return this.value;
	}

	@Override
	public String toString() {
		return "'" + getLiteralValue().getValue() + "'";
	}
	
}
