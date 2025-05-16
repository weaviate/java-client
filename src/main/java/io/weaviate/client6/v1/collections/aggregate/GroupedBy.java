package io.weaviate.client6.v1.collections.aggregate;

import java.util.function.Function;

public record GroupedBy<T>(String property, T value) {
  public boolean isText() {
    return value instanceof String;
  }

  public String getAsText() {
    if (!isText()) {
      throw new IllegalStateException(property + " is not a Text property");
    }
    return (String) value;
  }

  private void checkPropertyType(String name, Function<String, Boolean> check, String expected) {
    if (!check.apply(name)) {
      throw new IllegalStateException(name + "is not a " + expected + " property");
    }
  }
}
