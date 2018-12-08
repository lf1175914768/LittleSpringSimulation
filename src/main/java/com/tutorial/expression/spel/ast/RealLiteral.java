package com.tutorial.expression.spel.ast;

import com.tutorial.expression.TypedValue;

/**
 * @author Liufeng
 * Created on 2018年11月11日 下午9:19:59
 */
public class RealLiteral extends Literal  {

	private final TypedValue value;
	
	public RealLiteral(String payLoad, int pos, double value) {
		super(payLoad, pos);
		this.value = new TypedValue(value);
	}

	@Override
	public TypedValue getLiteralValue() {
		return this.value;
	}

}
