package io.weaviate.client6.v1.internal.orm;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
  public void setDouble(String property, Double value) {
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
  public void setUuid(String property, UUID value) {
    properties.put(property, value);
  }

  @Override
  public void setTextArray(String property, List<String> value) {
    properties.put(property, value);
  }

  @Override
  public void setUuidArray(String property, List<UUID> value) {
    properties.put(property, value);
  }

  @Override
  public void setBooleanArray(String property, List<Boolean> value) {
    properties.put(property, value);
  }

  @Override
  public void setOffsetDateTimeArray(String property, List<OffsetDateTime> value) {
    properties.put(property, value);
  }

  @Override
  public Map<String, Object> build() {
    return properties;
  }
}
