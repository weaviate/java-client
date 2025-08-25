package io.weaviate.client6.v1.api.collections.aggregate;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public record GroupedBy<T>(String property, T value) {
  public boolean isText() {
    return value instanceof String;
  }

  public String text() {
    checkPropertyType(this::isText, "TEXT");
    return (String) value;
  }

  public boolean isInteger() {
    return value instanceof Long;
  }

  public Long integer() {
    checkPropertyType(this::isInteger, "LONG");
    return (Long) value;
  }

  public boolean isBool() {
    return value instanceof Boolean;
  }

  public Boolean bool() {
    checkPropertyType(this::isBool, "BOOLEAN");
    return (Boolean) value;
  }

  public boolean isNumber() {
    return value instanceof Double;
  }

  public Double number() {
    checkPropertyType(this::isNumber, "NUMBER");
    return (Double) value;
  }

  public boolean isTextArray() {
    return value instanceof String[];
  }

  @SuppressWarnings("unchecked")
  public List<String> textArray() {
    checkPropertyType(this::isTextArray, "TEXT[]");
    return (List<String>) Arrays.asList(value);
  }

  public boolean isBoolArray() {
    return value instanceof Boolean[];
  }

  @SuppressWarnings("unchecked")
  public List<Boolean> boolArray() {
    checkPropertyType(this::isBoolArray, "BOOLEAN[]");
    return (List<Boolean>) Arrays.asList(value);
  }

  private void checkPropertyType(Supplier<Boolean> check, String expected) {
    if (!check.get()) {
      throw new IllegalStateException(property + "is not a " + expected + " property");
    }
  }
}
