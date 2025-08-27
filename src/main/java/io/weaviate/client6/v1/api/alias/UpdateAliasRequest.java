package io.weaviate.client6.v1.api.alias;

import java.util.Collections;

import com.google.gson.annotations.SerializedName;

import io.weaviate.client6.v1.internal.json.JSON;
import io.weaviate.client6.v1.internal.rest.Endpoint;
import io.weaviate.client6.v1.internal.rest.SimpleEndpoint;

public record UpdateAliasRequest(String alias, String newTargetCollection) {
  public final static Endpoint<UpdateAliasRequest, Void> _ENDPOINT = SimpleEndpoint.sideEffect(
      __ -> "PUT",
      request -> "/aliases/" + request.alias,
      __ -> Collections.emptyMap(),
      request -> JSON.serialize(request.toRequestBody()));

  private RequestBody toRequestBody() {
    return new RequestBody();
  }

  private class RequestBody {
    @SerializedName("class")
    private final String collection = UpdateAliasRequest.this.newTargetCollection;
  }
}
