package io.weaviate.client6.v1.api.rbac.users;

import java.util.Collections;

import io.weaviate.client6.v1.internal.json.JSON;
import io.weaviate.client6.v1.internal.rest.Endpoint;
import io.weaviate.client6.v1.internal.rest.SimpleEndpoint;
import io.weaviate.client6.v1.internal.rest.UrlEncoder;

public record RotateDbUserKeyRequest(String userId) {

  public static final Endpoint<RotateDbUserKeyRequest, String> _ENDPOINT = SimpleEndpoint.noBody(
      __ -> "POST",
      request -> "/users/db/" + UrlEncoder.encodeValue(request.userId) + "/rotate-key",
      request -> Collections.emptyMap(),
      (statusCode, response) -> JSON.deserialize(response, RotateDbUserKeyResponse.class).apiKey());
}
