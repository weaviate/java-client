package io.weaviate.client6.v1.api.collections.query;

import java.util.function.Function;

import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoSearchGet;

public record Rerank(String property, String query) {

  public static Rerank by(String property) {
    return by(property, ObjectBuilder.identity());
  }

  public static Rerank by(String property, Function<Builder, ObjectBuilder<Rerank>> fn) {
    return fn.apply(new Builder(property)).build();
  }

  void appendTo(WeaviateProtoSearchGet.SearchRequest.Builder req) {
    var rerank = WeaviateProtoSearchGet.Rerank.newBuilder()
        .setProperty(property);

    if (query != null) {
      rerank.setQuery(query);
    }
    req.setRerank(rerank);
  }

  public Rerank(Builder builder) {
    this(builder.property, builder.query);
  }

  public static class Builder implements ObjectBuilder<Rerank> {
    private final String property;
    private String query;

    public Builder(String property) {
      this.property = property;
    }

    public Builder query(String query) {
      this.query = query;
      return this;
    }

    @Override
    public Rerank build() {
      return new Rerank(this);
    }
  }
}
