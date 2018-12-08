package com.tutorial.expression.spel;

import com.tutorial.expression.EvaluationException;
import com.tutorial.expression.TypedValue;

/**
 * Represents a node in the Ast for a parsed expression.
 *
 * @author Andy Clement
 * @since 3.0
 */
public interface SpelNode {
	
	/**
	 * Evaluate the expression node in the context of the supplied expression state and return the value.
	 * @param expressionState the current expression state (includes the context)
	 * @return the value of this node evaluated against the specified state
	 */
	Object getValue(ExpressionState expressionState) throws EvaluationException;
	
	/**
	 * Evaluate the expression node in the context of the supplied expression state and return the typed value.
	 * @param expressionState the current expression state (includes the context)
	 * @return the type value of this node evaluated against the specified state
	 */
	TypedValue getTypedValue(ExpressionState expressionState) throws EvaluationException;
	
	/**
	 * Determine if this expression node will support a setValue() call.
	 * 
	 * @param expressionState the current expression state (includes the context)
	 * @return true if the expression node will allow setValue()
	 * @throws EvaluationException if something went wrong trying to determine if the node supports writing
	 */
	boolean isWritable(ExpressionState expressionState) throws EvaluationException;
	
	/**
	 * Evaluate the expression to a node and then set the new value on that node. For example, if the expression
	 * evaluates to a property reference then the property will be set to the new value.
	 * @param expressionState the current expression state (includes the context)
	 * @param newValue the new value
	 * @throws EvaluationException if any problem occurs evaluating the expression or setting the new value
	 */
	void setValue(ExpressionState expressionState, Object newValue) throws EvaluationException;
	
	/**
	 * @return the string form of this AST node
	 */
	String toStringAST();
	
	/**
	 * @return the number of children under this node
	 */
	int getChildCount();
	
	/**
	 * Helper method that returns a SpelNode rather than an Antlr Tree node.
	 * @return the child node cast to a SpelNode
	 */
	SpelNode getChild(int index);
	
	/**
	 * Determine the class of the object passed in, unless it is already a class object.
	 * @param o the object that the caller wants the class of
	 * @return the class of the object if it is not already a class object, or null if the object is null
	 */
	Class<?> getObjectClass(Object obj);
	
	/**
	 * @return the start position of this Ast node in the expression string
	 */
	int getStartPosition();
	
	/**
	 * @return the end position of this Ast node in the expression string
	 */
	int getEndPosition();

}
