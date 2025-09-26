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
      var propertyName = PojoDescriptor.propertyName(field);
      if (field.trySetAccessible()) {
        try {
          out.put(propertyName, field.get(properties));
        } catch (IllegalAccessException e) {
          new RuntimeException("accessible flag set but access denied", e);
        }
      }
      // TODO: how do we handle the case where a property is not accessible?
      // E.g. we weren't able to set `accessible` flag.
    }
    return out;
  }
}
