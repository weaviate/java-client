package io.weaviate.client6.v1.collections;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import io.weaviate.client6.Config;
import io.weaviate.client6.internal.DtoTypeAdapterFactory;
import io.weaviate.client6.internal.GrpcClient;
import io.weaviate.client6.internal.HttpClient;
import io.weaviate.client6.v1.collections.VectorIndex.IndexingStrategy;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class CollectionsClient {
  // TODO: hide befind an internal HttpClient
  private final Config config;

  private final HttpClient httpClient;
  private final GrpcClient grpcClient;

  static {
    DtoTypeAdapterFactory.register(
        Collection.class,
        CollectionDefinitionDTO.class,
        m -> {
          return new CollectionDefinitionDTO(m);
        });
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

  public void create(String name) throws IOException {
    create(name, opt -> {
    });
  }

  public void create(String name, Consumer<Collection.Configuration> options) throws IOException {
    var collection = Collection.with(name, options);
    ClassicHttpRequest httpPost = ClassicRequestBuilder
        .post(config.baseUrl() + "/schema")
        .setEntity(gson.toJson(collection), ContentType.APPLICATION_JSON)
        .build();

    // TODO: do not expose Apache HttpClient directly
    httpClient.http.execute(httpPost, response -> {
      var entity = response.getEntity();
      if (response.getCode() != HttpStatus.SC_SUCCESS) { // Does not return 201
        var message = EntityUtils.toString(entity);
        throw new RuntimeException("HTTP " + response.getCode() + ": " + message);
      }
      return null;
    });
  }

  public Optional<Collection> getConfig(String name) throws IOException {
    ClassicHttpRequest httpGet = ClassicRequestBuilder
        .get(config.baseUrl() + "/schema/" + name)
        .build();

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

  public void delete(String name) throws IOException {
    ClassicHttpRequest httpDelete = ClassicRequestBuilder
        .delete(config.baseUrl() + "/schema/" + name)
        .build();

    httpClient.http.execute(httpDelete, response -> {
      var entity = response.getEntity();
      if (response.getCode() != HttpStatus.SC_SUCCESS) { // Does not return 201
        var message = EntityUtils.toString(entity);
        throw new RuntimeException("HTTP " + response.getCode() + ": " + message);
      }
      return null;
    });
  }

  public CollectionClient<Map<String, Object>> use(String name) {
    return new CollectionClient<>(name, config, grpcClient, httpClient);
  }
}
