package io.weaviate.client6.v1.collections.aggregate;

import java.util.Map;

public record AggregateResult(Map<String, ? extends Metric.Values> properties, Long totalCount) {
  public boolean isTextProperties(String name) {
    return properties.get(name) instanceof TextMetric.Values;
  }

  public boolean isIntegerProperty(String name) {
    return properties.get(name) instanceof IntegerMetric.Values;
  }

  public TextMetric.Values getText(String name) {
    if (!isTextProperties(name)) {
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
