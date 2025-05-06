package io.weaviate.client6.v1.collections;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.google.gson.stream.JsonWriter;

import io.weaviate.client6.v1.collections.object.WeaviateObject;

public record Reference(String collection, List<String> uuids) {

  public Reference(String collection, String uuid) {
    this(collection, List.of(uuid));
  }

  /**
   * Create reference to objects by their UUIDs.
   * <p>
   * Weaviate will search each of the existing collections to identify
   * the objects before inserting the references, so this may include
   * some performance overhead.
   */
  public static Reference uuids(String... uuids) {
    return new Reference(null, Arrays.asList(uuids));
  }

  /** Create references to {@link WeaviateObject}. */
  public static Reference[] objects(WeaviateObject<?>... objects) {
    return Arrays.stream(objects)
        .map(o -> new Reference(o.collection(), o.metadata().id()))
        .toArray(Reference[]::new);
  }

  /** Create references to objects in a collection by their UUIDs. */
  public static Reference collection(String collection, String... uuids) {
    return new Reference(collection, Arrays.asList(uuids));
  }

  // TODO: put this in a type adapter.
  /** writeValue assumes an array has been started will be ended by the caller. */
  public void writeValue(JsonWriter w) throws IOException {
    for (var uuid : uuids) {
      w.beginObject();
      w.name("beacon");
      w.value(toBeacon(uuid));
      w.endObject();
    }
  }

  private String toBeacon(String uuid) {
    var beacon = "weaviate://localhost/";
    if (collection != null) {
      beacon += collection + "/";
    }
    beacon += uuid;
    return beacon;
  }
}
