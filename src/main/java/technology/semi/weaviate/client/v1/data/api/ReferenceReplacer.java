package technology.semi.weaviate.client.v1.data.api;

import technology.semi.weaviate.client.Config;
import technology.semi.weaviate.client.base.BaseClient;
import technology.semi.weaviate.client.base.ClientResult;
import technology.semi.weaviate.client.base.Response;
import technology.semi.weaviate.client.base.Result;
import technology.semi.weaviate.client.v1.data.model.SingleRef;
import technology.semi.weaviate.client.v1.data.util.ReferencesPath;

import java.util.Objects;

public class ReferenceReplacer extends BaseClient<Object> implements ClientResult<Boolean> {

  private final ReferencesPath referencesPath;
  private String id;
  private String className;
  private String referenceProperty;
  private SingleRef[] referencePayload;

  public ReferenceReplacer(Config config, ReferencesPath referencesPath) {
    super(config);
    this.referencesPath = Objects.requireNonNull(referencesPath);
  }

  public ReferenceReplacer withID(String id) {
    this.id = id;
    return this;
  }

  public ReferenceReplacer withClassName(String className) {
    this.className = className;
    return this;
  }

  public ReferenceReplacer withReferenceProperty(String propertyName) {
    this.referenceProperty = propertyName;
    return this;
  }

  public ReferenceReplacer withReferences(SingleRef[] referencePayload) {
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
    Response<Object> resp = sendPutRequest(path, referencePayload, Object.class);
    return new Result<>(resp.getStatusCode(), resp.getStatusCode() == 200, resp.getErrors());
  }
}
