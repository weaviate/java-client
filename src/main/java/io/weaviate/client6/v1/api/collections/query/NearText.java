package io.weaviate.client6.v1.api.collections.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import io.weaviate.client6.v1.api.collections.aggregate.AggregateObjectFilter;
import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoAggregate;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBaseSearch;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoSearchGet;

public record NearText(List<String> concepts, Float distance, Float certainty, Move moveTo, Move moveAway,
    BaseQueryOptions common) implements QueryOperator, AggregateObjectFilter {

  public static NearText of(String... concepts) {
    return of(Arrays.asList(concepts), ObjectBuilder.identity());
  }

  public static NearText of(List<String> concepts) {
    return of(concepts, ObjectBuilder.identity());
  }

  public static NearText of(String text, Function<Builder, ObjectBuilder<NearText>> fn) {
    return of(List.of(text), fn);
  }

  public static NearText of(List<String> concepts, Function<Builder, ObjectBuilder<NearText>> fn) {
    return fn.apply(new Builder(concepts)).build();
  }

  public NearText(Builder builder) {
    this(
        builder.concepts,
        builder.distance,
        builder.certainty,
        builder.moveTo,
        builder.moveAway,
        builder.baseOptions());
  }

  public static class Builder extends BaseVectorSearchBuilder<Builder, NearText> {
    // Required query parameters.
    private final List<String> concepts;

    // Optional query parameter.
    private Move moveTo;
    private Move moveAway;

    public Builder(List<String> concepts) {
      this.concepts = concepts;
    }

    public final Builder moveTo(float force, Function<Move.Builder, ObjectBuilder<Move>> fn) {
      this.moveTo = fn.apply(new Move.Builder(force)).build();
      return this;
    }

    public final Builder moveAway(float force, Function<Move.Builder, ObjectBuilder<Move>> fn) {
      this.moveAway = fn.apply(new Move.Builder(force)).build();
      return this;
    }

    @Override
    public final NearText build() {
      return new NearText(this);
    }
  }

  public static record Move(Float force, List<String> objects, List<String> concepts) {

    public Move(Builder builder) {
      this(builder.force, builder.objects, builder.concepts);
    }

    public static class Builder implements ObjectBuilder<Move> {
      private final Float force;

      private List<String> objects = new ArrayList<>();
      private List<String> concepts = new ArrayList<>();

      public Builder(float force) {
        this.force = force;
      }

      public final Builder uuids(String... uuids) {
        this.objects = Arrays.asList(uuids);
        return this;
      }

      public final Builder concepts(String... concepts) {
        this.concepts = Arrays.asList(concepts);
        return this;
      }

      @Override
      public Move build() {
        return new Move(this);
      }
    }

    public final void appendTo(WeaviateProtoBaseSearch.NearTextSearch.Move.Builder move) {
      move.setForce(force);
      if (!objects.isEmpty()) {
        move.addAllUuids(objects);
      }
      if (!concepts.isEmpty()) {
        move.addAllConcepts(concepts);
      }
    }
  }

  @Override
  public void appendTo(WeaviateProtoSearchGet.SearchRequest.Builder req) {
    common.appendTo(req);
    req.setNearText(protoBuilder());
  }

  @Override
  public void appendTo(WeaviateProtoAggregate.AggregateRequest.Builder req) {
    if (common.limit() != null) {
      req.setLimit(common.limit());
    }
    req.setNearText(protoBuilder());
  }

  private WeaviateProtoBaseSearch.NearTextSearch.Builder protoBuilder() {
    var nearText = WeaviateProtoBaseSearch.NearTextSearch.newBuilder();
    nearText.addAllQuery(concepts);

    if (certainty != null) {
      nearText.setCertainty(certainty);
    } else if (distance != null) {
      nearText.setDistance(distance);
    }

    // TODO: add targets
    if (moveTo != null) {
      var to = WeaviateProtoBaseSearch.NearTextSearch.Move.newBuilder();
      moveTo.appendTo(to);
      nearText.setMoveTo(to);
    }

    if (moveAway != null) {
      var away = WeaviateProtoBaseSearch.NearTextSearch.Move.newBuilder();
      moveAway.appendTo(away);
      nearText.setMoveAway(away);
    }

    return nearText;
  }
}
