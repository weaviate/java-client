package io.weaviate.client6.v1.internal.orm;

import java.util.Date;

public interface PropertiesBuilder<T> {
  void setText(String property, String value);

  void setBoolean(String property, Boolean value);

  void setInteger(String property, Long value);

  void setNumber(String property, Number value);

  void setDate(String property, Date value);

  T build();
}
