package io.weaviate.client6.v1.collections.aggregate;

import java.util.Map;

public record Group<T>(GroupedBy<T> by, Map<String, ? extends Metric.Values> properties, int totalCount) {
  // TODO: have DataType util method for this?
  public boolean isTextProperties(String name) {
    return properties.get(name) instanceof TextMetric.Values;
  }

  public TextMetric.Values getText(String name) {
    if (!isTextProperties(name)) {
      throw new IllegalStateException(name + " is not a Text property");
    }
    return (TextMetric.Values) this.properties.get(name);
  }
}
