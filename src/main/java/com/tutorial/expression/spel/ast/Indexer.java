package com.tutorial.expression.spel.ast;

import java.util.Arrays;
import java.util.Map;

import com.tutorial.core.convert.TypeDescriptor;
import com.tutorial.expression.EvaluationException;
import com.tutorial.expression.TypedValue;
import com.tutorial.expression.spel.ExpressionState;

/**
 * An Indexer can index into some proceeding structure to access a particular piece of it.
 * Supported structures are: strings/collections (lists/sets)/arrays
 *
 * @author Andy Clement
 * @since 3.0
 */
public class Indexer extends SpelNodeImpl {
	
	public Indexer(int pos, SpelNodeImpl expr) {
		super(pos, expr);
	}

	@Override
	public TypedValue getValueInternal(ExpressionState state)
			throws EvaluationException {
		TypedValue context = state.getActiveContextObject();
		Object targetObject = context.getValue();
		TypeDescriptor targetObjectTypeDescriptor = context.getTypeDescriptor();
		TypedValue indexValue = null;
		Object index = null;
		
		// This first part of the if clause prevents a 'double dereference' of the property (SPR-5847)
		if(targetObject instanceof Map && (children[0] instanceof PropertyOrFieldReference)) {
			PropertyOrFieldReference reference = (PropertyOrFieldReference) children[0];
			index = reference.getName();
			indexValue = new TypedValue(index);
		}
		return null;
	}

	@Override
	public String toStringAST() {  
		return null;
	}

}
