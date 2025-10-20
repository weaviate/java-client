package io.weaviate.client6.v1.api.collections.query;

import java.util.function.Function;

import io.weaviate.client6.v1.api.collections.aggregate.AggregateObjectFilter;
import io.weaviate.client6.v1.api.collections.query.Target.CombinedTextTarget;
import io.weaviate.client6.v1.api.collections.query.Target.TextTarget;
import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoAggregate;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBaseSearch;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoSearchGet;

public record NearAudio(Target searchTarget, Float distance, Float certainty, BaseQueryOptions common)
    implements QueryOperator, AggregateObjectFilter {

  public static NearAudio of(String audio) {
    return of(Target.blob(audio));
  }

  public static NearAudio of(String audio, Function<Builder, ObjectBuilder<NearAudio>> fn) {
    return of(Target.blob(audio), fn);
  }

  public static NearAudio of(Target searchTarget) {
    return of(searchTarget, ObjectBuilder.identity());
  }

  public static NearAudio of(Target searchTarget, Function<Builder, ObjectBuilder<NearAudio>> fn) {
    return fn.apply(new Builder(searchTarget)).build();
  }

  public NearAudio(Builder builder) {
    this(
        builder.media,
        builder.distance,
        builder.certainty,
        builder.baseOptions());
  }

  public static class Builder extends NearMediaBuilder<Builder, NearAudio> {
    public Builder(Target audio) {
      super(audio);
    }

    @Override
    public final NearAudio build() {
      return new NearAudio(this);
    }
  }

  @Override
  public void appendTo(WeaviateProtoSearchGet.SearchRequest.Builder req) {
    common.appendTo(req);
    req.setNearAudio(protoBuilder());
  }

  @Override
  public void appendTo(WeaviateProtoAggregate.AggregateRequest.Builder req) {
    if (common.limit() != null) {
      req.setLimit(common.limit());
    }
    req.setNearAudio(protoBuilder());
  }

  private WeaviateProtoBaseSearch.NearAudioSearch.Builder protoBuilder() {
    var nearAudio = WeaviateProtoBaseSearch.NearAudioSearch.newBuilder();
    if (searchTarget instanceof TextTarget text) {
      nearAudio.setAudio(text.query().get(0));
    } else if (searchTarget instanceof CombinedTextTarget combined) {
      nearAudio.setAudio(combined.query().get(0));
    }

    var targets = WeaviateProtoBaseSearch.Targets.newBuilder();
    if (searchTarget.appendTargets(targets)) {
      nearAudio.setTargets(targets);
    }

    if (certainty != null) {
      nearAudio.setCertainty(certainty);
    } else if (distance != null) {
      nearAudio.setDistance(distance);
    }
    return nearAudio;
  }
}
