package io.weaviate.client6.v1.api.rbac.users;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.google.gson.reflect.TypeToken;

import io.weaviate.client6.v1.api.rbac.Role;
import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.json.JSON;
import io.weaviate.client6.v1.internal.rest.Endpoint;
import io.weaviate.client6.v1.internal.rest.SimpleEndpoint;
import io.weaviate.client6.v1.internal.rest.UrlEncoder;

public record GetAssignedRolesRequest(String userId, UserType userType, Boolean includePermissions) {

  @SuppressWarnings("unchecked")
  public static final Endpoint<GetAssignedRolesRequest, List<Role>> _ENDPOINT = SimpleEndpoint.noBody(
      __ -> "GET",
      request -> "/authz/users/" + UrlEncoder.encodeValue(request.userId) + "/roles/" + request.userType.jsonValue(),
      request -> request.includePermissions != null ? Map.of("includePermissions", request.includePermissions)
          : Collections.emptyMap(),
      (statusCode,
          response) -> (List<Role>) JSON.deserialize(response, TypeToken.getParameterized(List.class, Role.class)));

  public static GetAssignedRolesRequest of(String userId, UserType userType) {
    return of(userId, userType, ObjectBuilder.identity());
  }

  public static GetAssignedRolesRequest of(String userId, UserType userType,
      Function<Builder, ObjectBuilder<GetAssignedRolesRequest>> fn) {
    return fn.apply(new Builder(userId, userType)).build();
  }

  public GetAssignedRolesRequest(Builder builder) {
    this(builder.userId, builder.userType, builder.includePermissions);
  }

  public static class Builder implements ObjectBuilder<GetAssignedRolesRequest> {
    private final String userId;
    private final UserType userType;
    private Boolean includePermissions;

    public Builder(String userId, UserType userType) {
      this.userId = userId;
      this.userType = userType;
    }

    public Builder includePermissions(boolean includePermissions) {
      this.includePermissions = includePermissions;
      return this;
    }

    @Override
    public GetAssignedRolesRequest build() {
      return new GetAssignedRolesRequest(this);
    }
  }
}
