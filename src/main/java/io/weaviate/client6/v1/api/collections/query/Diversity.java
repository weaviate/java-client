package io.weaviate.client6.v1.api.collections.query;

import java.util.function.Function;

import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.TaggedUnion;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBaseSearch;

public interface Diversity extends TaggedUnion<Diversity.Kind, Object> {
  public enum Kind {
    MMR;
  }

  /** Use {@link Mmr} diversity selection. */
  public static Mmr mmr(Function<Mmr.Builder, ObjectBuilder<Mmr>> fn) {
    return Mmr.of(fn);
  }

  /** Return this diversity as concrete {@link Mmr} type. */
  public default Mmr asMmr() {
    return _as(Diversity.Kind.MMR);
  }

  /** Maximal Marginal Relevance diversity selection. */
  public record Mmr(Integer limit, Float balance) implements Diversity {

    @Override
    public Diversity.Kind _kind() {
      return Diversity.Kind.MMR;
    }

    @Override
    public Object _self() {
      return this;
    }

    public static Mmr of(Function<Builder, ObjectBuilder<Mmr>> fn) {
      return fn.apply(new Builder()).build();
    }

    public Mmr(Builder builder) {
      this(builder.limit, builder.balance);
    }

    public static class Builder implements ObjectBuilder<Mmr> {
      private Integer limit;
      private Float balance;

      public Builder limit(int limit) {
        this.limit = limit;
        return this;
      }

      public Builder balance(float balance) {
        this.balance = balance;
        return this;
      }

      @Override
      public Mmr build() {
        return new Mmr(this);
      }
    }
  }

  public default WeaviateProtoBaseSearch.Selection.Builder toProto() {
    var selection = WeaviateProtoBaseSearch.Selection.newBuilder();
    switch (_kind()) {
      case MMR:
        var mmrBuilder = WeaviateProtoBaseSearch.Selection.MMR.newBuilder();
        var mmr = asMmr();
        if (mmr.limit() != null) {
          mmrBuilder.setLimit(mmr.limit());
        }
        if (mmr.balance() != null) {
          mmrBuilder.setBalance(mmr.balance());
        }
        selection.setMmr(mmrBuilder);
        break;
    }
    return selection;
  }
}
