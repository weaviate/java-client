package io.weaviate.client6.v1.api.rbac.users;

import java.util.Collections;

import io.weaviate.client6.v1.internal.json.JSON;
import io.weaviate.client6.v1.internal.rest.Endpoint;
import io.weaviate.client6.v1.internal.rest.SimpleEndpoint;
import io.weaviate.client6.v1.internal.rest.UrlEncoder;

public record CreateDbUserRequest(String userId) {

  public static final Endpoint<CreateDbUserRequest, String> _ENDPOINT = SimpleEndpoint.noBody(
      __ -> "POST",
      request -> "/users/db/" + UrlEncoder.encodeValue(request.userId),
      request -> Collections.emptyMap(),
      (statusCode, response) -> JSON.deserialize(response, CreateDbUserResponse.class).apiKey());
}
