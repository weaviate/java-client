package io.weaviate.client6.v1.collections.aggregate;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import io.weaviate.client6.grpc.protocol.v1.WeaviateProtoAggregate.AggregateRequest.Aggregation;
import io.weaviate.client6.grpc.protocol.v1.WeaviateProtoAggregate.AggregateRequest.Aggregation.Text;

public class TextMetric extends Metric<TextMetric> {
  private final Set<AggregateFunction> functions;
  private final boolean occurrenceCount;
  private final Integer atLeast;

  public record Values(Long count, List<TopOccurrence> topOccurrences) implements Metric.Values {
  }

  TextMetric(String property, Consumer<Builder> options) {
    super(property);

    var opt = new Builder(options);
    this.functions = opt.functions;
    this.occurrenceCount = opt.occurrenceCount;
    this.atLeast = opt.atLeast;
  }

  private enum AggregateFunction {
    COUNT, TYPE, TOP_OCCURENCES
  }

  public static class Builder {
    private final Set<AggregateFunction> functions = new HashSet<>();
    private boolean occurrenceCount = false;
    private Integer atLeast;

    public Builder count() {
      functions.add(AggregateFunction.COUNT);
      return this;
    }

    public Builder type() {
      functions.add(AggregateFunction.TYPE);
      return this;
    }

    public Builder topOccurences() {
      functions.add(AggregateFunction.TOP_OCCURENCES);
      return this;
    }

    public Builder topOccurences(int atLeast) {
      topOccurences();
      this.atLeast = atLeast;
      return this;
    }

    public Builder includeTopOccurencesCount() {
      topOccurences();
      this.occurrenceCount = true;
      return this;
    }

    Builder(Consumer<Builder> options) {
      options.accept(this);
    }
  }

  void appendTo(Aggregation.Builder aggregation) {
    aggregation.setProperty(property);
    var text = Text.newBuilder();
    for (var f : functions) {
      switch (f) {
        case TYPE:
          text.setType(true);
        case COUNT:
          text.setCount(true);
        case TOP_OCCURENCES:
          text.setTopOccurences(true);
          if (atLeast != null) {
            text.setTopOccurencesLimit(atLeast);
          }
      }
    }
    aggregation.setText(text);
  }
}
