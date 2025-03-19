package io.weaviate.client6.v1.collections.aggregate;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import io.weaviate.client6.grpc.protocol.v1.WeaviateProtoAggregate.AggregateRequest.Aggregation;

public class IntegerMetric extends Metric<IntegerMetric> {
  private final Set<AggregateFunction> functions;

  public record Values(Long count, Long min, Long max, Double mean, Double median, Long mode, Long sum)
      implements Metric.Values {
  }

  IntegerMetric(String property, Consumer<Builder> options) {
    super(property);
    var opt = new Builder(options);
    this.functions = opt.functions;
  }

  private enum AggregateFunction {
    COUNT, MIN, MAX, MEAN, MEDIAN, MODE, SUM
  }

  public static class Builder {
    private final Set<AggregateFunction> functions = new HashSet<>();

    public Builder count() {
      functions.add(AggregateFunction.COUNT);
      return this;
    }

    public Builder min() {
      functions.add(AggregateFunction.MIN);
      return this;
    }

    public Builder max() {
      functions.add(AggregateFunction.MAX);
      return this;
    }

    public Builder mean() {
      functions.add(AggregateFunction.MEAN);
      return this;
    }

    public Builder median() {
      functions.add(AggregateFunction.MEDIAN);
      return this;
    }

    public Builder mode() {
      functions.add(AggregateFunction.MODE);
      return this;
    }

    public Builder sum() {
      functions.add(AggregateFunction.SUM);
      return this;
    }

    Builder(Consumer<Builder> options) {
      options.accept(this);
    }
  }

  void appendTo(Aggregation.Builder aggregation) {
    aggregation.setProperty(property);
    var integer = io.weaviate.client6.grpc.protocol.v1.WeaviateProtoAggregate.AggregateRequest.Aggregation.Integer
        .newBuilder();
    for (var f : functions) {
      switch (f) {
        case COUNT:
          integer.setCount(true);
          break;
        case MIN:
          integer.setMinimum(true);
          break;
        case MAX:
          integer.setMaximum(true);
          break;
        case MEAN:
          integer.setMean(true);
          break;
        case MODE:
          integer.setMode(true);
          break;
        case MEDIAN:
          integer.setMedian(true);
          break;
        case SUM:
          integer.setSum(true);
          break;
      }
    }
    aggregation.setInt(integer);
  }
}
