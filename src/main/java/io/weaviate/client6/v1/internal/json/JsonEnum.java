package io.weaviate.client6.v1.internal.json;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public interface JsonEnum<E extends Enum<E>> {
  String jsonValue();

  static <E extends Enum<E>> Map<String, E> collectNames(JsonEnum<E>[] values) {
    final var jsonValueMap = new HashMap<String, E>(values.length);
    for (var value : values) {
      @SuppressWarnings("unchecked")
      var enumInstance = (E) value;
      jsonValueMap.put(value.jsonValue(), enumInstance);
    }
    return Collections.unmodifiableMap(jsonValueMap);
  }

  static <E extends Enum<E>> E valueOfJson(String jsonValue, Map<String, E> enums, Class<E> cls) {
    if (!enums.containsKey(jsonValue)) {
      throw new IllegalArgumentException("%s does not have a member with jsonValue=%s".formatted(cls, jsonValue));
    }
    return enums.get(jsonValue);
  }
}
