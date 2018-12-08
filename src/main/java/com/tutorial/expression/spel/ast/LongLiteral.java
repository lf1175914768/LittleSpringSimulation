package com.tutorial.expression.spel.ast;

import com.tutorial.expression.TypedValue;

/**
 * Expression language AST node that represents a long integer literal.
 * 
 * @author Liufeng
 * Created on 2018年11月11日 下午9:14:39
 */
public class LongLiteral extends Literal {
	
	private final TypedValue value;

	public LongLiteral(String payLoad, int pos, long value) {
		super(payLoad, pos);
		this.value = new TypedValue(value); 
	}

	@Override
	public TypedValue getLiteralValue() {
		return this.value;
	}

}
