package io.weaviate.client6.v1.collections.aggregate;

import java.util.Map;

public record Group<T>(GroupedBy<T> by, Map<String, ? extends Metric.Values> properties, Long totalCount) {
  // TODO: have DataType util method for this?
  public boolean isTextProperty(String name) {
    return properties.get(name) instanceof TextMetric.Values;
  }

  public boolean isIntegerProperty(String name) {
    return properties.get(name) instanceof IntegerMetric.Values;
  }

  public TextMetric.Values getText(String name) {
    if (!isTextProperty(name)) {
      throw new IllegalStateException(name + " is not a Text property");
    }
    return (TextMetric.Values) this.properties.get(name);
  }

  public IntegerMetric.Values getInteger(String name) {
    if (!isIntegerProperty(name)) {
      throw new IllegalStateException(name + " is not a Integer property");
    }
    return (IntegerMetric.Values) this.properties.get(name);
  }
}
