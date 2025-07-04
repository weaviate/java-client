package io.weaviate.client6.v1.api.collections.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.hc.core5.http.HttpStatus;

import io.weaviate.client6.v1.internal.json.JSON;
import io.weaviate.client6.v1.internal.rest.Endpoint;

public record ReferenceAddManyRequest(List<BatchReference> references) {

  public static final Endpoint<ReferenceAddManyRequest, ReferenceAddManyResponse> endpoint(
      List<BatchReference> references) {
    return Endpoint.of(
        request -> "POST",
        request -> "/batch/references",
        (gson, request) -> JSON.serialize(request.references),
        request -> Collections.emptyMap(),
        code -> code != HttpStatus.SC_SUCCESS,
        (gson, response) -> {
          var result = JSON.deserialize(response, ReferenceAddManyResponse.class);
          var errors = new ArrayList<ReferenceAddManyResponse.BatchError>();

          for (var err : result.errors()) {
            errors.add(new ReferenceAddManyResponse.BatchError(
                err.message(),
                references.get(err.referenceIndex()),
                err.referenceIndex()));
          }
          return new ReferenceAddManyResponse(errors);
        });
  }

}
