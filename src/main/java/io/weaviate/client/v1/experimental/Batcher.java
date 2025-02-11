package io.weaviate.client.v1.experimental;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import io.weaviate.client.Config;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.http.HttpClient;
import io.weaviate.client.base.util.DbVersionSupport;
import io.weaviate.client.base.util.GrpcVersionSupport;
import io.weaviate.client.v1.auth.provider.AccessTokenProvider;
import io.weaviate.client.v1.batch.Batch;
import io.weaviate.client.v1.batch.api.ObjectsBatcher;
import io.weaviate.client.v1.batch.model.ObjectGetResponse;
import io.weaviate.client.v1.data.Data;
import io.weaviate.client.v1.data.model.WeaviateObject;
import lombok.AllArgsConstructor;

public class Batcher<T> implements AutoCloseable {
  private final Class<T> cls;
  private final ObjectsBatcher objectsBatcher;

  public Batcher(Config config, HttpClient httpClient, AccessTokenProvider tokenProvider, DbVersionSupport dbVersion,
      GrpcVersionSupport grpcVersion, Data data, Class<T> cls) {
    this.cls = cls;
    this.objectsBatcher = new Batch(httpClient, config, dbVersion, grpcVersion, tokenProvider, data).objectsBatcher();
  }

  public boolean insert(Consumer<InsertBatch<T>> data) {
    InsertBatch<T> batch = new InsertBatch<>(cls, data);
    batch.append(objectsBatcher);

    final Result<ObjectGetResponse[]> result = objectsBatcher.run();
    return !result.hasErrors();
  }

  @Override
  public void close() {
    this.objectsBatcher.close();
  }

  public static class InsertBatch<T> {
    private final Class<T> cls;
    private final List<$WeaviateObject<T>> objects = new ArrayList<>();

    public void add(T properties) {
      add(properties, null, null);
    }

    public void add(T properties, String id) {
      add(properties, id, null);
    }

    public void add(T properties, Float[] vector) {
      add(properties, null, vector);
    }

    public void add(T properties, String id, Float[] vector) {
      objects.add(new $WeaviateObject<T>(id, vector, properties));
    }

    InsertBatch(Class<T> cls, Consumer<InsertBatch<T>> populate) {
      this.cls = cls;
      populate.accept(this);
    }

    void append(ObjectsBatcher batcher) {
      for ($WeaviateObject<T> object : objects) {

        batcher.withObject(WeaviateObject.builder()
            .className(cls.getSimpleName() + "s")
            .vector(object.vector)
            .properties(toMap(object.properties))
            .id(object.id)
            .build());
      }
    }

    private Map<String, Object> toMap(T properties) {
      Map<String, Object> fieldMap = new HashMap<>();
      for (Field field : cls.getDeclaredFields()) {
        field.setAccessible(true);
        try {
          fieldMap.put(field.getName(), field.get(properties));
        } catch (IllegalAccessException e) {
          // Ignore
        }
      }
      return fieldMap;
    }

    @AllArgsConstructor
    private static class $WeaviateObject<T> {
      final String id;
      final Float[] vector;
      final T properties;
    }
  }
}
