package io.weaviate.client6.v1.api.rbac.users;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.google.gson.reflect.TypeToken;

import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.json.JSON;
import io.weaviate.client6.v1.internal.rest.Endpoint;
import io.weaviate.client6.v1.internal.rest.SimpleEndpoint;

public record ListDbUsersRequest(Boolean includeLastUsedAt) {

  @SuppressWarnings("unchecked")
  public static final Endpoint<ListDbUsersRequest, List<DbUser>> _ENDPOINT = SimpleEndpoint.noBody(
      __ -> "GET",
      request -> "/users/db",
      request -> request.includeLastUsedAt != null
          ? Map.of("includeLastUsedTime", request.includeLastUsedAt)
          : Collections.emptyMap(),
      (statusCode,
          response) -> (List<DbUser>) JSON.deserialize(response,
              TypeToken.getParameterized(List.class, DbUser.class)));

  public static ListDbUsersRequest of() {
    return of(ObjectBuilder.identity());
  }

  public static ListDbUsersRequest of(Function<Builder, ObjectBuilder<ListDbUsersRequest>> fn) {
    return fn.apply(new Builder()).build();
  }

  public ListDbUsersRequest(Builder builder) {
    this(builder.includeLastUsedAt);
  }

  public static class Builder implements ObjectBuilder<ListDbUsersRequest> {
    private Boolean includeLastUsedAt;

    public Builder includeLastUsedAt(boolean includeLastUsedAt) {
      this.includeLastUsedAt = includeLastUsedAt;
      return this;
    }

    @Override
    public ListDbUsersRequest build() {
      return new ListDbUsersRequest(this);
    }
  }
}
