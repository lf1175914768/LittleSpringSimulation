package com.tutorial.core;

/**
 * Extension of the {@link Ordered} interface, expressing a 'priority'
 * ordering: Order values expressed by PriorityOrdered objects always
 * apply before order values of 'plain' Ordered values.
 *
 * <p>This is primarily a special-purpose interface, used for objects
 * where it is particularly important to determine 'prioritized'
 * objects first, without even obtaining the remaining objects.
 * A typical example: Prioritized post-processors in a Spring
 * {@link com.tutorial.context.ApplicationContext}.
 *
 * <p>Note: PriorityOrdered post-processor beans are initialized in
 * a special phase, ahead of other post-processor beans. This subtly
 * affects their autowiring behavior: They will only be autowired against
 * beans which do not require eager initialization for type matching.
 *
 * @author Juergen Hoeller
 * @since 2.5
 * @see com.tutorial.beans.factory.config.PropertyOverrideConfigurer
 * @see com.tutorial.beans.factory.config.PropertyPlaceholderConfigurer
 */
public interface PriorityOrdered extends Ordered {

}
