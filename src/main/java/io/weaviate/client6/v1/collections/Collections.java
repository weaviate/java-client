package io.weaviate.client6.v1.collections;

import java.io.IOException;
import java.util.Map;
import java.util.function.Consumer;

import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import io.weaviate.client6.Config;
import io.weaviate.client6.internal.GrpcClient;
import io.weaviate.client6.internal.HttpClient;
import io.weaviate.client6.v1.Collection;
import io.weaviate.client6.v1.collections.CollectionDefinition.Configuration;
import io.weaviate.client6.v1.collections.VectorIndex.IndexType;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class Collections {
  // TODO: hide befind an internal HttpClient
  private final Config config;

  private final HttpClient httpClient;
  private final GrpcClient grpcClient;

  // TODO: use singleton configured in one place
  private static final Gson gson = new GsonBuilder()
      // TODO: create TypeAdapters via TypeAdapterFactory
      .registerTypeAdapter(Vectors.class, new TypeAdapter<Vectors>() {
        Gson gson = new Gson();

        @Override
        public void write(JsonWriter out, Vectors value) throws IOException {
          var unnamed = value.getUnnamed();
          if (unnamed.isPresent()) {
            var index = unnamed.get();
            out.name("vectorIndexType");
            gson.toJson(index.type(), IndexType.class, out);
            out.name("vectorizer");
            gson.toJson(index.vectorizer(), Vectorizer.class, out);
            out.name("vectorIndexConfig");
            gson.toJson(index.configuration(), Configuration.class, out);
            return;
          }

          gson.toJson(value.asMap(), Map.class, out);
        }

        @Override
        public Vectors read(JsonReader in) throws IOException {
          // TODO Auto-generated method stub
          throw new UnsupportedOperationException("Unimplemented method 'read'");
        }

      })
      .registerTypeHierarchyAdapter(Vectorizer.class, new TypeAdapter<Vectorizer>() {
        Gson gson = new Gson();

        @Override
        public void write(JsonWriter out, Vectorizer value) throws IOException {
          gson.toJson(value, value.getClass(), out);
        }

        @Override
        public Vectorizer read(JsonReader in) throws IOException {
          return Vectorizer.none();
        }
      })
      .create();

  public void create(String name, Consumer<CollectionDefinition.Configuration> options) throws IOException {
    var collection = CollectionDefinition.with(name, options);

    ClassicHttpRequest httpPost = ClassicRequestBuilder
        .post(config.baseUrl() + "/schema")
        .setEntity(collection.toJson(gson), ContentType.APPLICATION_JSON)
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

  public Collection<Map<String, Object>> use(String name) {
    return new Collection<>(name, config, grpcClient, httpClient);
  }
}
