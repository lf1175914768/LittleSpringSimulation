package com.tutorial.core.env;

import java.util.Map;
import java.util.Properties;

/**
 * {@link PropertySource} implementation that extracts properties from a
 * {@link java.util.Properties} object.
 *
 * <p>Note that because a {@code Properties} object is technically an
 * {@code <Object, Object>} {@link java.util.Hashtable Hashtable}, one may contain
 * non-{@code String} keys or values. This implementation, however is restricted to
 * accessing only {@code String}-based keys and values, in the same fashion as
 * {@link Properties#getProperty} and {@link Properties#setProperty}.
 *
 * @author Chris Beams
 * @since 3.1
 * @see com.tutorial.mock.env.MockPropertySource
 */
public class PropertiesPropertySource extends MapPropertySource {
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public PropertiesPropertySource(String name, Properties source) {
		super(name, (Map) source);
	}
	
}
