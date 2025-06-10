package io.weaviate.client6.v1.api.collections.data;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import io.weaviate.client6.v1.api.collections.WeaviateObject;

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
  public static Reference[] objects(WeaviateObject<?, ?, ?>... objects) {
    return Arrays.stream(objects)
        .map(o -> new Reference(o.collection(), o.metadata().uuid()))
        .toArray(Reference[]::new);
  }

  /** Create references to objects in a collection by their UUIDs. */
  public static Reference collection(String collection, String... uuids) {
    return new Reference(collection, Arrays.asList(uuids));
  }

  public static final TypeAdapter<Reference> TYPE_ADAPTER = new TypeAdapter<Reference>() {

    @Override
    public void write(JsonWriter out, Reference value) throws IOException {
      for (var uuid : value.uuids()) {
        out.beginObject();
        out.name("beacon");

        var beacon = "weaviate://localhost";
        if (value.collection() != null) {
          beacon += "/" + value.collection();
        }
        beacon += "/" + uuid;

        out.value(beacon);
        out.endObject();
      }
    }

    @Override
    public Reference read(JsonReader in) throws IOException {
      String collection = null;
      String id = null;

      in.beginObject();
      in.nextName(); // expect "beacon"?
      var beacon = in.nextString();
      in.endObject();

      beacon = beacon.replaceFirst("weaviate://localhost/", "");
      if (beacon.contains("/")) {
        var parts = beacon.split("/");
        collection = parts[0];
        id = parts[1];
      } else {
        id = beacon;
      }

      return new Reference(collection, id);
    }

  }.nullSafe();
}
