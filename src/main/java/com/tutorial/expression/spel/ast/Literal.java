package com.tutorial.expression.spel.ast;

import com.tutorial.expression.EvaluationException;
import com.tutorial.expression.TypedValue;
import com.tutorial.expression.spel.ExpressionState;
import com.tutorial.expression.spel.InternalParseException;
import com.tutorial.expression.spel.SpelMessage;
import com.tutorial.expression.spel.SpelParseException;

public abstract class Literal extends SpelNodeImpl {
	
	protected String literalValue;

	public Literal(String payLoad, int pos) {
		super(pos);
		this.literalValue = payLoad;
	}
	
	@Override
	public TypedValue getValueInternal(ExpressionState expressionState) throws EvaluationException {
		return getLiteralValue();
	}
	
	@Override
	public String toStringAST() {
		return toString();
	}
	
	@Override
	public String toString() {
		return getLiteralValue().getValue().toString();
	}
	
	public static Literal getIntLiteral(String numberToken, int pos, int radix) {
		try {
			int value = Integer.parseInt(numberToken, radix);
			return new IntLiteral(numberToken, pos, value);
		} catch (NumberFormatException nfe) {
			throw new InternalParseException(new SpelParseException(pos >> 16, 
					nfe, SpelMessage.NOT_AN_INTEGER, numberToken));
		}
	}
	
	public static Literal getLongLiteral(String numberToken, int pos, int radix) {
		try {
			long value = Long.parseLong(numberToken, radix);
			return new LongLiteral(numberToken, pos, value);
		} catch (NumberFormatException nfe) {
			throw new InternalParseException(new SpelParseException(pos>>16, 
					nfe, SpelMessage.NOT_A_LONG, numberToken));
		}
	}
	
	public abstract TypedValue getLiteralValue();
	
	public static Literal getRealLiteral(String numberToken, int pos, boolean isFloat) {
		try {
			if(isFloat) {
				float value = Float.parseFloat(numberToken);
				return new RealLiteral(numberToken, pos, value);
			} else {
				double value = Double.parseDouble(numberToken);
				return new RealLiteral(numberToken, pos, value);
			}
		} catch (NumberFormatException nfe) {
			throw new InternalParseException(new SpelParseException(pos>>16, nfe, SpelMessage.NOT_A_REAL, numberToken));
		} 
	}

}
