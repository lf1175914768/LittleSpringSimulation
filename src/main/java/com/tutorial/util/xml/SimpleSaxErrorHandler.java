package com.tutorial.util.xml;

import org.apache.commons.logging.Log;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Simple <code>org.xml.sax.ErrorHandler</code> implementation:
 * logs warnings using the given Commons Logging logger instance,
 * and rethrows errors to discontinue the XML transformation.
 *
 * @author Juergen Hoeller
 * @since 1.2
 */
public class SimpleSaxErrorHandler implements ErrorHandler {
	
	private final Log logger;
	
	/**
	 * Create a new SimpleSaxErrorHandler for the given
	 * Commons Logging logger instance.
	 */
	public SimpleSaxErrorHandler(Log logger) {
		this.logger = logger;
	}

	public void warning(SAXParseException exception) throws SAXException {
		logger.warn("Ignored XML validation warning", exception);
	}

	public void error(SAXParseException exception) throws SAXException {
		throw exception;
	}

	public void fatalError(SAXParseException exception) throws SAXException {
		throw exception;
	}

}
