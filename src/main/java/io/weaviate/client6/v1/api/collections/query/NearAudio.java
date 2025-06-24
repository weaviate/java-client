package io.weaviate.client6.v1.api.collections.query;

import java.util.function.Function;

import io.weaviate.client6.v1.api.collections.aggregate.AggregateObjectFilter;
import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoAggregate;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBaseSearch;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoSearchGet;

public record NearAudio(String audio, Float distance, Float certainty, BaseQueryOptions common)
    implements QueryOperator, AggregateObjectFilter {

  public static NearAudio of(String audio) {
    return of(audio, ObjectBuilder.identity());
  }

  public static NearAudio of(String audio, Function<Builder, ObjectBuilder<NearAudio>> fn) {
    return fn.apply(new Builder(audio)).build();
  }

  public NearAudio(Builder builder) {
    this(
        builder.media,
        builder.distance,
        builder.certainty,
        builder.baseOptions());
  }

  public static class Builder extends NearMediaBuilder<Builder, NearAudio> {
    public Builder(String audio) {
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
    nearAudio.setAudio(audio);

    if (certainty != null) {
      nearAudio.setCertainty(certainty);
    } else if (distance != null) {
      nearAudio.setDistance(distance);
    }
    return nearAudio;
  }
}
