package io.weaviate.client6.v1.api.rbac.users;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.json.JSON;
import io.weaviate.client6.v1.internal.rest.Endpoint;
import io.weaviate.client6.v1.internal.rest.OptionalEndpoint;
import io.weaviate.client6.v1.internal.rest.UrlEncoder;

public record GetDbUserRequest(String userId, Boolean includeLastUsedAt) {

  public static final Endpoint<GetDbUserRequest, Optional<DbUser>> _ENDPOINT = OptionalEndpoint.noBodyOptional(
      __ -> "GET",
      request -> "/users/db/" + UrlEncoder.encodeValue(request.userId),
      request -> request.includeLastUsedAt != null
          ? Map.of("includeLastUsedTime", request.includeLastUsedAt)
          : Collections.emptyMap(),
      (statusCode, response) -> JSON.deserialize(response, DbUser.class));

  public static GetDbUserRequest of(String userId) {
    return of(userId, ObjectBuilder.identity());
  }

  public static GetDbUserRequest of(String userId, Function<Builder, ObjectBuilder<GetDbUserRequest>> fn) {
    return fn.apply(new Builder(userId)).build();
  }

  public GetDbUserRequest(Builder builder) {
    this(builder.userId, builder.includeLastUsedAt);
  }

  public static class Builder implements ObjectBuilder<GetDbUserRequest> {
    private final String userId;
    private Boolean includeLastUsedAt;

    public Builder(String userId) {
      this.userId = userId;
    }

    public Builder includeLastUsedAt(boolean includeLastUsedAt) {
      this.includeLastUsedAt = includeLastUsedAt;
      return this;
    }

    @Override
    public GetDbUserRequest build() {
      return new GetDbUserRequest(this);
    }
  }
}
