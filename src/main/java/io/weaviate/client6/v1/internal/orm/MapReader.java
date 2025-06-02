package io.weaviate.client6.v1.internal.orm;

import java.util.Map;

public class MapReader implements PropertiesReader<Map<String, Object>> {
  private final Map<String, Object> properties;

  public MapReader(Map<String, Object> properties) {
    this.properties = properties;
  }

  @Override
  public Map<String, Object> readProperties() {
    return Map.copyOf(properties); // ensure original properties immutable
  }
}
