package io.weaviate.client6.v1.api.alias;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.json.JSON;
import io.weaviate.client6.v1.internal.rest.Endpoint;
import io.weaviate.client6.v1.internal.rest.SimpleEndpoint;

public record ListAliasRequest(String collection) {
  public final static Endpoint<ListAliasRequest, List<Alias>> _ENDPOINT = SimpleEndpoint.noBody(
      __ -> "GET",
      __ -> "/aliases",
      request -> request.collection != null
          ? Map.of("class", request.collection)
          : Collections.emptyMap(),
      (statusCode, response) -> JSON.deserialize(response, ListAliasResponse.class).aliases());

  public static ListAliasRequest of() {
    return of(ObjectBuilder.identity());
  }

  public static ListAliasRequest of(Function<Builder, ObjectBuilder<ListAliasRequest>> fn) {
    return fn.apply(new Builder()).build();
  }

  public ListAliasRequest(Builder builder) {
    this(builder.collection);
  }

  public static class Builder implements ObjectBuilder<ListAliasRequest> {
    private String collection;

    public Builder collection(String collection) {
      this.collection = collection;
      return this;
    }

    @Override
    public ListAliasRequest build() {
      return new ListAliasRequest(this);
    }
  }
}
