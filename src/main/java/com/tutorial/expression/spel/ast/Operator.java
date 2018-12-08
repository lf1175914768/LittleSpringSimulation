package com.tutorial.expression.spel.ast;

/**
 * Common supertype for operators that operate on either one or two operands. In the case of multiply or divide there
 * would be two operands, but for unary plus or minus, there is only one.
 *
 * @author Andy Clement
 * @since 3.0
 */
public abstract class Operator extends SpelNodeImpl {
	
	String operatorName;
	
	public Operator(String payLoad, int pos, SpelNodeImpl... operands) {
		super(pos, operands);
		this.operatorName = payLoad;
	}
	
	public SpelNodeImpl getLeftOperand() {
		return children[0];
	}
	
	public SpelNodeImpl getRightOperand() {
		return children[1];
	}
	
	public final String getOperatorName() {
		return operatorName;
	}

	/**
	 * String format for all operators is the same '(' [operand] [operator] [operand] ')'
	 */
	@Override
	public String toStringAST() {
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		sb.append(getChild(0).toStringAST());
		for(int i = 1; i < getChildCount(); i++) {
			sb.append(" ").append(getOperatorName()).append(" ");
			sb.append(getChild(i).toStringAST());
		}
		sb.append(")");
		return sb.toString();
	}

}
