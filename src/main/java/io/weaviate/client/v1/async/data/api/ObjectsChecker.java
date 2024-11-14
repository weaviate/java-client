package io.weaviate.client.v1.async.data.api;

import io.weaviate.client.Config;
import io.weaviate.client.base.AsyncBaseClient;
import io.weaviate.client.base.AsyncClientResult;
import io.weaviate.client.base.Response;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.WeaviateErrorMessage;
import io.weaviate.client.base.WeaviateErrorResponse;
import io.weaviate.client.base.http.async.ResponseParser;
import io.weaviate.client.v1.data.util.ObjectsPath;
import java.util.Collections;
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

public class ObjectsChecker extends AsyncBaseClient<Boolean> implements AsyncClientResult<Boolean> {
  private final ObjectsPath objectsPath;
  private String id;
  private String className;
  private String tenant;

  public ObjectsChecker(CloseableHttpAsyncClient client, Config config, ObjectsPath objectsPath) {
    super(client, config);
    this.objectsPath = objectsPath;
  }

  public ObjectsChecker withID(String id) {
    this.id = id;
    return this;
  }

  public ObjectsChecker withClassName(String className) {
    this.className = className;
    return this;
  }

  public ObjectsChecker withTenant(String tenant) {
    this.tenant = tenant;
    return this;
  }

  @Override
  public Future<Result<Boolean>> run(FutureCallback<Result<Boolean>> callback) {
    if (StringUtils.isEmpty(this.id)) {
      WeaviateErrorMessage errorMessage = WeaviateErrorMessage.builder()
        .message("id cannot be empty").build();
      WeaviateErrorResponse errors = WeaviateErrorResponse.builder()
        .error(Stream.of(errorMessage).collect(Collectors.toList())).build();
      return CompletableFuture.completedFuture(new Result<>(500, false, errors));
    }
    String path = objectsPath.buildCheck(ObjectsPath.Params.builder()
      .id(id)
      .className(className)
      .tenant(tenant)
      .build());
    return sendHeadRequest(path, callback, new ResponseParser<Boolean>() {
      @Override
      public Result<Boolean> parse(HttpResponse response, String body, ContentType contentType) {
        Response<String> resp = serializer.toResponse(response.getCode(), null, String.class);
        switch (resp.getStatusCode()) {
          case HttpStatus.SC_NO_CONTENT:
          case HttpStatus.SC_NOT_FOUND:
            return new Result<>(resp.getStatusCode(), resp.getStatusCode() == HttpStatus.SC_NO_CONTENT, resp.getErrors());
          default:
            WeaviateErrorResponse dummyError = WeaviateErrorResponse.builder().error(Collections.emptyList()).build();
            return new Result<>(resp.getStatusCode(), resp.getStatusCode() == HttpStatus.SC_NO_CONTENT, dummyError);
        }
      }
    });
  }
}
