package com.tutorial.expression.spel.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.tutorial.expression.EvaluationException;
import com.tutorial.expression.TypedValue;
import com.tutorial.expression.spel.ExpressionState;
import com.tutorial.expression.spel.SpelNode;

/**
 * @author Liufeng Created on 2018年11月17日 上午10:57:22
 */
public class InlineList extends SpelNodeImpl {

	// if the list is purely literals, it is a constant value and can be
	// computed and cached
	TypedValue constant = null; // TODO must be immutable list

	public InlineList(int pos, SpelNodeImpl... args) {
		super(pos, args);
		checkIfConstant();
	}

	/**
	 * If all the components of the list are constants, or lists that themselves
	 * contain constants, then a constant list can be built to represent this
	 * node. This will speed up later getValue calls and reduce the amount of
	 * garbage created.
	 */
	private void checkIfConstant() {
		boolean isConstant = true;
		for (int c = 0, max = getChildCount(); c < max; c++) {
			SpelNode child = getChild(c);
			if (!(child instanceof Literal)) {
				if (child instanceof InlineList) {
					InlineList inlineList = (InlineList) child;
					if (!inlineList.isConstant()) {
						isConstant = false;
					}
				} else {
					isConstant = false;
				}
			}
		}
		if (isConstant) {
			List<Object> constantList = new ArrayList<Object>();
			int childcount = getChildCount();
			for (int c = 0; c < childcount; c++) {
				SpelNode child = getChild(c);
				if ((child instanceof Literal)) {
					constantList.add(((Literal) child).getLiteralValue().getValue());
				} else if (child instanceof InlineList) {
					constantList.add(((InlineList) child).getConstantValue());
				}
			}
			this.constant = new TypedValue(Collections.unmodifiableList(constantList));
		}
	}

	@Override
	public TypedValue getValueInternal(ExpressionState expressionState) throws EvaluationException {
		if (constant != null) {
			return constant;
		} else {
			List<Object> returnValue = new ArrayList<Object>();
			int childcount = getChildCount();
			for (int c = 0; c < childcount; c++) {
				returnValue.add(getChild(c).getValue(expressionState));
			}
			return new TypedValue(returnValue);
		}
	}

	@Override
	public String toStringAST() {
		StringBuilder s = new StringBuilder();
		// string ast matches input string, not the 'toString()' of the
		// resultant collection, which would use []
		s.append('{');
		int count = getChildCount();
		for (int c = 0; c < count; c++) {
			if (c > 0) {
				s.append(',');
			}
			s.append(getChild(c).toStringAST());
		}
		s.append('}');
		return s.toString();
	}

	/**
	 * @return whether this list is a constant value
	 */
	public boolean isConstant() {
		return constant != null;
	}

	@SuppressWarnings("unchecked")
	private List<Object> getConstantValue() {
		return (List<Object>) constant.getValue();
	}

}
