package io.weaviate.client6.v1.api.rbac.groups;

import java.util.Collections;
import java.util.List;

import com.google.gson.reflect.TypeToken;

import io.weaviate.client6.v1.internal.json.JSON;
import io.weaviate.client6.v1.internal.rest.Endpoint;
import io.weaviate.client6.v1.internal.rest.SimpleEndpoint;

public record GetKnownGroupNamesRequest() {

  @SuppressWarnings("unchecked")
  public static final Endpoint<Void, List<String>> _ENDPOINT = SimpleEndpoint.noBody(
      __ -> "GET",
      request -> "/authz/groups/oidc",
      request -> Collections.emptyMap(),
      (statusCode,
          response) -> (List<String>) JSON.deserialize(response, TypeToken.getParameterized(List.class, String.class)));
}
