package com.tutorial.expression.spel.ast;

import com.tutorial.expression.EvaluationException;
import com.tutorial.expression.Operation;
import com.tutorial.expression.TypedValue;
import com.tutorial.expression.spel.ExpressionState;

/**
 * The power operator.
 * 
 * @author Andy Clement
 * @since 3.0
 */
public class OperatorPower extends Operator {
	
	public OperatorPower(int pos, SpelNodeImpl... operands) {
		super("^", pos, operands);
	}

	@Override
	public TypedValue getValueInternal(ExpressionState state) 
			throws EvaluationException {
		SpelNodeImpl leftOp = getLeftOperand();
		SpelNodeImpl rightOp = getRightOperand();
	
		Object operandOne = leftOp.getValueInternal(state).getValue();
		Object operandTwo = rightOp.getValueInternal(state).getValue();
		if (operandOne instanceof Number && operandTwo instanceof Number) {
			Number op1 = (Number) operandOne;
			Number op2 = (Number) operandTwo;
			if (op1 instanceof Double || op2 instanceof Double) {
				return new TypedValue(Math.pow(op1.doubleValue(),op2.doubleValue()));
			} else if (op1 instanceof Long || op2 instanceof Long) {
				double d= Math.pow(op1.longValue(), op2.longValue());
				return new TypedValue((long)d);
			} else {
				double d= Math.pow(op1.longValue(), op2.longValue());
				if (d > Integer.MAX_VALUE) {
					return new TypedValue((long)d);
				} else {
					return new TypedValue((int)d);
				}
			}
		}
		return state.operate(Operation.POWER, operandOne, operandTwo);
	}

}
