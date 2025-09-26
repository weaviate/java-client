package io.weaviate.client6.v1.internal.orm;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MapReader implements PropertiesReader<Map<String, Object>> {
  private final Map<String, Object> properties;

  public MapReader(Map<String, Object> properties) {
    // Defensive copy to ensure original properties are not modified
    this.properties = Collections.unmodifiableMap(new HashMap<>(properties));
  }

  @Override
  public Map<String, Object> readProperties() {
    return properties;
  }
}
