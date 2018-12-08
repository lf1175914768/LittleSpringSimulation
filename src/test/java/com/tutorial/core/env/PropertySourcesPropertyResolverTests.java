package com.tutorial.core.env;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

import com.tutorial.core.convert.ConversionException;
import com.tutorial.mock.env.MockPropertySource;

public class PropertySourcesPropertyResolverTests {
	
	private Properties testProperties;
	private MutablePropertySources propertySources;
	private ConfigurablePropertyResolver propertyResolver;
	
	@Before
	public void setUp() {
		propertySources = new MutablePropertySources();
		propertyResolver = new PropertySourcesPropertyResolver(propertySources);
		testProperties = new Properties();
		propertySources.addFirst(new PropertiesPropertySource("testProperties", testProperties));
	}
	
	@Test
	public void containsProperty() {
		assertThat(propertyResolver.containsProperty("foo"), is(false));
		testProperties.put("foo", "bar");
		assertThat(propertyResolver.containsProperty("foo"), is(true));
	}
	
	@Test
	public void getProperty() {
		assertThat(propertyResolver.getProperty("foo"), nullValue());
		testProperties.put("foo", "bar");
		assertThat(propertyResolver.getProperty("foo"), is("bar"));
	}
	
	@Test
	public void getProperty_withDefaultValue() {
		assertThat(propertyResolver.getProperty("foo", "myDefault"), is("myDefault"));
		testProperties.put("foo", "bar");
		assertThat(propertyResolver.getProperty("foo"), is("bar"));
	}
	
	@Test
	public void getProperty_propertySourceSearchOrderIsFIFO() {
		MutablePropertySources source = new MutablePropertySources();
		PropertyResolver resolver = new PropertySourcesPropertyResolver(source);
		source.addFirst(new MockPropertySource("ps1").withProperty("pName", "ps1Value"));
		assertThat(resolver.getProperty("pName"), equalTo("ps1Value"));
		source.addFirst(new MockPropertySource("ps2").withProperty("pName", "ps2Value"));
		assertThat(resolver.getProperty("pName"), equalTo("ps2Value"));
		source.addFirst(new MockPropertySource("ps3").withProperty("pName", "ps3Value"));
		assertThat(resolver.getProperty("pName"), equalTo("ps3Value"));
	}
	
	@Test
	public void getProperty_withExplicitNullValue() {
		Map<String, Object> nullableProperties = new HashMap<String, Object>();
		propertySources.addLast(new MapPropertySource("nullableProperties", nullableProperties));
		nullableProperties.put("foo", null);
		assertThat(propertyResolver.getProperty("foo"), nullValue());
	}
	
	@Test
	public void getProperty_withTargetType_andDefaultValue() {
		assertThat(propertyResolver.getProperty("foo", Integer.class, 42), equalTo(42));
		testProperties.put("foo", 13);
		assertThat(propertyResolver.getProperty("foo", Integer.class, 42), equalTo(13));
	}
	
	@Test
	public void getProperty_withStringArrayConversion() {
		testProperties.put("foo", "bar,baz");
		assertThat(propertyResolver.getProperty("foo", String[].class), equalTo(new String[] { "bar", "baz" }));
	}
	
	@Test
	public void getProperty_withNonConvertibleTargetType() {
		testProperties.put("foo", "bar");

		class TestType { }

		try {
			propertyResolver.getProperty("foo", TestType.class);
			fail("Expected IllegalArgumentException due to non-convertible types");
		} catch (IllegalArgumentException ex) {
			// expected
		}
	}
	
	@Test
	public void getProperty_doesNotCache_replaceExistingKeyPostConstruction() {
		String key = "foo";
		String value1 = "bar";
		String value2 = "biz";

		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put(key, value1); // before construction
		MutablePropertySources propertySources = new MutablePropertySources();
		propertySources.addFirst(new MapPropertySource("testProperties", map));
		PropertyResolver propertyResolver = new PropertySourcesPropertyResolver(propertySources);
		assertThat(propertyResolver.getProperty(key), equalTo(value1));
		map.put(key, value2); // after construction and first resolution
		assertThat(propertyResolver.getProperty(key), equalTo(value2));
	}

