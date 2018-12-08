package com.tutorial.core.convert.converter;

import java.util.Set;

import com.tutorial.core.convert.TypeDescriptor;

/**
 * Generic converter interface for converting between two or more types.
 *
 * <p>This is the most flexible of the Converter SPI interfaces, but also the most complex.
 * It is flexible in that a GenericConverter may support converting between multiple source/target
 * type pairs (see {@link #getConvertibleTypes()}. In addition, GenericConverter implementations
 * have access to source/target {@link TypeDescriptor field context} during the type conversion process.
 * This allows for resolving source and target field metadata such as annotations and generics
 * information, which can be used influence the conversion logic.
 *
 * <p>This interface should generally not be used when the simpler {@link Converter} or
 * {@link ConverterFactory} interfaces are sufficient.
 *
 * @author Keith Donald
 * @author Juergen Hoeller
 * @since 3.0
 * @see TypeDescriptor
 * @see Converter
 * @see ConverterFactory
 */
public interface GenericConverter {
	
	/**
	 * Return the source and target types which this converter can convert between.
	 * <p>Each entry is a convertible source-to-target type pair.
	 */
	Set<ConvertiblePair> getConvertibleTypes();
	
	/**
	 * Convert the source to the targetType described by the TypeDescriptor.
	 * @param source the source object to convert (may be null)
	 * @param sourceType the type descriptor of the field we are converting from
	 * @param targetType the type descriptor of the field we are converting to
	 * @return the converted object
	 */
	Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType);
	
	/**
	 * Holder for a source-to-target class pair.
	 */
	public static final class ConvertiblePair {
		
		private final Class<?> sourceType;
		
		private final Class<?> targetType;
		
		public ConvertiblePair(Class<?> sourceType, Class<?> targetType) {
			this.sourceType = sourceType;
			this.targetType = targetType;
		}

		public Class<?> getSourceType() {
			return sourceType;
		}

		public Class<?> getTargetType() {
			return targetType;
		}
		
		@Override
        public boolean equals(Object obj) {
            if (this == obj) {
				return true;
			}
            if (obj == null || obj.getClass() != ConvertiblePair.class) {
				return false;
			}
            ConvertiblePair other = (ConvertiblePair) obj;
            return this.sourceType.equals(other.sourceType) && this.targetType.equals(other.targetType);

        }

        @Override
        public int hashCode() {
            return this.sourceType.hashCode() * 31 + this.targetType.hashCode();
        }
		
	}

}
