package technology.semi.weaviate.client.v1.data.api;

import technology.semi.weaviate.client.Config;
import technology.semi.weaviate.client.base.BaseClient;
import technology.semi.weaviate.client.base.ClientResult;
import technology.semi.weaviate.client.base.Response;
import technology.semi.weaviate.client.base.Result;
import technology.semi.weaviate.client.v1.data.model.SingleRef;
import technology.semi.weaviate.client.v1.data.util.ReferencesPath;

import java.util.Objects;

public class ReferenceCreator extends BaseClient<Object> implements ClientResult<Boolean> {

  private final ReferencesPath referencesPath;
  private String id;
  private String className;
  private String referenceProperty;
  private SingleRef referencePayload;

  public ReferenceCreator(Config config, ReferencesPath referencesPath) {
    super(config);
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
    String path = referencesPath.build(ReferencesPath.Params.builder()
            .id(id)
            .className(className)
            .property(referenceProperty)
            .build());
    Response<Object> resp = sendPostRequest(path, referencePayload, Object.class);
    return new Result<>(resp.getStatusCode(), resp.getStatusCode() == 200, resp.getErrors());
  }
}
