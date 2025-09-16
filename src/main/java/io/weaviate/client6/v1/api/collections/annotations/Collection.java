package io.weaviate.client6.v1.api.collections.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Collection {
  /** The name of the collection mapped by this class. */
  String value();

  /** Collection description to add on creation. */
  String description() default "";
}
