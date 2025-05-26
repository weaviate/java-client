package io.weaviate.client6.v1.collections;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Optional;

import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import io.weaviate.client6.Config;
import io.weaviate.client6.internal.DtoTypeAdapterFactory;
import io.weaviate.client6.internal.HttpClient;
import io.weaviate.client6.v1.collections.VectorIndex.IndexingStrategy;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class CollectionConfigClient {
  // TODO: hide befind an internal HttpClient
  private final String collectionName;
  private final Config config;
  private final HttpClient httpClient;

  static {
    DtoTypeAdapterFactory.register(
        Collection.class,
        CollectionDefinitionDTO.class,
        m -> new CollectionDefinitionDTO(m));
  }

  // Gson cannot deserialize interfaces:
  // https://stackoverflow.com/a/49871339/14726116
  private static class IndexingStrategySerde
      implements JsonDeserializer<IndexingStrategy>, JsonSerializer<IndexingStrategy> {

    @Override
    public IndexingStrategy deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {
      return IndexingStrategy.hnsw();
    }

    @Override
    public JsonElement serialize(IndexingStrategy src, Type typeOfSrc, JsonSerializationContext context) {
      return context.serialize(src);
    }
  }

  // Gson cannot deserialize interfaces:
  // https://stackoverflow.com/a/49871339/14726116
  private static class VectorizerSerde
      implements JsonDeserializer<Vectorizer>, JsonSerializer<Vectorizer> {

    @Override
    public Vectorizer deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {
      return Vectorizer.none();
    }

    @Override
    public JsonElement serialize(Vectorizer src, Type typeOfSrc, JsonSerializationContext context) {
      return context.serialize(src);
    }
  }

  private static final Gson gson = new GsonBuilder()
      .registerTypeAdapterFactory(new DtoTypeAdapterFactory())
      .registerTypeAdapter(Vectorizer.class, new VectorizerSerde())
      .registerTypeAdapter(IndexingStrategy.class, new IndexingStrategySerde())
      .registerTypeAdapter(Vectors.class, new TypeAdapter<Vectors>() {
        Gson gson = new GsonBuilder()
            .registerTypeAdapter(Vectorizer.class, new VectorizerSerde())
            .registerTypeAdapter(IndexingStrategy.class, new IndexingStrategySerde())
            .create();

        @Override
        public void write(JsonWriter out, Vectors value) throws IOException {
          if (value != null) {
            gson.toJson(value.asMap(), Map.class, out);
          } else {
            out.nullValue();
          }
        }

        @Override
        public Vectors read(JsonReader in) throws IOException {
          Map<String, VectorIndex<? extends Vectorizer>> vectors = gson.fromJson(in,
              new TypeToken<Map<String, VectorIndex<? extends Vectorizer>>>() {
              }.getType());
          return Vectors.of(vectors);
        }
      })
      .create();

  public Optional<Collection> get() throws IOException {
    ClassicHttpRequest httpGet = ClassicRequestBuilder
        .get(config.baseUrl() + "/schema/" + collectionName)
        .build();

    ClassicRequestBuilder.create("GET").setUri("uri").setEntity("", ContentType.APPLICATION_JSON).build();

    return httpClient.http.execute(httpGet, response -> {
      if (response.getCode() == HttpStatus.SC_NOT_FOUND) {
        return Optional.empty();
      }
      try (var r = new InputStreamReader(response.getEntity().getContent())) {
        var collection = gson.fromJson(r, Collection.class);
        return Optional.ofNullable(collection);
      }
    });
  }

  public void addProperty(Property property) throws IOException {
    ClassicHttpRequest httpPost = ClassicRequestBuilder
        .post(config.baseUrl() + "/schema/" + collectionName + "/properties")
        .setEntity(gson.toJson(property), ContentType.APPLICATION_JSON)
        .build();

    httpClient.http.execute(httpPost, response -> {
      var entity = response.getEntity();
      if (response.getCode() != HttpStatus.SC_SUCCESS) {
        var message = EntityUtils.toString(entity);
        throw new RuntimeException("HTTP " + response.getCode() + ": " + message);
      }
      return null;
    });
  }

  public void addReference(String name, String... dataTypes) throws IOException {
    var property = Property.reference(name, dataTypes);
    ClassicHttpRequest httpPost = ClassicRequestBuilder
        .post(config.baseUrl() + "/schema/" + collectionName + "/properties")
        .setEntity(gson.toJson(property), ContentType.APPLICATION_JSON)
        .build();

    httpClient.http.execute(httpPost, response -> {
      var entity = response.getEntity();
      if (response.getCode() != HttpStatus.SC_SUCCESS) {
        var message = EntityUtils.toString(entity);
        throw new RuntimeException("HTTP " + response.getCode() + ": " + message);
      }
      return null;
    });
  }
}
