package io.weaviate.client.v1.async.schema.api;

import io.weaviate.client.Config;
import io.weaviate.client.base.AsyncBaseClient;
import io.weaviate.client.base.AsyncClientResult;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.WeaviateErrorMessage;
import io.weaviate.client.base.WeaviateErrorResponse;
import io.weaviate.client.v1.auth.provider.AccessTokenProvider;
import io.weaviate.client.v1.schema.model.WeaviateClass;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.core5.concurrent.FutureCallback;

public class ClassGetter extends AsyncBaseClient<WeaviateClass> implements AsyncClientResult<WeaviateClass> {

  private String className;

  public ClassGetter(CloseableHttpAsyncClient client, Config config, AccessTokenProvider tokenProvider) {
    super(client, config, tokenProvider);
  }

  public ClassGetter withClassName(String className) {
    this.className = className;
    return this;
  }

  @Override
  public Future<Result<WeaviateClass>> run(FutureCallback<Result<WeaviateClass>> callback) {
    if (StringUtils.isEmpty(this.className)) {
      WeaviateErrorMessage errorMessage = WeaviateErrorMessage.builder()
        .message("classname cannot be empty").build();
      WeaviateErrorResponse errors = WeaviateErrorResponse.builder()
        .error(Stream.of(errorMessage).collect(Collectors.toList())).build();
      return CompletableFuture.completedFuture(new Result<>(500, null, errors));
    }
    String path = String.format("/schema/%s", this.className);
    return sendGetRequest(path, WeaviateClass.class, callback);
  }
}
