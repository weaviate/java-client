package io.weaviate.client6.v1.api.backup;

import java.util.Collections;
import java.util.List;

import com.google.gson.reflect.TypeToken;

import io.weaviate.client6.v1.internal.json.JSON;
import io.weaviate.client6.v1.internal.rest.Endpoint;
import io.weaviate.client6.v1.internal.rest.SimpleEndpoint;

public record ListBackupsRequest(String backend) {

  @SuppressWarnings("unchecked")
  public static Endpoint<ListBackupsRequest, List<Backup>> _ENDPOINT = SimpleEndpoint.noBody(
      request -> "GET",
      request -> "/backups/" + request.backend,
      request -> Collections.emptyMap(),
      (statusCode, response) -> (List<Backup>) JSON.deserialize(
          response, TypeToken.getParameterized(List.class, Backup.class)));
}
