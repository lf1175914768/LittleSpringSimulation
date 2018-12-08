package com.tutorial.expression.spel.ast;

import com.tutorial.expression.EvaluationException;
import com.tutorial.expression.Operation;
import com.tutorial.expression.TypedValue;
import com.tutorial.expression.spel.ExpressionState;

/**
 * Implements the multiply operator. Conversions and promotions:
 * http://java.sun.com/docs/books/jls/third_edition/html/conversions.html Section 5.6.2:
 *
 * <p>If any of the operands is of a reference type, unboxing conversion (?.1.8) is performed. Then:<br>
 * If either operand is of type double, the other is converted to double.<br>
 * Otherwise, if either operand is of type float, the other is converted to float.<br>
 * Otherwise, if either operand is of type long, the other is converted to long.<br>
 * Otherwise, both operands are converted to type int.
 *
 * <p>
 *
 * @author Andy Clement
 * @since 3.0
 */
public class OpMultiply extends Operator {

	public OpMultiply(int pos, SpelNodeImpl... operands) {
		super("*", pos, operands);
	}

	/**
	 * Implements multiply directly here for some types of operand, otherwise delegates to any registered overloader for
	 * types it does not recognize. Supported types here are:
	 * <ul>
	 * <li>integers
	 * <li>doubles
	 * <li>string and int ('abc' * 2 == 'abcabc')
	 * </ul>
	 */
	@Override
	public TypedValue getValueInternal(ExpressionState state) 
			throws EvaluationException {
		Object operandOne = getLeftOperand().getValueInternal(state).getValue();
		Object operandTwo = getRightOperand().getValueInternal(state).getValue();
		if (operandOne instanceof Number && operandTwo instanceof Number) {
			Number leftNumber = (Number) operandOne;
			Number rightNumber = (Number) operandTwo;
			if (leftNumber instanceof Double || rightNumber instanceof Double) {
				return new TypedValue(leftNumber.doubleValue() * rightNumber.doubleValue());
			} else if (leftNumber instanceof Long || rightNumber instanceof Long) {
				return new TypedValue(leftNumber.longValue() * rightNumber.longValue());
			} else {
				return new TypedValue(leftNumber.intValue() * rightNumber.intValue());
			}
		} else if (operandOne instanceof String && operandTwo instanceof Integer) {
			int repeats = (Integer) operandTwo;
			StringBuilder result = new StringBuilder();
			for (int i = 0; i < repeats; i++) {
				result.append(operandOne);
			}
			return new TypedValue(result.toString());
		}
		return state.operate(Operation.MULTIPLY, operandOne, operandTwo);
	}

}
