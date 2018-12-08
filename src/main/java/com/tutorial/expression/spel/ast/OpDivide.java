package com.tutorial.expression.spel.ast;

import com.tutorial.expression.EvaluationException;
import com.tutorial.expression.Operation;
import com.tutorial.expression.TypedValue;
import com.tutorial.expression.spel.ExpressionState;

/**
 * Implements division operator.
 *
 * @author Andy Clement
 * @since 3.0
 */
public class OpDivide extends Operator {

	public OpDivide(int pos, SpelNodeImpl... operands) {
		super("/", pos, operands);
	}

	@Override
	public TypedValue getValueInternal(ExpressionState state)
			throws EvaluationException {
		Object operandOne = getLeftOperand().getValueInternal(state).getValue();
		Object operandTwo = getRightOperand().getValueInternal(state).getValue();
		if (operandOne instanceof Number && operandTwo instanceof Number) {
			Number op1 = (Number) operandOne;
			Number op2 = (Number) operandTwo;
			if (op1 instanceof Double || op2 instanceof Double) {
				return new TypedValue(op1.doubleValue() / op2.doubleValue());
			} else if (op1 instanceof Long || op2 instanceof Long) {
				return new TypedValue(op1.longValue() / op2.longValue());
			} else { 
				return new TypedValue(op1.intValue() / op2.intValue());
			}
		}
		Object result = state.operate(Operation.DIVIDE, operandOne, operandTwo);
		return new TypedValue(result);
	}

}
