package com.tutorial.expression;

/**
 * Super class for exceptions that can occur whilst processing expressions
 *
 * @author Andy Clement
 * @since 3.0
 */
@SuppressWarnings("serial")
public class ExpressionException extends RuntimeException {
	
	protected String expressionString;
	
	protected int position;   // -1 if not known - but should be known in all reasonable cases
	
	/**
	 * Creates a new expression exception.
	 * @param expressionString the expression string
	 * @param message a descriptive message
	 */
	public ExpressionException(String expressionString, String message) {
		super(message);
		this.position = -1;
		this.expressionString = expressionString;
	}
	
	/**
	 * Creates a new expression exception.
	 * @param expressionString the expression string
	 * @param position the position in the expression string where the problem occurred
	 * @param message a descriptive message
	 */
	public ExpressionException(String expressionString, int position, String message) {
		super(message);
		this.position = position;
		this.expressionString = expressionString;
	}
	
	/**
	 * Creates a new expression exception.
	 * @param position the position in the expression string where the problem occurred
	 * @param message a descriptive message
	 */
	public ExpressionException(int position, String message) {
		super(message);
		this.position = position;
	}
	
	/**
	 * Creates a new expression exception.
	 * @param position the position in the expression string where the problem occurred
	 * @param message a descriptive message
	 * @param cause the underlying cause of this exception
	 */ 
	public ExpressionException(int position, String message, Throwable cause) {
		super(message, cause);
		this.position = position;
	}

	/**
	 * Creates a new expression exception.
	 * @param message a descriptive message
	 */ 
	public ExpressionException(String message) {
		super(message);
	}

	public ExpressionException(String message, Throwable cause) {
		super(message,cause);
	}
	
	public String toDetailedString() {
		StringBuilder output = new StringBuilder();
		if(expressionString != null) {
			output.append("Expression '");
			output.append(expressionString);
			output.append("'");
			if(position != -1) {
				output.append(" @ ");
				output.append(position);
			}
			output.append(": ");
		}
		output.append(getMessage());
		return output.toString();
	}

	public final String getExpressionString() {
		return this.expressionString;
	}
	
	public final int getPosition() {
		return position;
	}

}
