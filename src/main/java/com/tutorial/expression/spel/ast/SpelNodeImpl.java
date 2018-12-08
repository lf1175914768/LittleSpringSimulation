package com.tutorial.expression.spel.ast;

import com.tutorial.expression.EvaluationException;
import com.tutorial.expression.TypedValue;
import com.tutorial.expression.common.ExpressionUtils;
import com.tutorial.expression.spel.ExpressionState;
import com.tutorial.expression.spel.SpelEvaluationException;
import com.tutorial.expression.spel.SpelMessage;
import com.tutorial.expression.spel.SpelNode;
import com.tutorial.expression.spel.support.StandardEvaluationContext;
import com.tutorial.util.Assert;

/**
 * The common supertype of all AST nodes in a parsed Spring Expression Language format expression.
 *
 * @author Andy Clement
 * @since 3.0
 */
public abstract class SpelNodeImpl implements SpelNode {
	
	private static SpelNodeImpl[] NO_CHILDREN = new SpelNodeImpl[0];
	
	protected int pos;     // start = top 16bits, end = bottom 16bits
	
	protected SpelNodeImpl[] children = SpelNodeImpl.NO_CHILDREN;
	
	private SpelNodeImpl parent;
	
	public SpelNodeImpl(int pos, SpelNodeImpl... operands) {
		this.pos = pos;
		// Pos combines start and end so can never be zero because tokens cann't be zero length
		Assert.isTrue(pos != 0);
		if(operands != null && operands.length > 0) {
			this.children = operands;
			for(SpelNodeImpl childNode : operands) {
				childNode.parent = this;
			}
		}
	}
	
	protected SpelNodeImpl getPreviousChild() {
		SpelNodeImpl result = null;
		if(parent != null) {
			for(SpelNodeImpl child : parent.children) {
				if(this == child) break;
				result = child;
			}
		}
		return result;
	}

	/**
     * @return true if the next child is one of the specified classes
     */
	protected boolean nextChildIs(Class<?>... clazzes) {
		if(parent != null) {
			SpelNodeImpl[] peers = parent.children;
			for(int i = 0, max = peers.length; i < max; i++) {
				if(peers[i] == this) {
					if((i + 1) >= max) {
						return false;
					} else {
						Class<?> clazz = peers[i + 1].getClass();
						for(Class<?> desiredClass : clazzes) {
							if(clazz.equals(desiredClass)) {
								return true;
							}
						}
						return false;
					}
				}
			}
		}
		return false;
	}

	public final Object getValue(ExpressionState expressionState) throws EvaluationException {
		if(expressionState != null) {
			return getValueInternal(expressionState).getValue();
		} else {
			// Configuration not set - does that matter?
			return getValue(new ExpressionState(new StandardEvaluationContext()));
		}
	}

	public final TypedValue getTypedValue(ExpressionState expressionState) throws EvaluationException {
		if(expressionState != null) {
			return getValueInternal(expressionState);
		} else {
			// configuration not set - does that matter? 
			return getTypedValue(new ExpressionState(new StandardEvaluationContext()));	
		}
	}

	public boolean isWritable(ExpressionState expressionState) throws EvaluationException {
		return false;
	}

	public void setValue(ExpressionState expressionState, Object newValue) throws EvaluationException {
		throw new SpelEvaluationException(getStartPosition(), SpelMessage.SETVALUE_NOT_SUPPORTED, getClass());
	}

	public int getChildCount() {
		return this.children.length;
	}

	public SpelNode getChild(int index) {
		return children[index];
	}

	public Class<?> getObjectClass(Object obj) {
		if(obj == null) {
			return null;
		}
		return (obj instanceof Class ? (Class<?>) obj : obj.getClass());
	}
	
	@SuppressWarnings("unchecked")
	protected final <T> T getValue(ExpressionState state, Class<T> desiredReturnType) throws EvaluationException {
		Object result = getValueInternal(state).getValue();
		if(result != null && desiredReturnType != null) {
			Class<?> resultType = result.getClass();
			if(desiredReturnType.isAssignableFrom(resultType)) {
				return (T) result;
			}
			// Attempt conversion to the requested type, may throw an exception
			return ExpressionUtils.convert(state.getEvaluationContext(), result, desiredReturnType);
		}
		return (T) result;
	}

	public int getStartPosition() {
		return (pos >> 16);
	}

	public int getEndPosition() {
		return (pos & 0xffff);
	}
	
	public abstract TypedValue getValueInternal(ExpressionState expressionState) throws EvaluationException;

	public abstract String toStringAST();

}
