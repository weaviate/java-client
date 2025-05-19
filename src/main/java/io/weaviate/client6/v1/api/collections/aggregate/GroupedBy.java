package io.weaviate.client6.v1.api.collections.aggregate;

import java.util.function.Supplier;

public record GroupedBy<T>(String property, T value) {
  public boolean isText() {
    return value instanceof String;
  }

  public String text() {
    checkPropertyType(this::isText, "Text");
    return (String) value;
  }

  public boolean isInteger() {
    return value instanceof String;
  }

  public Integer integer() {
    checkPropertyType(this::isInteger, "Integer");
    return (Integer) value;
  }

  private void checkPropertyType(Supplier<Boolean> check, String expected) {
    if (!check.get()) {
      throw new IllegalStateException(property + "is not a " + expected + " property");
    }
  }
}
