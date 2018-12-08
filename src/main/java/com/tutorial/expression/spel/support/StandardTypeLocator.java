package com.tutorial.expression.spel.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.tutorial.expression.EvaluationException;
import com.tutorial.expression.TypeLocator;
import com.tutorial.expression.spel.SpelEvaluationException;
import com.tutorial.expression.spel.SpelMessage;
import com.tutorial.util.ClassUtils;

/**
 * A default implementation of a TypeLocator that uses the context classloader (or any classloader set upon it). It
 * supports 'well known' packages so if a type cannot be found it will try the registered imports to locate it.
 * 
 * @author Andy Clement
 * @author Juergen Hoeller
 * @since 3.0
 */
public class StandardTypeLocator implements TypeLocator {
	
	private ClassLoader loader;
	
	private final List<String> knownPackagePrefixes = new ArrayList<String>();
	
	public StandardTypeLocator() {
		this(ClassUtils.getDefaultClassLoader());
	}
	
	public StandardTypeLocator(ClassLoader loader) {
		this.loader = loader;	
		// Similar to when writing JAVA, it only knows about java.lang by default.
		registerImport("java.lang");
	}

	/**
	 * Register a new import prefix that will be used when searching for unqualified types.
	 * Expected format is something like "java.lang".
	 * @param prefix the prefix to register
	 */
	public void registerImport(String prefix) {
		this.knownPackagePrefixes.add(prefix);
	}
	
	/**
	 * Return a list of all the import prefixes registered with this StandardTypeLocator.
	 * @return list of registered import prefixes
	 */
	public List<String> getImportPrefixes() {
		return Collections.unmodifiableList(this.knownPackagePrefixes);
	}
	
	public void removeImport(String prefix) {
		this.knownPackagePrefixes.remove(prefix);
	}

	/**
	 * Find a (possibly unqualified) type reference - first using the typename as is, then trying any registered
	 * prefixes if the typename cannot be found.
	 * @param typename the type to locate
	 * @return the class object for the type
	 * @throws EvaluationException if the type cannot be found
	 */
	public Class<?> findType(String typeName) throws EvaluationException {
		String nameToLookup = typeName;
		try {
			return this.loader.loadClass(nameToLookup);
		} catch (ClassNotFoundException e) {
			// try any registered prefixes before giving up.
		}
		for(String prefix : this.knownPackagePrefixes) {
			try {
				nameToLookup = new StringBuilder().append(prefix).append(".").append(typeName).toString();
				return this.loader.loadClass(nameToLookup);
			} catch (ClassNotFoundException e) {
				// might be a different 
			}
		}
		throw new SpelEvaluationException(SpelMessage.TYPE_NOT_FOUND, typeName);
	}

}
