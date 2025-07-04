package io.weaviate.client6.v1.api.collections.data;

import java.util.List;

public record InsertManyResponse(float took, List<InsertObject> responses, List<String> uuids, List<String> errors) {

  public static record InsertObject(String uuid, boolean successful, String error) {
  }
}
