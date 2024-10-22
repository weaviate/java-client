package io.weaviate.client.v1.async.schema.api;

import io.weaviate.client.Config;
import io.weaviate.client.base.AsyncBaseClient;
import io.weaviate.client.base.AsyncClientResult;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.WeaviateError;
import io.weaviate.client.base.WeaviateErrorMessage;
import io.weaviate.client.base.WeaviateErrorResponse;
import io.weaviate.client.base.http.async.ResponseParser;
import io.weaviate.client.v1.schema.model.WeaviateClass;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.HttpStatus;

public class ClassExists extends AsyncBaseClient<Boolean> implements AsyncClientResult<Boolean> {
  private String className;

  public ClassExists(CloseableHttpAsyncClient client, Config config) {
    super(client, config);
  }

  public ClassExists withClassName(String className) {
    this.className = className;
    return this;
  }

  @Override
  public Future<Result<Boolean>> run() {
    return run(null);
  }

  @Override
  public Future<Result<Boolean>> run(FutureCallback<Result<Boolean>> callback) {
    if (StringUtils.isEmpty(this.className)) {
      WeaviateErrorMessage errorMessage = WeaviateErrorMessage.builder()
        .message("classname cannot be empty").build();
      WeaviateErrorResponse errors = WeaviateErrorResponse.builder()
        .error(Stream.of(errorMessage).collect(Collectors.toList())).build();
      return CompletableFuture.completedFuture(new Result<>(500, null, errors));
    }
    String path = String.format("/schema/%s", this.className);
    return sendGetRequest(path, callback, new ResponseParser<Boolean>() {
      @Override
      public Result<Boolean> parse(HttpResponse response, String body, ContentType contentType) {
        Result<WeaviateClass> getterClass = this.serializer.toResult(response.getCode(), body, WeaviateClass.class);
        if (getterClass.hasErrors()) {
          WeaviateError error = getterClass.getError();
          return new Result<>(error.getStatusCode(), null, WeaviateErrorResponse.builder().error(error.getMessages()).build());
        }
        return new Result<>(HttpStatus.SC_OK, getterClass.getResult() != null, null);
      }
    });
  }
}
