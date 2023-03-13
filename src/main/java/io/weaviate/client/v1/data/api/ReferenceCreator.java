package io.weaviate.client.v1.data.api;

import io.weaviate.client.v1.data.model.SingleRef;
import io.weaviate.client.v1.data.util.ReferencesPath;
import io.weaviate.client.Config;
import io.weaviate.client.base.BaseClient;
import io.weaviate.client.base.ClientResult;
import io.weaviate.client.base.Response;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.http.HttpClient;

import java.util.Objects;

public class ReferenceCreator extends BaseClient<Object> implements ClientResult<Boolean> {

  private final ReferencesPath referencesPath;
  private String id;
  private String className;
  private String consistencyLevel;
  private String referenceProperty;
  private SingleRef referencePayload;

  public ReferenceCreator(HttpClient httpClient, Config config, ReferencesPath referencesPath) {
    super(httpClient, config);
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

  public ReferenceCreator withReferenceProperty(String propertyName) {
    this.referenceProperty = propertyName;
    return this;
  }

  public ReferenceCreator withReference(SingleRef referencePayload) {
    this.referencePayload = referencePayload;
    return this;
  }

  @Override
  public Result<Boolean> run() {
    String path = referencesPath.buildCreate(ReferencesPath.Params.builder()
            .id(id)
            .className(className)
            .consistencyLevel(consistencyLevel)
            .property(referenceProperty)
        .build());
    Response<Object> resp = sendPostRequest(path, referencePayload, Object.class);
    return new Result<>(resp.getStatusCode(), resp.getStatusCode() == 200, resp.getErrors());
  }
}
