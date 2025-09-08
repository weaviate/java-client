package io.weaviate.client6.v1.internal.orm;

import java.util.HashMap;
import java.util.Map;

final class PojoReader<PropertiesT> implements PropertiesReader<PropertiesT> {
  private final PropertiesT properties;

  PojoReader(PropertiesT properties) {
    this.properties = properties;
  }

  @Override
  public Map<String, Object> readProperties() {
    var out = new HashMap<String, Object>();
    for (var field : properties.getClass().getDeclaredFields()) {
      var propertyName = field.getName();
      field.setAccessible(true);
      try {
        out.put(propertyName, field.get(properties));
      } catch (IllegalAccessException e) {
        assert e == null : e.getMessage();
      }
    }
    return out;
  }
}
