package io.weaviate.client6.v1.api.collections.aggregate;

import java.util.Map;
import java.util.function.Function;

public record AggregateResponseGroup<T>(GroupedBy<T> groupedBy, Map<String, ? extends Object> properties,
    Long totalCount) {
  public boolean isText(String name) {
    return properties.get(name) instanceof TextAggregation.Values;
  }

  public TextAggregation.Values text(String name) {
    checkPropertyType(name, this::isText, "Text");
    return (TextAggregation.Values) this.properties.get(name);
  }

  public boolean isInteger(String name) {
    return properties.get(name) instanceof IntegerAggregation.Values;
  }

  public IntegerAggregation.Values integer(String name) {
    checkPropertyType(name, this::isInteger, "Integer");
    return (IntegerAggregation.Values) this.properties.get(name);
  }

  public boolean isBool(String name) {
    return properties.get(name) instanceof BooleanAggregation.Values;
  }

  public BooleanAggregation.Values bool(String name) {
    checkPropertyType(name, this::isBool, "Boolean");
    return (BooleanAggregation.Values) this.properties.get(name);
  }

  public boolean isDate(String name) {
    return properties.get(name) instanceof DateAggregation.Values;
  }

  public DateAggregation.Values date(String name) {
    checkPropertyType(name, this::isDate, "Date");
    return (DateAggregation.Values) this.properties.get(name);
  }

  private void checkPropertyType(String name, Function<String, Boolean> check, String expected) {
    if (!check.apply(name)) {
      throw new IllegalStateException(name + "is not a " + expected + " property");
    }
  }
}
