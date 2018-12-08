package com.tutorial.expression.spel.ast;

import com.tutorial.expression.spel.support.BooleanTypedValue;

/**
 * @author Liufeng
 * Created on 2018年11月11日 下午9:25:49
 */
public class BooleanLiteral extends Literal {
	
	private final BooleanTypedValue value;

	public BooleanLiteral(String payLoad, int pos, boolean value) {
		super(payLoad, pos);
		this.value = BooleanTypedValue.forValue(value);
	}

	@Override
	public BooleanTypedValue getLiteralValue() {
		return this.value;
	}

}
