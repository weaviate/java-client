package io.weaviate.client6.v1.api.collections.data;

import java.io.IOException;
import java.util.Arrays;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import io.weaviate.client6.v1.api.collections.WeaviateObject;

public record BatchReference(String fromCollection, String fromProperty, String fromUuid, ObjectReference reference) {

  public static BatchReference[] objects(WeaviateObject<?> fromObject, String fromProperty,
      WeaviateObject<?>... toObjects) {
    return Arrays.stream(toObjects)
        .map(to -> new BatchReference(
            fromObject.collection(), fromProperty, fromObject.uuid(),
            ObjectReference.object(to)))
        .toArray(BatchReference[]::new);
  }

  public static BatchReference[] uuids(WeaviateObject<?> fromObject, String fromProperty,
      String... toUuids) {
    return Arrays.stream(toUuids)
        .map(to -> new BatchReference(
            fromObject.collection(), fromProperty, fromObject.uuid(),
            ObjectReference.uuid(to)))
        .toArray(BatchReference[]::new);
  }

  public static final TypeAdapter<BatchReference> TYPE_ADAPTER = new TypeAdapter<BatchReference>() {

    @Override
    public void write(JsonWriter out, BatchReference value) throws IOException {
      out.beginObject();

      out.name("from");
      out.value(ObjectReference.toBeacon(value.fromCollection, value.fromProperty, value.fromUuid));

      out.name("to");
      out.value(ObjectReference.toBeacon(value.reference.collection(), value.reference.uuid()));

      // TODO: add tenant

      out.endObject();
    }

    @Override
    public BatchReference read(JsonReader in) throws IOException {
      String fromCollection = null;
      String fromProperty = null;
      String fromUuid = null;
      ObjectReference toReference = null;

      in.beginObject();
      while (in.hasNext()) {
        switch (in.nextName()) {

          case "from": {
            var beacon = in.nextString();
            beacon = beacon.replaceFirst("weaviate://localhost/", "");

            var parts = beacon.split("/");
            fromCollection = parts[0];
            fromUuid = parts[1];
            fromProperty = parts[2];
            break;
          }

          case "to": {
            String collection = null;
            String id = null;

            var beacon = in.nextString();
            beacon = beacon.replaceFirst("weaviate://localhost/", "");
            if (beacon.contains("/")) {
              var parts = beacon.split("/");
              collection = parts[0];
              id = parts[1];
            } else {
              id = beacon;
            }
            toReference = new ObjectReference(collection, id);
            break;
          }

          // case "tenant":
          // switch (in.peek()) {
          // case STRING:
          // in.nextString();
          // case NULL:
          // in.nextNull();
          // default:
          // // We don't expect anything else
          // }
          // System.out.println("processed tenant");
          // break;
          // default:
          // in.skipValue();
        }
      }
      in.endObject();

      return new BatchReference(fromCollection, fromProperty, fromUuid, toReference);
    }
  }.nullSafe();
}
