package io.weaviate.client6.v1.api.collections.data;

import java.util.List;

public record DeleteManyResponse(float took, long failed, long matches, long successful, List<DeletedObject> objects) {

  public static record DeletedObject(String uuid, boolean successful, String error) {
  }
}
