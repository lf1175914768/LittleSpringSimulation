package com.tutorial.context.support;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import com.tutorial.core.DecoratingClassLoader;
import com.tutorial.core.OverridingClassLoader;
import com.tutorial.core.SmartClassLoader;
import com.tutorial.util.ReflectionUtils;

/**
 * Special variant of an overriding ClassLoader, used for temporary type
 * matching in {@link AbstractApplicationContext}. Redefines classes from
 * a cached byte array for every <code>loadClass</code> call in order to
 * pick up recently loaded types in the parent ClassLoader.
 * 
 * @author Liufeng
 * Created on 2018年11月25日 上午12:17:09
 */
public class ContextTypeMatchClassLoader extends DecoratingClassLoader implements SmartClassLoader {
	
	private static Method findLoaderClassMethod;
	
	static {
		try {
			findLoaderClassMethod = ClassLoader.class.getDeclaredMethod("findLoadedClass", new Class[] {String.class});
		} catch (NoSuchMethodException e) {
			throw new IllegalStateException("Invalid [java.lang.ClassLoader] class: no 'findLoadedClass' method defined!");
		}
	}
	
	/** Cache for byte array per class name */
	private final Map<String, byte[]> bytesCache = new HashMap<String, byte[]>();
	
	public ContextTypeMatchClassLoader(ClassLoader parent) {
		super(parent);
	}
	
	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		return new ContextOverridingClassLoader(getParent()).loadClass(name);
	}
	
	@Override
	public boolean isClassReloadable(Class clazz) {
		return clazz.getClassLoader() instanceof ContextOverridingClassLoader;
	}
	
	/**
	 * ClassLoader to be created for each loaded class.
	 * Caches class file content but redefines class for each call.
	 */
	private class ContextOverridingClassLoader extends OverridingClassLoader {
		public ContextOverridingClassLoader(ClassLoader parent) {
			super(parent);
		}
		
		@Override
		protected boolean isEligibleForOverriding(String name) {
			if(isExcluded(name) || ContextTypeMatchClassLoader.this.isExcluded(name)) {
				return false;
			}
			ReflectionUtils.makeAccessible(findLoaderClassMethod);
			ClassLoader parent = getParent();
			while(parent != null) {
				if(ReflectionUtils.invokeMethod(findLoaderClassMethod, parent, name) != null) {
					return false;
				}
				parent = parent.getParent();
			}
			return true;
		}
		
		@Override
		protected Class<?> loadClassForOverriding(String name) throws ClassNotFoundException {
			byte[] bytes = bytesCache.get(name);
			if(bytes == null) {
				bytes = loadBytesForClass(name);
				if(bytes != null) {
					bytesCache.put(name, bytes);
				} else {
					return null;
				}
			}
			return defineClass(name, bytes, 0, bytes.length);
		}
	}

}
