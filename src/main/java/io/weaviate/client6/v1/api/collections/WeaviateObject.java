package io.weaviate.client6.v1.api.collections;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.annotations.SerializedName;
import com.google.gson.internal.Streams;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import io.weaviate.client6.v1.api.collections.data.Reference;
import io.weaviate.client6.v1.api.collections.query.QueryMetadata;
import io.weaviate.client6.v1.internal.ObjectBuilder;

public record WeaviateObject<PropertiesT>(
    @SerializedName("id") String uuid,
    @SerializedName("class") String collection,
    @SerializedName("tenant") String tenant,
    @SerializedName("properties") PropertiesT properties,
    @SerializedName("vectors") Vectors vectors,
    @SerializedName("creationTimeUnix") Long createdAt,
    @SerializedName("lastUpdateTimeUnix") Long lastUpdatedAt,

    QueryMetadata queryMetadata,
    Map<String, List<IReference>> references) implements IReference {

  /**
   * Cast {@code this} into an instance of {@link IWeaviateObject<Map<String,
   * Object>>}. Useful when working with references retrieved in a query.
   *
   * <pre>{@code
   *  var metalSongs = songs.query.fetchObjects(q -> q
   *    .filters(Filter.property("genres").containsAll("metal")
   *    .returnReferences(QueryReference.multi("performedBy"));
   *
   *  metalSongs.objects().forEach(song -> {
   *    var songName = song.properties().get("name");
   *    song.references().forEach(ref -> {
   *      var artistName = ref.asWeaviateObject().properties().get("artistName");
   *      System.out.printf("%s is performed by %s", songName, artistName);
   *    });
   *  });
   * }</pre>
   *
   * <p>
   * Only call this method on objects returned from methods under {@code .query}
   * namespace, as insert-references do not implement this interface.
   *
   * @throws IllegalStateException if reference object is an instance of
   *                               {@link Reference}. See usage guidelines above.
   */
  @SuppressWarnings("unchecked")
  @Override
  public WeaviateObject<Map<String, Object>> asWeaviateObject() {
    return (WeaviateObject<Map<String, Object>>) this;
  }

  public static <PropertiesT> WeaviateObject<PropertiesT> of(
      Function<Builder<PropertiesT>, ObjectBuilder<WeaviateObject<PropertiesT>>> fn) {
    return fn.apply(new Builder<>()).build();
  }

  public WeaviateObject(Builder<PropertiesT> builder) {
    this(
        builder.uuid,
        null, // collection name is derived from CollectionHandle
        builder.tenant, // tenant MAY be derived from CollectionHandle
        builder.properties,
        builder.vectors,
        null, // createdAt is read-only
        null, // lastUpdatedAt is read-only
        null, // queryMetadata is read-only
        builder.references);
  }

  public static class Builder<PropertiesT> implements ObjectBuilder<WeaviateObject<PropertiesT>> {
    /**
     * The server <i>should be</i> providing default UUIDs, but it does not do that
     * during batch inserts and we have to provide our own.
     * Rather than make this behaviour special to {@code insertMany}, we are going
     * to provide a fallback UUID "globally".
     */
    private String uuid = UUID.randomUUID().toString();
    private String tenant;
    private PropertiesT properties;
    private Vectors vectors;
    private Map<String, List<IReference>> references = new HashMap<>();

    public Builder<PropertiesT> uuid(String uuid) {
      this.uuid = uuid;
      return this;
    }

    public Builder<PropertiesT> tenant(String tenant) {
      this.tenant = tenant;
      return this;
    }

    public Builder<PropertiesT> properties(PropertiesT properties) {
      this.properties = properties;
      return this;
    }

    /**
     * Add a reference. Calls to {@link #reference} can be chained
     * to add multiple references.
     */
    public Builder<PropertiesT> reference(String property, IReference... references) {
      for (var ref : references) {
        addReference(property, ref);
      }
      return this;
    }

    public Builder<PropertiesT> references(Map<String, List<IReference>> references) {
      this.references = references;
      return this;
    }

    private void addReference(String property, IReference reference) {
      if (!references.containsKey(property)) {
        references.put(property, new ArrayList<>());
      }
      references.get(property).add(reference);
    }

    public Builder<PropertiesT> vectors(Vectors... vectors) {
      if (this.vectors == null) {
        this.vectors = vectors.length == 1 ? vectors[0] : new Vectors(vectors);
      } else {
        this.vectors = this.vectors.withVectors(vectors);
      }
      return this;
    }

    @Override
    public WeaviateObject<PropertiesT> build() {
      return new WeaviateObject<>(this);
    }
  }

  public static enum CustomTypeAdapterFactory implements TypeAdapterFactory {
    INSTANCE;

    @SuppressWarnings("unchecked")
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
      var type = typeToken.getType();
      var rawType = typeToken.getRawType();
      if (rawType != WeaviateObject.class ||
          !(type instanceof ParameterizedType parameterized)
          || parameterized.getActualTypeArguments().length != 1) {
        return null;
      }

      var typeParams = parameterized.getActualTypeArguments();
      final var propertiesType = typeParams[0];

      final var delegate = (TypeAdapter<WeaviateObject<?>>) gson
          .getDelegateAdapter(this, typeToken);
      final var propertiesAdapter = (TypeAdapter<Object>) gson.getAdapter(TypeToken.get(propertiesType));
      final var referencesAdapter = gson.getAdapter(Reference.class);

      return (TypeAdapter<T>) new TypeAdapter<WeaviateObject<?>>() {

        @Override
        public void write(JsonWriter out, WeaviateObject<?> value) throws IOException {
          var json = delegate.toJsonTree(value).getAsJsonObject();
          var properties = value.properties() != null
              ? propertiesAdapter.toJsonTree(value.properties()).getAsJsonObject()
              : new JsonObject();

          if (value.references() != null && !value.references().isEmpty()) {
            for (var refEntry : value.references().entrySet()) {
              var beacons = new JsonArray();
              for (var reference : refEntry.getValue()) {
                var beacon = referencesAdapter.toJsonTree((Reference) reference);
                beacons.add(beacon);
              }
              properties.add(refEntry.getKey(), beacons);
            }
          }

          json.add("properties", properties);
          json.remove("references");
          Streams.write(json, out);
        }

        @Override
        public WeaviateObject<?> read(JsonReader in) throws IOException {
          var json = JsonParser.parseReader(in).getAsJsonObject();

          var jsonProperties = json.get("properties").getAsJsonObject();
          var objectProperties = new JsonObject();
          var objectReferences = new JsonObject();

          for (var property : jsonProperties.entrySet()) {
            var value = property.getValue();

            if (value.isJsonArray()) {
              var array = value.getAsJsonArray();
              var first = array.get(0);
              var isReference = first.isJsonObject() && first.getAsJsonObject().has("beacon");

              if (isReference) {
                objectReferences.add(property.getKey(), value);
                continue;
              }
            }

            objectProperties.add(property.getKey(), value);
          }

          json.add("references", objectReferences);
          json.add("properties", objectProperties);
          return delegate.fromJsonTree(json);
        }
      }.nullSafe();
    }
  }
}
