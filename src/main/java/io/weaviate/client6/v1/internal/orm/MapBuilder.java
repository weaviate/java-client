package io.weaviate.client6.v1.internal.orm;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

public class MapBuilder implements PropertiesBuilder<Map<String, Object>> {
  private final Map<String, Object> properties = new HashMap<>();

  @Override
  public void setNull(String property) {
    properties.put(property, null);
  }

  @Override
  public void setText(String property, String value) {
    properties.put(property, value);
  }

  @Override
  public void setBoolean(String property, Boolean value) {
    properties.put(property, value);
  }

  @Override
  public void setInteger(String property, Long value) {
    properties.put(property, value);
  }

  @Override
  public void setNumber(String property, Number value) {
    properties.put(property, value);
  }

  @Override
  public void setBlob(String property, String value) {
    properties.put(property, value);
  }

  @Override
  public void setOffsetDateTime(String property, OffsetDateTime value) {
    properties.put(property, value);
  }

  @Override
  public Map<String, Object> build() {
    return properties;
  }
}
