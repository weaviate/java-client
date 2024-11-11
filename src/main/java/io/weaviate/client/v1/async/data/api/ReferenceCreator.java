package io.weaviate.client.v1.async.data.api;

import io.weaviate.client.Config;
import io.weaviate.client.base.AsyncBaseClient;
import io.weaviate.client.base.AsyncClientResult;
import io.weaviate.client.base.Response;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.http.async.ResponseParser;
import io.weaviate.client.v1.data.model.SingleRef;
import io.weaviate.client.v1.data.util.ReferencesPath;
import java.util.Objects;
import java.util.concurrent.Future;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpResponse;

public class ReferenceCreator extends AsyncBaseClient<Boolean> implements AsyncClientResult<Boolean> {
  private final ReferencesPath referencesPath;
  private String id;
  private String className;
  private String consistencyLevel;
  private String tenant;
  private String referenceProperty;
  private SingleRef referencePayload;

  public ReferenceCreator(CloseableHttpAsyncClient client, Config config, ReferencesPath referencesPath) {
    super(client, config);
    this.referencesPath = Objects.requireNonNull(referencesPath);
  }

  public ReferenceCreator withID(String id) {
    this.id = id;
    return this;
  }

  public ReferenceCreator withClassName(String className) {
    this.className = className;
    return this;
  }

  public ReferenceCreator withConsistencyLevel(String consistencyLevel) {
    this.consistencyLevel = consistencyLevel;
    return this;
  }

  public ReferenceCreator withTenant(String tenant) {
    this.tenant = tenant;
    return this;
  }

  public ReferenceCreator withReferenceProperty(String propertyName) {
    this.referenceProperty = propertyName;
    return this;
  }

  public ReferenceCreator withReference(SingleRef referencePayload) {
    this.referencePayload = referencePayload;
    return this;
  }

  @Override
  public Future<Result<Boolean>> run() {
    return run(null);
  }

  @Override
  public Future<Result<Boolean>> run(FutureCallback<Result<Boolean>> callback) {
    String path = referencesPath.buildCreate(ReferencesPath.Params.builder()
      .id(id)
      .className(className)
      .consistencyLevel(consistencyLevel)
      .tenant(tenant)
      .property(referenceProperty)
      .build());
    return sendPostRequest(path, referencePayload, callback, new ResponseParser<Boolean>() {
      @Override
      public Result<Boolean> parse(HttpResponse response, String body, ContentType contentType) {
        Response<Object> resp = serializer.toResponse(response.getCode(), body, Object.class);
        return new Result<>(resp.getStatusCode(), resp.getStatusCode() == 200, resp.getErrors());
      }
    });
  }
}
