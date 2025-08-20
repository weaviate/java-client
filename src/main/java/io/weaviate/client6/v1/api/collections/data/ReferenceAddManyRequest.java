package io.weaviate.client6.v1.api.collections.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.weaviate.client6.v1.api.collections.CollectionHandleDefaults;
import io.weaviate.client6.v1.api.collections.CollectionHandleDefaults.Location;
import io.weaviate.client6.v1.internal.json.JSON;
import io.weaviate.client6.v1.internal.rest.Endpoint;
import io.weaviate.client6.v1.internal.rest.SimpleEndpoint;

public record ReferenceAddManyRequest(List<BatchReference> references) {

  public static final Endpoint<ReferenceAddManyRequest, ReferenceAddManyResponse> endpoint(
      List<BatchReference> references,
      CollectionHandleDefaults defaults) {
    return defaults.endpoint(
        new SimpleEndpoint<>(
            request -> "POST",
            request -> "/batch/references",
            request -> Collections.emptyMap(),
            request -> JSON.serialize(request.references),
            (statusCode, response) -> {
              var result = JSON.deserialize(response, ReferenceAddManyResponse.class);
              var errors = new ArrayList<ReferenceAddManyResponse.BatchError>();

              for (var err : result.errors()) {
                errors.add(new ReferenceAddManyResponse.BatchError(
                    err.message(),
                    references.get(err.referenceIndex()),
                    err.referenceIndex()));
              }
              return new ReferenceAddManyResponse(errors);
            }),
        add -> add
            .consistencyLevel(Location.QUERY)
            .tenant(Location.QUERY));
  }

}
