package io.weaviate.client6.v1.collections.aggregate;

import java.util.List;
import java.util.function.Consumer;

public interface Metric {
  String property();

  List<? extends Enum<?>> functions();

  public static TextMetric text(String property) {
    return TextMetric.with(property, _options -> {
    });
  }

  public static TextMetric text(String property, Consumer<TextMetric.Builder> options) {
    return TextMetric.with(property, options);
  }

  public static IntegerMetric integer(String property) {
    return IntegerMetric.with(property, _options -> {
    });
  }

  public static IntegerMetric integer(String property, Consumer<IntegerMetric.Builder> options) {
    return IntegerMetric.with(property, options);
  }

  public interface Values {
    Long count();
  }
}
