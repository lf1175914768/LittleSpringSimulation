package com.tutorial.expression.spel;

import com.tutorial.expression.ParseException;

/**
 * Root exception for Spring EL related exceptions. Rather than holding a hard coded string indicating the problem, it
 * records a message key and the inserts for the message. See {@link SpelMessage} for the list of all possible messages
 * that can occur.
 * 
 * @author Andy Clement
 * @since 3.0
 */
@SuppressWarnings("serial")
public class SpelParseException extends ParseException {
	
	private SpelMessage message;
	private Object[] inserts;
	
	public SpelParseException(String expressionString, int position, SpelMessage message, Object... inserts) {
		super(expressionString, position, message.formatMessage(position, inserts));
		this.position = position;
		this.message = message;
		this.inserts = inserts;
	}
	
	public SpelParseException(int position, SpelMessage message, Object... inserts) {
		super(position, message.formatMessage(position,inserts));
		this.position = position;
		this.message = message;
		this.inserts = inserts;
	}

	public SpelParseException(int position, Throwable cause, SpelMessage message, Object... inserts) {
		super(position, message.formatMessage(position,inserts), cause);
		this.position = position;
		this.message = message;
		this.inserts = inserts;
	}

	/**
	 * @return a formatted message with inserts applied
	 */
	@Override
	public String getMessage() {
		if (message != null)
			return message.formatMessage(position, inserts);
		else
			return super.getMessage();
	}

	/**
	 * @return the message code
	 */
	public SpelMessage getMessageCode() {
		return this.message;
	}

	/**
	 * @return the message inserts
	 */
	public Object[] getInserts() {
		return inserts;
	}


}
