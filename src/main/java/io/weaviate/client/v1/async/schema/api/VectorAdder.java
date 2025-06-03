package io.weaviate.client.v1.async.schema.api;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpResponse;

import io.weaviate.client.Config;
import io.weaviate.client.base.AsyncBaseClient;
import io.weaviate.client.base.AsyncClientResult;
import io.weaviate.client.base.Response;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.http.async.ResponseParser;
import io.weaviate.client.base.util.UrlEncoder;
import io.weaviate.client.v1.auth.provider.AccessTokenProvider;
import io.weaviate.client.v1.schema.model.WeaviateClass;
import io.weaviate.client.v1.schema.model.WeaviateClass.VectorConfig;

public class VectorAdder extends AsyncBaseClient<Boolean> implements AsyncClientResult<Boolean> {
  private final ClassGetter getter;

  private String className;
  private Map<String, VectorConfig> addedVectors = new HashMap<>();

  public VectorAdder(CloseableHttpAsyncClient client, Config config, AccessTokenProvider tokenProvider) {
    super(client, config, tokenProvider);
    this.getter = new ClassGetter(client, config, tokenProvider);
  }

  public VectorAdder withClass(String className) {
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
  public Future<Result<Boolean>> run(FutureCallback<Result<Boolean>> callback) {
    CompletableFuture<Result<WeaviateClass>> getClass = CompletableFuture.supplyAsync(() -> {
      try {
        return getter.withClassName(className).run().get();
      } catch (InterruptedException | ExecutionException e) {
        throw new CompletionException(e);
      }
    });
    CompletableFuture<Result<Boolean>> addVectors = getClass.<Result<Boolean>>thenApplyAsync(result -> {
      if (result.getError() != null) {
        return result.toErrorResult();
      }
      WeaviateClass cls = result.getResult();
      addedVectors.entrySet().stream()
          .forEach(vector -> cls.getVectorConfig()
              .putIfAbsent(vector.getKey(), vector.getValue()));

      String path = String.format("/schema/%s", UrlEncoder.encodePathParam(className));
      try {
        return sendPutRequest(path, cls, callback, new ResponseParser<Boolean>() {

          @Override
          public Result<Boolean> parse(HttpResponse response, String body, ContentType contentType) {
            Response<WeaviateClass> resp = this.serializer.toResponse(response.getCode(), body, WeaviateClass.class);
            return new Result<>(response.getCode(), response.getCode() <= 299, resp.getErrors());
          }
        }).get();
      } catch (InterruptedException | ExecutionException e) {
        throw new CompletionException(e);
      }
    });

    return addVectors;
  }
}
