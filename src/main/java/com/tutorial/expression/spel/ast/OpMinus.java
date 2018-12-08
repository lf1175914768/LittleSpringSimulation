package com.tutorial.expression.spel.ast;

import com.tutorial.expression.EvaluationException;
import com.tutorial.expression.Operation;
import com.tutorial.expression.TypedValue;
import com.tutorial.expression.spel.ExpressionState;

/**
 * The minus operator supports:
 * <ul>
 * <li>subtraction of doubles (floats are represented as doubles)
 * <li>subtraction of longs
 * <li>subtraction of integers
 * <li>subtraction of an int from a string of one character (effectively decreasing that character), so 'd'-3='a'
 * </ul>
 * It can be used as a unary operator for numbers (double/long/int).  The standard promotions are performed
 * when the operand types vary (double-int=double).
 * For other options it defers to the registered overloader.
 *
 * @author Andy Clement
 * @since 3.0
 */
public class OpMinus extends Operator {

	public OpMinus(int pos, SpelNodeImpl... operands) {
		super("-", pos, operands);
	}

	@Override
	public TypedValue getValueInternal(ExpressionState state) throws EvaluationException {
		SpelNodeImpl leftOp = getLeftOperand();
		SpelNodeImpl rightOp = getRightOperand();
		if (rightOp == null) {// If only one operand, then this is unary minus
			Object operand = leftOp.getValueInternal(state).getValue();
			if (operand instanceof Number) {
				Number n = (Number) operand;
				if (operand instanceof Double) {
					return new TypedValue(0 - n.doubleValue());
				} else if (operand instanceof Long) {
					return new TypedValue(0 - n.longValue());
				} else {
					return new TypedValue(0 - n.intValue());
				}
			}
			return state.operate(Operation.SUBTRACT, operand, null);
		} else {
			Object left = leftOp.getValueInternal(state).getValue();
			Object right = rightOp.getValueInternal(state).getValue();
			if (left instanceof Number && right instanceof Number) {
				Number op1 = (Number) left;
				Number op2 = (Number) right;
				if (op1 instanceof Double || op2 instanceof Double) {
					return new TypedValue(op1.doubleValue() - op2.doubleValue());
				} else if (op1 instanceof Long || op2 instanceof Long) {
					return new TypedValue(op1.longValue() - op2.longValue());
				} else {
					return new TypedValue(op1.intValue() - op2.intValue());
				}
			} else if (left instanceof String && right instanceof Integer && ((String)left).length()==1) {
				String theString = (String) left;
				Integer theInteger = (Integer) right;
				// implements character - int (ie. b - 1 = a)
				return new TypedValue(Character.toString((char) (theString.charAt(0) - theInteger)));
			}
			return state.operate(Operation.SUBTRACT, left, right);
		}
	}

	@Override
	public String toStringAST() {
		if (getRightOperand() == null) { // unary minus
			return new StringBuilder().append("-").append(getLeftOperand().toStringAST()).toString();
		}
		return super.toStringAST();
	}
	public SpelNodeImpl getRightOperand() {
		if (children.length<2) {return null;}
		return children[1];
	}

}
