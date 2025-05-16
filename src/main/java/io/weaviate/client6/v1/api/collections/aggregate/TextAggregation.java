package io.weaviate.client6.v1.api.collections.aggregate;

import java.util.List;
import java.util.Set;
import java.util.function.Function;

import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoAggregate;

public class TextAggregation
    extends AbstractPropertyAggregation<WeaviateProtoAggregate.AggregateRequest.Aggregation.Text.Builder> {

  private final Integer topOccurrencesCutoff;

  public TextAggregation(String property,
      Set<Metric<WeaviateProtoAggregate.AggregateRequest.Aggregation.Text.Builder>> metrics,
      Integer topOccurrencesCutoff) {
    super(property, metrics);
    this.topOccurrencesCutoff = topOccurrencesCutoff;
  }

  public static TextAggregation of(String property, Function<Builder, ObjectBuilder<TextAggregation>> fn) {
    return fn.apply(new Builder(property)).build();
  }

  public TextAggregation(Builder builder) {
    this(builder.property, builder.metrics, builder.topOccurrencesCutoff);
  }

  public static class Builder extends
      AbstractPropertyAggregation.Builder<WeaviateProtoAggregate.AggregateRequest.Aggregation.Text.Builder, TextAggregation, Builder> {
    private Integer topOccurrencesCutoff;

    public Builder(String property) {
      super(property);
    }

    public final Builder count() {
      return addMetric(WeaviateProtoAggregate.AggregateRequest.Aggregation.Text.Builder::setCount);
    }

    public Builder type() {
      return addMetric(WeaviateProtoAggregate.AggregateRequest.Aggregation.Text.Builder::setType);
    }

    public Builder topOccurences() {
      return addMetric(WeaviateProtoAggregate.AggregateRequest.Aggregation.Text.Builder::setTopOccurences);
    }

    public Builder topOccurencesCutoff(int cutoff) {
      this.topOccurrencesCutoff = cutoff;
      return topOccurences();
    }

    @Override
    public final TextAggregation build() {
      return new TextAggregation(this);
    }
  }

  public static record TopOccurrence(String value, long occurrenceCount) {
  }

  public static record Values(Long count, List<TopOccurrence> topOccurrences) {
  }

  @Override
  public void appendTo(WeaviateProtoAggregate.AggregateRequest.Aggregation.Builder req) {
    super.appendTo(req);

    var text = WeaviateProtoAggregate.AggregateRequest.Aggregation.Text.newBuilder();
    if (topOccurrencesCutoff != null) {
      text.setTopOccurencesLimit(topOccurrencesCutoff);
    }

    appendMetrics(text);
    req.setText(text);
  }
}
