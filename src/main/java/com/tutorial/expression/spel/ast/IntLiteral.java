package com.tutorial.expression.spel.ast;

import com.tutorial.expression.TypedValue;

/**
 * @author Liufeng
 * Created on 2018年11月11日 下午9:07:41
 */
public class IntLiteral extends Literal {
	
	private final TypedValue value;

	IntLiteral(String payLoad, int pos, int value) {
		super(payLoad, pos);
		this.value = new TypedValue(value);
	}

	@Override
	public TypedValue getLiteralValue() {
		return this.value;
	}

}
