package com.tutorial.expression.spel.ast;

import com.tutorial.expression.TypedValue;

/**
 * @author Andy Clement
 * @since 3.0
 */

public class NullLiteral extends Literal {

	public NullLiteral(int pos) {
		super(null, pos);
	}

	@Override
	public TypedValue getLiteralValue() {
		return TypedValue.NULL;
	}
	
	@Override
	public String toString() {
		return "null";
	}
	
}
