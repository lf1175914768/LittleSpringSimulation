package com.tutorial.beans.support;

import java.beans.PropertyEditor;
import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

import org.xml.sax.InputSource;

import com.tutorial.beans.PropertyEditorRegistrar;
import com.tutorial.beans.PropertyEditorRegistry;
import com.tutorial.beans.PropertyEditorRegistrySupport;
import com.tutorial.beans.propertyeditors.ClassArrayEditor;
import com.tutorial.beans.propertyeditors.ClassEditor;
import com.tutorial.beans.propertyeditors.FileEditor;
import com.tutorial.beans.propertyeditors.InputSourceEditor;
import com.tutorial.beans.propertyeditors.InputStreamEditor;
import com.tutorial.beans.propertyeditors.URIEditor;
import com.tutorial.beans.propertyeditors.URLEditor;
import com.tutorial.core.env.PropertyResolver;
import com.tutorial.core.io.ContextResource;
import com.tutorial.core.io.Resource;
import com.tutorial.core.io.ResourceEditor;
import com.tutorial.core.io.ResourceLoader;
import com.tutorial.core.io.support.ResourceArrayPropertyEditor;
import com.tutorial.core.io.support.ResourcePatternResolver;

/**
 * /**
 * PropertyEditorRegistrar implementation that populates a given
 * {@link com.tutorial.beans.PropertyEditorRegistry}
 * (typically a {@link com.tutorial.beans.BeanWrapper} used for bean
 * creation within an {@link com.tutorial.context.ApplicationContext})
 * with resource editors. Used by
 * {@link com.tutorial.context.support.AbstractApplicationContext}.
 *
 * @since 2.0
 * @author Liufeng
 * Created on 2018年11月24日 上午10:14:49
 */
public class ResourceEditorRegistrar implements PropertyEditorRegistrar {
	
	private final PropertyResolver propertyResolver;
	
	private final ResourceLoader resourceLoader;
	
	/**
	 * Create a new ResourceEditorRegistrar for the given ResourceLoader
	 * @param resourceLoader the ResourceLoader (or ResourcePatternResolver)
	 * to create editors for (usually an ApplicationContext)
	 * @see com.tutorial.core.io.support.ResourcePatternResolver
	 * @see com.tutorial.context.ApplicationContext
	 */
	public ResourceEditorRegistrar(ResourceLoader resourceLoader, PropertyResolver propertyResolver) {
		this.propertyResolver = propertyResolver;
		this.resourceLoader = resourceLoader;
	}

	/**
	 * Populate the given <code>registry</code> with the following resource editors:
	 * ResourceEditor, InputStreamEditor, InputSourceEditor, FileEditor, URLEditor,
	 * URIEditor, ClassEditor, ClassArrayEditor.
	 * <p>If this registrar has been configured with a {@link ResourcePatternResolver},
	 * a ResourceArrayPropertyEditor will be registered as well.
	 * @see com.tutorial.core.io.ResourceEditor
	 * @see com.tutorial.beans.propertyeditors.InputStreamEditor
	 * @see com.tutorial.beans.propertyeditors.InputSourceEditor
	 * @see com.tutorial.beans.propertyeditors.FileEditor
	 * @see com.tutorial.beans.propertyeditors.URLEditor
	 * @see com.tutorial.beans.propertyeditors.URIEditor
	 * @see com.tutorial.beans.propertyeditors.ClassEditor
	 * @see com.tutorial.beans.propertyeditors.ClassArrayEditor
	 * @see com.tutorial.core.io.support.ResourceArrayPropertyEditor
	 */
	@Override
	public void registerCustomEditors(PropertyEditorRegistry registry) {
		ResourceEditor baseEditor = new ResourceEditor(this.resourceLoader, this.propertyResolver);
		doRegisterEditor(registry, Resource.class, baseEditor);
		doRegisterEditor(registry, ContextResource.class, baseEditor);
		doRegisterEditor(registry, InputStream.class, new InputStreamEditor(baseEditor));
		doRegisterEditor(registry, InputSource.class, new InputSourceEditor(baseEditor));
		doRegisterEditor(registry, File.class, new FileEditor(baseEditor));
		doRegisterEditor(registry, URL.class, new URLEditor(baseEditor));
		
		ClassLoader classLoader = this.resourceLoader.getClassLoader();
		doRegisterEditor(registry, URI.class, new URIEditor(classLoader));
		doRegisterEditor(registry, Class.class, new ClassEditor(classLoader));
		doRegisterEditor(registry, Class[].class, new ClassArrayEditor(classLoader));
		
		if(this.resourceLoader instanceof ResourcePatternResolver) {
			doRegisterEditor(registry, Resource[].class, 
					new ResourceArrayPropertyEditor((ResourcePatternResolver) this.resourceLoader, 
							this.propertyResolver));
		}
	}

	/**
	 * Override default editor, if possible (since that's what we really mean to do here);
	 * otherwise register as a custom editor.
	 */
	private void doRegisterEditor(PropertyEditorRegistry registry, Class<?> requiredType, PropertyEditor editor) {
		if(registry instanceof PropertyEditorRegistrySupport) {
			((PropertyEditorRegistrySupport) registry).overrideDefaultEditor(requiredType, editor);
 		} else {  
 			registry.registerCustomEditor(requiredType, editor);
 		}
	}

}
