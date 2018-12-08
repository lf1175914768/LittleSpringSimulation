package com.tutorial.beans.propertyeditors;

import java.beans.PropertyEditorSupport;

import com.tutorial.util.ClassUtils;
import com.tutorial.util.ObjectUtils;
import com.tutorial.util.StringUtils;

/**
 * Property editor for an array of {@link java.lang.Class Classes}, to enable
 * the direct population of a <code>Class[]</code> property without having to
 * use a <code>String</code> class name property as bridge.
 *
 * <p>Also supports "java.lang.String[]"-style array class names, in contrast
 * to the standard {@link Class#forName(String)} method.
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 2.0
 */
public class ClassArrayEditor extends PropertyEditorSupport {

	private final ClassLoader classLoader;

	/**
	 * Create a default <code>ClassEditor</code>, using the thread
	 * context <code>ClassLoader</code>.
	 */
	public ClassArrayEditor() {
		this(null);
	}
	
	/**
	 * Create a default <code>ClassArrayEditor</code>, using the given
	 * <code>ClassLoader</code>.
	 * @param classLoader the <code>ClassLoader</code> to use
	 * (or pass <code>null</code> for the thread context <code>ClassLoader</code>)
	 */
	public ClassArrayEditor(ClassLoader classLoader) {
		this.classLoader = classLoader != null ? classLoader : ClassUtils.getDefaultClassLoader();
	}
	
	@Override
	public void setAsText(String text) throws IllegalArgumentException {
		if(StringUtils.hasText(text)) {
			String[] classNames = StringUtils.commaDelimitedListToStringArray(text);
			Class<?>[] classes = new Class[classNames.length];
			for(int i = 0; i < classNames.length; i++) {
				String className = classNames[i];
				classes[i] = ClassUtils.resolveClassName(className, classLoader);
			}
			setValue(classes);
		} else {
			setValue(null);
		}
	}
	
	@Override
	public String getAsText() {
		Class<?>[] classes = (Class[]) getValue();
		if(ObjectUtils.isEmpty(classes)) {
			return "";
		}
		
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < classes.length; i++) {
			if(i > 0) {
				sb.append(", ");
			}
			sb.append(ClassUtils.getQualifiedName(classes[i]));
		}
		return sb.toString();
	}
}
