package com.tutorial.beans.factory.xml;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.tutorial.core.io.Resource;
import com.tutorial.core.io.ResourceLoader;

/**
 * EntityResolver implementation that tries to resolve entity references
 * through a {@link com.tutorial.core.io.ResourceLoader} (usually,
 * relative to the resource base of an ApplicationContext), if applicable.
 * Extends {@link DelegatingEntityResolver} to also provide DTD and XSD lookup.
 *
 * <p>Allows to use standard XML entities to include XML snippets into an
 * application context definition, for example to split a large XML file
 * into various modules. The include paths can be relative to the
 * application context's resource base as usual, instead of relative
 * to the JVM working directory (the XML parser's default).
 *
 * <p>Note: In addition to relative paths, every URL that specifies a
 * file in the current system root, i.e. the JVM working directory,
 * will be interpreted relative to the application context too.
 *
 * @author Juergen Hoeller
 * @since 31.07.2003
 * @see com.tutorial.core.io.ResourceLoader
 * @see com.tutorial.context.ApplicationContext
 */
public class ResourceEntityResolver extends DelegatingEntityResolver {
	
	private static final Log logger = LogFactory.getLog(ResourceEntityResolver.class);
	
	private final ResourceLoader resourceLoader;

	/**
	 * Create a ResourceEntityResolver for the specified ResourceLoader
	 * (usually, an ApplicationContext).
	 * @param resourceLoader the ResourceLoader (or ApplicationContext)
	 * to load XML entity includes with
	 */
	public ResourceEntityResolver(ResourceLoader resourceLoader) {
		super(resourceLoader.getClassLoader());
		this.resourceLoader = resourceLoader;
	}

	@SuppressWarnings("deprecation")
	@Override
	public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
		InputSource source = super.resolveEntity(publicId, systemId);
		if(source == null && systemId != null) {
			String resourcePath = null;
			try {
				String decodedSystemId = URLDecoder.decode(systemId);
				String givenUrl = new URL(decodedSystemId).toString();
				String systemRootUrl = new File("").toURL().toString();
				// Try relative to resource base if currently in system root.
				if(givenUrl.startsWith(systemRootUrl)) {
					resourcePath = givenUrl.substring(systemRootUrl.length());
				}
			} catch (Exception e) {
				// Typically a MalformedURLException or AccessControlException.
				if(logger.isDebugEnabled()) {
					logger.debug("Could not resolve XML entity [" + systemId + "] against system root URL", e);
				}
				// No URL (or no resolvable URL) -> try relative to resource base.
				resourcePath = systemId;
			}
			if(resourcePath != null) {
				if(logger.isTraceEnabled()) {
					logger.trace("Trying to locate XML entity [" + systemId + "] as resource [" + 
									resourcePath + "]");
				}
				Resource resource = this.resourceLoader.getResource(resourcePath);
				source = new InputSource(resource.getInputStream());
				source.setPublicId(publicId);
				source.setSystemId(systemId);
				if(logger.isDebugEnabled()) {
					logger.debug("Found XML entity [" + systemId + "]: " + resource);
				}
			}
		}
		return source;
	}
}
