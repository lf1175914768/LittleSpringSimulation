package com.tutorial.context.support;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.tutorial.util.Assert;

/**
 * Simple implementation of {@link com.tutorial.context.MessageSource}
 * which allows messages to be registered programmatically.
 * This MessageSource supports basic internationalization.
 *
 * <p>Intended for testing rather than for use in production systems.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
public class StaticMessageSource extends AbstractMessageSource {
	
	/** Map from 'code + locale' keys to message Strings */
	private final Map<String, String> messages = new HashMap<String, String>();
	
	private final Map<String, MessageFormat> cachedMessageFormats = 
			new HashMap<String, MessageFormat>();
	
	@Override
	protected String resolveCodeWithoutArguments(String code, Locale locale) {
		return this.messages.get(code + "_" + locale.toString());
	}

	@Override
	protected MessageFormat resolveCode(String code, Locale locale) {
		String key = code + "_" + locale.toString();
		String msg = this.messages.get(key);
		if(msg == null) {
			return null;
		}
		synchronized(this.cachedMessageFormats) {
			MessageFormat messageFormat = this.cachedMessageFormats.get(key);
			if(messageFormat == null) {
				messageFormat = createMessageFormat(msg, locale);
				this.cachedMessageFormats.put(key, messageFormat);
			}
			return messageFormat;
		}
	}
	
	/**
	 * Associate the given message with the given code.
	 * @param code the lookup code
	 * @param locale the locale that the message should be found within
	 * @param msg the message associated with this lookup code
	 */
	public void addMessage(String code, Locale locale, String msg) {
		Assert.notNull(code, "Code must not be null");
		Assert.notNull(locale, "Locale must not be null");
		Assert.notNull(msg, "Message must not be null");
		this.messages.put(code + "_" + locale.toString(), msg);
		if(logger.isDebugEnabled()) {
			logger.debug("Added message [" + msg + "] for code [" + code + "] and Locale [" + locale + "]");
		}
	}

	/**
	 * Associate the given message values with the given keys as codes.
	 * @param messages the messages to register, with messages codes
	 * as keys and message texts as values
	 * @param locale the locale that the messages should be found within
	 */
	public void addMessage(Map<String, String> messages, Locale locale) {
		Assert.notNull(messages, "Message Map must not be null");
		for(Map.Entry<String, String> entry : messages.entrySet()) {
			addMessage(entry.getKey(), locale, entry.getValue());
		}
	}
	
	@Override
	public String toString() {
		return getClass().getName() + ": " + this.messages;
	}
}