	@Test
	public void getProperty_doesNotCache_addNewKeyPostConstruction() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		MutablePropertySources propertySources = new MutablePropertySources();
		propertySources.addFirst(new MapPropertySource("testProperties", map));
		PropertyResolver propertyResolver = new PropertySourcesPropertyResolver(propertySources);
		assertThat(propertyResolver.getProperty("foo"), equalTo(null));
		map.put("foo", "42");
		assertThat(propertyResolver.getProperty("foo"), equalTo("42"));
	}

	@Test
	public void getPropertySources_replacePropertySource() {
		propertySources = new MutablePropertySources();
		propertyResolver = new PropertySourcesPropertyResolver(propertySources);
		propertySources.addLast(new MockPropertySource("local").withProperty("foo", "localValue"));
		propertySources.addLast(new MockPropertySource("system").withProperty("foo", "systemValue"));

		// 'local' was added first so has precedence
		assertThat(propertyResolver.getProperty("foo"), equalTo("localValue"));

		// replace 'local' with new property source
		propertySources.replace("local", new MockPropertySource("new").withProperty("foo", "newValue"));

		// 'system' now has precedence
		assertThat(propertyResolver.getProperty("foo"), equalTo("newValue"));

		assertThat(propertySources.size(), is(2));
	}

	@Test
	public void getRequiredProperty() {
		testProperties.put("exists", "xyz");
		assertThat(propertyResolver.getRequiredProperty("exists"), is("xyz"));

		try {
			propertyResolver.getRequiredProperty("bogus");
			fail("expected IllegalStateException");
		} catch (IllegalStateException ex) {
			// expected
		}
	}

	@Test
	public void getRequiredProperty_withStringArrayConversion() {
		testProperties.put("exists", "abc,123");
		assertThat(propertyResolver.getRequiredProperty("exists", String[].class), equalTo(new String[] { "abc", "123" }));

		try {
			propertyResolver.getRequiredProperty("bogus", String[].class);
			fail("expected IllegalStateException");
		} catch (IllegalStateException ex) {
			// expected
		}
	}
	
	@Test
	public void resolvePlaceholders() {
		MutablePropertySources propertySources = new MutablePropertySources();
		propertySources.addFirst(new MockPropertySource().withProperty("key", "value"));
		PropertyResolver resolver = new PropertySourcesPropertyResolver(propertySources);
		assertThat(resolver.resolvePlaceHolders("Replace this ${key}"), equalTo("Replace this value"));
	}
	
	@Test
	public void resolvePlaceholders_withUnResolvable() {
		MutablePropertySources propertySources = new MutablePropertySources();
		propertySources.addFirst(new MockPropertySource().withProperty("key", "value"));
		PropertyResolver resolver = new PropertySourcesPropertyResolver(propertySources);
		assertThat(resolver.resolvePlaceHolders("Replace this ${key} plus ${unknown}"), 
				equalTo("Replace this value plus ${unknown}")); 
	}
	
	@Test
	public void resolvePlaceholders_withDefaultValue() {
		MutablePropertySources propertySources = new MutablePropertySources();
		propertySources.addFirst(new MockPropertySource().withProperty("key", "value"));
		PropertyResolver resolver = new PropertySourcesPropertyResolver(propertySources);
		assertThat(resolver.resolvePlaceHolders("Replace this ${key} plus ${unknown:defaultValue}"),
				equalTo("Replace this value plus defaultValue"));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void resolvePlaceholders_withNullInput() {
		new PropertySourcesPropertyResolver(new MutablePropertySources()).resolvePlaceHolders(null); 
	}
	
	@Test
	public void resolveRequiredPlaceholders() {
		MutablePropertySources propertySources = new MutablePropertySources();
		propertySources.addFirst(new MockPropertySource().withProperty("key", "value"));
		PropertyResolver resolver = new PropertySourcesPropertyResolver(propertySources);
		assertThat(resolver.resolveRequiredPlaceHolders("Replace this ${key}"), equalTo("Replace this value"));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void resolveRequiredPlaceholders_withUnresolvable() {
		MutablePropertySources propertySources = new MutablePropertySources();
		propertySources.addFirst(new MockPropertySource().withProperty("key", "value"));
		PropertyResolver resolver = new PropertySourcesPropertyResolver(propertySources);
		resolver.resolveRequiredPlaceHolders("Replace this ${key} plus ${unknown}");
	}
	
	@Test
	public void resolveRequiredPlaceholders_withDefaultValue() {
		MutablePropertySources propertySources = new MutablePropertySources();
		propertySources.addFirst(new MockPropertySource().withProperty("key", "value"));
		PropertyResolver resolver = new PropertySourcesPropertyResolver(propertySources);
		assertThat(resolver.resolveRequiredPlaceHolders("Replace this ${key} plus ${unknown:defaultValue}"),
				equalTo("Replace this value plus defaultValue"));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void resolveRequiredPlaceholders_withNullInput() {
		new PropertySourcesPropertyResolver(new MutablePropertySources()).resolveRequiredPlaceHolders(null);
	}
	
	@Test
	public void getPropertyAsClass() {
		MutablePropertySources propertySources = new MutablePropertySources();
		propertySources.addFirst(new MockPropertySource().withProperty("some.class", SpecificType.class.getName()));
		PropertyResolver resolver = new PropertySourcesPropertyResolver(propertySources);
		assertTrue(resolver.getPropertyAsClass("some.class", SomeType.class).equals(SpecificType.class));
	}
	
	@Test
	public void getPropertyAsClass_withInterfaceAsTarget() throws ClassNotFoundException, LinkageError {
		MutablePropertySources propertySources = new MutablePropertySources();
		propertySources.addFirst(new MockPropertySource().withProperty("some.class", SomeType.class.getName()));
		PropertyResolver resolver = new PropertySourcesPropertyResolver(propertySources);
		assertTrue(resolver.getPropertyAsClass("some.class", SomeType.class).equals(SomeType.class));
	}
	
	@Test(expected=ConversionException.class)
	public void getPropertyAsClass_withMismatchedTypeForValue() {
		MutablePropertySources propertySources = new MutablePropertySources();
		propertySources.addFirst(new MockPropertySource().withProperty("some.class", "java.lang.String"));
		PropertyResolver resolver = new PropertySourcesPropertyResolver(propertySources);
		resolver.getPropertyAsClass("some.class", SomeType.class);
	}
	
	@Test(expected=ConversionException.class)
	public void getPropertyAsClass_withNonExistentClassForValue() {
		MutablePropertySources propertySources = new MutablePropertySources();
		propertySources.addFirst(new MockPropertySource().withProperty("some.class", "some.bogus.Class"));
		PropertyResolver resolver = new PropertySourcesPropertyResolver(propertySources);
		resolver.getPropertyAsClass("some.class", SomeType.class);
	}
	
	@Test
	public void getPropertyAsClass_withObjectForValue() {
		MutablePropertySources propertySources = new MutablePropertySources();
		propertySources.addFirst(new MockPropertySource().withProperty("some.class", new SpecificType()));
		PropertyResolver resolver = new PropertySourcesPropertyResolver(propertySources);
		assertTrue(resolver.getPropertyAsClass("some.class", SomeType.class).equals(SpecificType.class));
	}

	@Test(expected=ConversionException.class)
	public void getPropertyAsClass_withMismatchedObjectForValue() {
		MutablePropertySources propertySources = new MutablePropertySources();
		propertySources.addFirst(new MockPropertySource().withProperty("some.class", new Integer(42)));
		PropertyResolver resolver = new PropertySourcesPropertyResolver(propertySources);
		resolver.getPropertyAsClass("some.class", SomeType.class);
	}
	
	@Test
	public void getPropertyAsClass_withRealClassForValue() {
		MutablePropertySources propertySources = new MutablePropertySources();
		propertySources.addFirst(new MockPropertySource().withProperty("some.class", SpecificType.class));
		PropertyResolver resolver = new PropertySourcesPropertyResolver(propertySources);
		assertTrue(resolver.getPropertyAsClass("some.class", SomeType.class).equals(SpecificType.class));
	}

	@Test(expected=ConversionException.class)
	public void getPropertyAsClass_withMismatchedRealClassForValue() {
		MutablePropertySources propertySources = new MutablePropertySources();
		propertySources.addFirst(new MockPropertySource().withProperty("some.class", Integer.class));
		PropertyResolver resolver = new PropertySourcesPropertyResolver(propertySources);
		resolver.getPropertyAsClass("some.class", SomeType.class);
	}
	
	@Test
	public void setRequiredProperties_andValidateRequiredProperties() {
		// no properties have been marked as required -> validation should pass
		propertyResolver.validateRequiredProperties();

		// mark which properties are required
		propertyResolver.setRequiredProperties("foo", "bar");

		// neither foo nor bar properties are present -> validating should throw
		try {
			propertyResolver.validateRequiredProperties();
			fail("expected validation exception");
		} catch (MissingRequiredPropertiesException ex) {
			assertThat(ex.getMessage(), equalTo(
					"The following properties were declared as required " +
					"but could not be resolved: [foo, bar]"));
		}

		// add foo property -> validation should fail only on missing 'bar' property
		testProperties.put("foo", "fooValue");
		try {
			propertyResolver.validateRequiredProperties();
			fail("expected validation exception");
		} catch (MissingRequiredPropertiesException ex) {
			assertThat(ex.getMessage(), equalTo(
					"The following properties were declared as required " +
					"but could not be resolved: [bar]"));
		}

		// add bar property -> validation should pass, even with an empty string value
		testProperties.put("bar", "");
		propertyResolver.validateRequiredProperties();
	}

	static interface SomeType {}
	static class SpecificType implements SomeType {}
	
}