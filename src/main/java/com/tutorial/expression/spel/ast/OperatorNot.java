package com.tutorial.expression.spel.ast;

import com.tutorial.core.convert.TypeDescriptor;
import com.tutorial.expression.EvaluationException;
import com.tutorial.expression.TypedValue;
import com.tutorial.expression.spel.ExpressionState;
import com.tutorial.expression.spel.SpelEvaluationException;
import com.tutorial.expression.spel.SpelMessage;
import com.tutorial.expression.spel.support.BooleanTypedValue;

/**
 * Represents a NOT operation.
 *
 * @author Andy Clement
 * @author Mark Fisher
 * @since 3.0
 */
public class OperatorNot extends SpelNodeImpl {

	public OperatorNot(int pos, SpelNodeImpl operand) {
		super(pos, operand);
	}
	
	@Override
	public TypedValue getValueInternal(ExpressionState state) 
			throws EvaluationException {
		try {
			TypedValue typedValue = children[0].getValueInternal(state);
			if (TypedValue.NULL.equals(typedValue)) {
				throw new SpelEvaluationException(SpelMessage.TYPE_CONVERSION_ERROR, "null", "boolean");
			}
			boolean value = (Boolean) state.convertValue(typedValue, TypeDescriptor.valueOf(Boolean.class));
			return BooleanTypedValue.forValue(!value);
		}
		catch (SpelEvaluationException see) {
			see.setPosition(getChild(0).getStartPosition());
			throw see;
		}
	}

	@Override
	public String toStringAST() {
		StringBuilder sb = new StringBuilder();
		sb.append("!").append(getChild(0).toStringAST());
		return sb.toString();
	}

}
