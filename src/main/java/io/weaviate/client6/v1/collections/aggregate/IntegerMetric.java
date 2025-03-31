package io.weaviate.client6.v1.collections.aggregate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public record IntegerMetric(String property, List<_Function> functions) implements Metric {

  public record Values(Long count, Long min, Long max, Double mean, Double median, Long mode, Long sum)
      implements Metric.Values {
  }

  static IntegerMetric with(String property, Consumer<Builder> options) {
    var opt = new Builder(options);
    return new IntegerMetric(property, new ArrayList<>(opt.functions));
  }

  public enum _Function {
    COUNT, MIN, MAX, MEAN, MEDIAN, MODE, SUM;
  }

  public static class Builder {
    private final Set<_Function> functions = new HashSet<>();

    public Builder count() {
      functions.add(_Function.COUNT);
      return this;
    }

    public Builder min() {
      functions.add(_Function.MIN);
      return this;
    }

    public Builder max() {
      functions.add(_Function.MAX);
      return this;
    }

    public Builder mean() {
      functions.add(_Function.MEAN);
      return this;
    }

    public Builder median() {
      functions.add(_Function.MEDIAN);
      return this;
    }

    public Builder mode() {
      functions.add(_Function.MODE);
      return this;
    }

    public Builder sum() {
      functions.add(_Function.SUM);
      return this;
    }

    Builder(Consumer<Builder> options) {
      options.accept(this);
    }
  }
}
