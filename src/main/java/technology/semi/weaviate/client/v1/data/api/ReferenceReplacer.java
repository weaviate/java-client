package technology.semi.weaviate.client.v1.data.api;

import technology.semi.weaviate.client.Config;
import technology.semi.weaviate.client.base.BaseClient;
import technology.semi.weaviate.client.base.ClientResult;
import technology.semi.weaviate.client.base.Response;
import technology.semi.weaviate.client.base.Result;
import technology.semi.weaviate.client.v1.data.model.SingleRef;

public class ReferenceReplacer extends BaseClient<Object> implements ClientResult<Boolean> {
  private String id;
  private String referenceProperty;
  private SingleRef[] referencePayload;

  public ReferenceReplacer(Config config) {
    super(config);
  }

  public ReferenceReplacer withID(String id) {
    this.id = id;
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
    String path = String.format("/objects/%s/references/%s", this.id, this.referenceProperty);
    Response<Object> resp = sendPutRequest(path, this.referencePayload, Object.class);
    return new Result<>(resp.getStatusCode(), resp.getStatusCode() == 200, resp.getErrors());
  }
}
