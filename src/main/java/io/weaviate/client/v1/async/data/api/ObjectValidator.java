package io.weaviate.client.v1.async.data.api;

import io.weaviate.client.Config;
import io.weaviate.client.base.AsyncBaseClient;
import io.weaviate.client.base.AsyncClientResult;
import io.weaviate.client.base.Response;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.WeaviateErrorMessage;
import io.weaviate.client.base.WeaviateErrorResponse;
import io.weaviate.client.base.http.async.ResponseParser;
import io.weaviate.client.v1.auth.provider.AccessTokenProvider;
import io.weaviate.client.v1.data.model.WeaviateObject;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpResponse;

public class ObjectValidator extends AsyncBaseClient<Boolean> implements AsyncClientResult<Boolean> {
  private String id;
  private String className;
  private Map<String, Object> properties;

  public ObjectValidator(CloseableHttpAsyncClient client, Config config, AccessTokenProvider tokenProvider) {
    super(client, config, tokenProvider);
  }

  public ObjectValidator withID(String id) {
    this.id = id;
    return this;
  }

  public ObjectValidator withClassName(String className) {
    this.className = className;
    return this;
  }

  public ObjectValidator withProperties(Map<String, Object> properties) {
    this.properties = properties;
    return this;
  }

  @Override
  public Future<Result<Boolean>> run(FutureCallback<Result<Boolean>> callback) {
    if (StringUtils.isEmpty(id)) {
      WeaviateErrorMessage errorMessage = WeaviateErrorMessage.builder()
        .message("id cannot be empty").build();
      WeaviateErrorResponse errors = WeaviateErrorResponse.builder()
        .error(Stream.of(errorMessage).collect(Collectors.toList())).build();
      return CompletableFuture.completedFuture(new Result<>(500, false, errors));
    }
    WeaviateObject obj = WeaviateObject.builder()
      .className(className)
      .properties(properties)
      .id(id)
      .build();
    return sendPostRequest("/objects/validate", obj, callback, new ResponseParser<Boolean>() {
      @Override
      public Result<Boolean> parse(HttpResponse response, String body, ContentType contentType) {
        Response<WeaviateObject> resp = serializer.toResponse(response.getCode(), body, WeaviateObject.class);
        return new Result<>(resp.getStatusCode(), resp.getStatusCode() == 200, resp.getErrors());
      }
    });
  }
}
