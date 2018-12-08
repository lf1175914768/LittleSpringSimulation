package com.tutorial.expression.spel.ast;

import com.tutorial.expression.EvaluationException;
import com.tutorial.expression.TypedValue;
import com.tutorial.expression.spel.ExpressionState;
import com.tutorial.expression.spel.SpelEvaluationException;
import com.tutorial.expression.spel.SpelMessage;
import com.tutorial.expression.spel.support.BooleanTypedValue;

public class OperatorInstanceof extends Operator {
	
	public OperatorInstanceof(int pos, SpelNodeImpl... operands) {
		super("instanceof", pos, operands);
	}

	/**
	 * Compare the left operand to see it is an instance of the type specified as the right operand.
	 * The right operand must be a class.
	 * @param state the expression state
	 * @return true if the left operand is an instanceof of the right operand, otherwise false
	 * @throws EvaluationException if there is a problem evaluating the expression
	 */
	@Override
	public TypedValue getValueInternal(ExpressionState expressionState)
			throws EvaluationException {
		TypedValue left = getLeftOperand().getValueInternal(expressionState);
		TypedValue right = getRightOperand().getValueInternal(expressionState);
		Object leftValue = left.getValue();
		Object rightValue = right.getValue();
		if(leftValue == null) {
			return BooleanTypedValue.FALSE;    // null is not an instanceof anything
		}
		if(rightValue == null || !(rightValue instanceof Class<?>)) {
			throw new SpelEvaluationException(getRightOperand().getStartPosition(),
					SpelMessage.INSTANCEOF_OPERATOR_NEEDS_CLASS_OPERAND,
					(rightValue == null ? "null" : rightValue.getClass().getName()));
		}
		Class<?> rightClass = (Class<?>) rightValue;
		return BooleanTypedValue.forValue(rightClass.isAssignableFrom(leftValue.getClass())); 
	}

}
