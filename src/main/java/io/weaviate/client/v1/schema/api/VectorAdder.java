package io.weaviate.client.v1.schema.api;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import io.weaviate.client.Config;
import io.weaviate.client.base.BaseClient;
import io.weaviate.client.base.ClientResult;
import io.weaviate.client.base.Response;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.http.HttpClient;
import io.weaviate.client.base.util.UrlEncoder;
import io.weaviate.client.v1.schema.model.WeaviateClass;
import io.weaviate.client.v1.schema.model.WeaviateClass.VectorConfig;

public class VectorAdder extends BaseClient<WeaviateClass> implements ClientResult<Boolean> {
  private final ClassGetter getter;

  private String className;
  private Map<String, VectorConfig> addedVectors = new HashMap<>();

  public VectorAdder(HttpClient httpClient, Config config) {
    super(httpClient, config);
    this.getter = new ClassGetter(httpClient, config);
  }

  public VectorAdder withClassName(String className) {
    this.className = className;
    return this;
  }

  /**
   * Add a named vectors. This method can be chained to add multiple named vectors
   * without using a {@link Map}.
   */
  public VectorAdder withVectorConfig(String name, VectorConfig vector) {
    this.addedVectors.put(name, vector);
    return this;
  }

  /**
   * Add all vectors from the map. This will overwrite any vectors added
   * previously.
   */
  public VectorAdder withVectorConfig(Map<String, VectorConfig> vectors) {
    this.addedVectors = Collections.unmodifiableMap(vectors);
    return this;
  }

  @Override
  public Result<Boolean> run() {
    Result<WeaviateClass> result = getter.withClassName(className).run();
    if (result.hasErrors()) {
      result.toErrorResult();
    }

    WeaviateClass cls = result.getResult();
    addedVectors.entrySet().stream()
        .forEach(vector -> cls.getVectorConfig()
            .putIfAbsent(vector.getKey(), vector.getValue()));

    String path = String.format("/schema/%s", UrlEncoder.encodePathParam(className));
    Response<WeaviateClass> resp = sendPutRequest(path, cls, WeaviateClass.class);
    return new Result<>(resp.getStatusCode(), resp.getStatusCode() == 200, resp.getErrors());
  }
}
