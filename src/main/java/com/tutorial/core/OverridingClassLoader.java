package com.tutorial.core;

import java.io.InputStream;

import com.tutorial.util.FileCopyUtils;

/**
 * <code>ClassLoader</code> that does <i>not</i> always delegate to the
 * parent loader, as normal class loaders do. This enables, for example,
 * instrumentation to be forced in the overriding ClassLoader, or a
 * "throwaway" class loading behavior, where selected classes are
 * temporarily loaded in the overriding ClassLoader, in order to load
 * an instrumented version of the class in the parent ClassLoader later on.
 * 
 * @author Liufeng
 * Created on 2018年11月25日 上午10:22:26
 */
public class OverridingClassLoader extends DecoratingClassLoader {
	
	public static final String[] DEFAULT_EXCLUDED_PACKAGES = 
			new String[] {"java.", "javax.", "sun.", "oracle."};
	
	private static final String CLASS_FILE_SUFFIX = ".class";
	
	/**
	 * Create a new OverridingClassLoader for the given class loader.
	 * @param parent the ClassLoader to build an overriding ClassLoader for
	 */
	public OverridingClassLoader(ClassLoader parent) {
		super(parent);
		for(String packageName : DEFAULT_EXCLUDED_PACKAGES) {
			excludePackage(packageName);
		}
	}
	
	@Override
	protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		Class<?> result = null;
		if(isEligibleForOverriding(name)) {
			result = loadClassForOverriding(name);
		}
		if(result != null) {
			if(resolve) {
				resolveClass(result);
			} 
			return result;
		} else {
			return super.loadClass(name, resolve);
		}
	}

	/**
	 * Determine whether the specified class is eligible for overriding
	 * by this class loader.
	 * @param className the class name to check
	 * @return whether the specified class is eligible
	 * @see #isExcluded
	 */
	protected boolean isEligibleForOverriding(String name) {
		return !isExcluded(name);
	}

	/**
	 * Load the specified class for overriding purposes in this ClassLoader.
	 * <p>The default implementation delegates to {@link #findLoadedClass},
	 * {@link #loadBytesForClass} and {@link #defineClass}.
	 * @param name the name of the class
	 * @return the Class object, or <code>null</code> if no class defined for that name
	 * @throws ClassNotFoundException if the class for the given name couldn't be loaded
	 */
	protected Class<?> loadClassForOverriding(String name) throws ClassNotFoundException {
		Class<?> result = findLoadedClass(name);
		if(result == null) {
			byte[] bytes = loadBytesForClass(name);
			if(bytes != null) {
				result = defineClass(name, bytes, 0, bytes.length);
			}
		}
		return result;
	}

	protected byte[] loadBytesForClass(String name) throws ClassNotFoundException {
		InputStream is = openStreamForClass(name);
		if(is == null) {
			return null;
		} 
		try {
			// load the raw bytes.
			byte[] bytes = FileCopyUtils.copyToByteArray(is);
			// transform if necessary and use the potentially transformed bytes.
			return transformIfNecessary(name, bytes);
		} catch (Exception ex) {
			throw new ClassNotFoundException("Cannot load resource for class [" + name + "]", ex);
		}
	}
	
	/**
	 * Open an InputStream for the specified class.
	 * <p>The default implementation loads a standard class file through
	 * the parent ClassLoader's <code>getResourceAsStream</code> method.
	 * @param name the name of the class
	 * @return the InputStream containing the byte code for the specified class
	 */
	protected InputStream openStreamForClass(String name) {
		String internalName = name.replace('.', '/') + CLASS_FILE_SUFFIX; 
		return getParent().getResourceAsStream(internalName);
	}

	/**
	 * Transformation hook to be implemented by subclasses.
	 * <p>The default implementation simply returns the given bytes as-is.
	 * @param name the fully-qualified name of the class being transformed
	 * @param bytes the raw bytes of the class
	 * @return the transformed bytes (never <code>null</code>;
	 * same as the input bytes if the transformation produced no changes)
	 */
	protected byte[] transformIfNecessary(String name, byte[] bytes) {
		return bytes;
	}
}
