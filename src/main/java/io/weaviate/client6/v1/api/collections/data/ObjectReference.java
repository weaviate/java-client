package io.weaviate.client6.v1.api.collections.data;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import io.weaviate.client6.v1.api.collections.Reference;
import io.weaviate.client6.v1.api.collections.WeaviateObject;

public record ObjectReference(String collection, List<String> uuids) implements Reference {

  @Override
  public String uuid() {
    return uuids.get(0);
  }

  @Override
  public WeaviateObject<Map<String, Object>> asWeaviateObject() {
    throw new IllegalStateException("cannot convert to WeaviateObject");
  }

  public ObjectReference(String collection, String uuid) {
    this(collection, List.of(uuid));
  }

  /**
   * Create reference to objects by their UUIDs.
   * <p>
   * Weaviate will search each of the existing collections to identify
   * the objects before inserting the references, so this may include
   * some performance overhead.
   */
  public static ObjectReference uuids(String... uuids) {
    return new ObjectReference(null, Arrays.asList(uuids));
  }

  /** Create references to single {@link WeaviateObject}. */
  public static ObjectReference object(WeaviateObject<?> object) {
    return new ObjectReference(object.collection(), object.uuid());
  }

  /** Create references to multiple {@link WeaviateObject}. */
  public static ObjectReference[] objects(WeaviateObject<?>... objects) {
    return Arrays.stream(objects)
        .map(o -> new ObjectReference(o.collection(), o.uuid()))
        .toArray(ObjectReference[]::new);
  }

  /** Create references to objects in a collection by their UUIDs. */
  public static ObjectReference collection(String collection, String... uuids) {
    return new ObjectReference(collection, Arrays.asList(uuids));
  }

  public static String toBeacon(String collection, String uuid) {
    return toBeacon(collection, null, uuid);
  }

  public static String toBeacon(String collection, String property, String uuid) {
    var beacon = "weaviate://localhost";
    if (collection != null) {
      beacon += "/" + collection;
    }
    beacon += "/" + uuid;
    if (property != null) {
      beacon += "/" + property;
    }
    return beacon;
  }

  public static final TypeAdapter<ObjectReference> TYPE_ADAPTER = new TypeAdapter<ObjectReference>() {

    @Override
    public void write(JsonWriter out, ObjectReference value) throws IOException {
      for (var uuid : value.uuids()) {
        out.beginObject();
        out.name("beacon");
        out.value(toBeacon(value.collection(), uuid));
        out.endObject();
      }
    }

    @Override
    public ObjectReference read(JsonReader in) throws IOException {
      String collection = null;
      String id = null;

      in.beginObject();
      in.nextName(); // expect "beacon"?
      var beacon = in.nextString();

      // Skip to the end of the object. There's going to be the "href"
      // key too, which is irrelevant for us.
      while (in.peek() != JsonToken.END_OBJECT) {
        in.skipValue();
      }
      in.endObject();

      beacon = beacon.replaceFirst("weaviate://localhost/", "");
      if (beacon.contains("/")) {
        var parts = beacon.split("/");
        collection = parts[0];
        id = parts[1];
      } else {
        id = beacon;
      }

      return new ObjectReference(collection, id);
    }

  }.nullSafe();
}
