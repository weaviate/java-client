package io.weaviate.client6.v1.collections.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import io.weaviate.client6.grpc.protocol.v1.WeaviateProtoBaseSearch;

public record NearText(List<String> text, Float distance, Float certainty, Move moveTo, Move moveAway,
    CommonQueryOptions common) {

  public static NearText with(String text, Consumer<Builder> fn) {
    return with(List.of(text), fn);
  }

  public static NearText with(List<String> text, Consumer<Builder> fn) {
    var opt = new Builder();
    fn.accept(opt);
    return new NearText(text, opt.distance, opt.certainty, opt.moveTo, opt.moveAway, new CommonQueryOptions(opt));
  }

  public static class Builder extends CommonQueryOptions.Builder<Builder> {
    private Float distance;
    private Float certainty;
    private Move moveTo;
    private Move moveAway;

    public Builder distance(float distance) {
      this.distance = distance;
      return this;
    }

    public Builder certainty(float certainty) {
      this.certainty = certainty;
      return this;
    }

    public Builder moveTo(float force, Consumer<Move> fn) {
      var move = new Move(force);
      fn.accept(move);
      this.moveTo = move;
      return this;
    }

    public Builder moveAway(float force, Consumer<Move> fn) {
      var move = new Move(force);
      fn.accept(move);
      this.moveAway = move;
      return this;
    }

  }

  public static class Move {
    private final Float force;
    private List<String> objects = new ArrayList<>();
    private List<String> concepts = new ArrayList<>();

    Move(float force) {
      this.force = force;
    }

    public Move uuids(String... uuids) {
      this.objects = Arrays.asList(uuids);
      return this;
    }

    public Move concepts(String... concepts) {
      this.concepts = Arrays.asList(concepts);
      return this;
    }

    public void appendTo(WeaviateProtoBaseSearch.NearTextSearch.Move.Builder move) {
      move.setForce(force);
      if (!objects.isEmpty()) {
        move.addAllUuids(objects);
      }
      if (!concepts.isEmpty()) {
        move.addAllConcepts(concepts);
      }
    }
  }

  public static record GroupBy(String property, int maxGroups, int maxObjectsPerGroup) {
  }
}
