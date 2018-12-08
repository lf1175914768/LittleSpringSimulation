package com.tutorial.expression.spel.ast;

import com.tutorial.expression.EvaluationException;
import com.tutorial.expression.TypedValue;
import com.tutorial.expression.spel.ExpressionState;
import com.tutorial.expression.spel.SpelEvaluationException;

/**
 * Represents a DOT separated expression sequence, such as 'property1.property2.methodOne()'
 * 
 * @author Andy Clement
 * @since 3.0
 */
public class CompoundExpression extends SpelNodeImpl {
	
	public CompoundExpression(int pos, SpelNodeImpl... expressionComponents) {
		super(pos, expressionComponents);
		if(expressionComponents.length < 2) {
			throw new IllegalStateException("Dont build compound expression less than one entry: "
						+ expressionComponents.length);
		}
	}

	/**
	 * Evalutes a compound expression. This involves evaluating each piece in turn and the return value from each piece
	 * is the active context object for the subsequent piece.
	 * @param state the state in which the expression is being evaluated
	 * @return the final value from the last piece of the compound expression
	 */
	@Override
	public TypedValue getValueInternal(ExpressionState expressionState) 
			throws EvaluationException {
		TypedValue result = null;
		SpelNodeImpl nextNode = null;
		try {
			nextNode = children[0];
			result = nextNode.getValueInternal(expressionState);
			for(int i = 1; i < getChildCount(); i++) {
				try {
					expressionState.pushActiveContextObject(result);
					nextNode = children[i];
					result = nextNode.getValueInternal(expressionState);
				} finally {
					expressionState.popActiveContextObject();
				}
			}
		} catch (SpelEvaluationException ee) {
			ee.setPosition(nextNode.getStartPosition());
			throw ee;
		}
		return result;
	}

	@Override
	public void setValue(ExpressionState state, Object value) throws EvaluationException {
		if (getChildCount() == 1) {
			getChild(0).setValue(state, value);
			return;
		}
		TypedValue ctx = children[0].getValueInternal(state);
		for (int i = 1; i < getChildCount() - 1; i++) {
			try {
				state.pushActiveContextObject(ctx);
				ctx = children[i].getValueInternal(state);
			} finally {
				state.popActiveContextObject();
			}
		}
		try {
			state.pushActiveContextObject(ctx);
			getChild(getChildCount() - 1).setValue(state, value);
		} finally {
			state.popActiveContextObject();
		}
	}

	@Override
	public boolean isWritable(ExpressionState state) throws EvaluationException {
		if (getChildCount() == 1) {
			return getChild(0).isWritable(state);
		}
		TypedValue ctx = children[0].getValueInternal(state);
		for (int i = 1; i < getChildCount() - 1; i++) {
			try {
				state.pushActiveContextObject(ctx);
				ctx = children[i].getValueInternal(state);
			} finally {
				state.popActiveContextObject();
			}
		}
		try {
			state.pushActiveContextObject(ctx);
			return getChild(getChildCount() - 1).isWritable(state);
		} finally {
			state.popActiveContextObject();
		}
	}

	@Override
	public String toStringAST() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < getChildCount(); i++) {
			if (i>0) { sb.append("."); }
			sb.append(getChild(i).toStringAST());
		}
		return sb.toString();
	}

}
