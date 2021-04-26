package technology.semi.weaviate.client.v1.data.api;

import technology.semi.weaviate.client.Config;
import technology.semi.weaviate.client.base.BaseClient;
import technology.semi.weaviate.client.base.ClientResult;
import technology.semi.weaviate.client.base.Response;
import technology.semi.weaviate.client.base.Result;
import technology.semi.weaviate.client.v1.data.model.SingleRef;

public class ReferenceCreator extends BaseClient<Object> implements ClientResult<Boolean> {
  private String id;
  private String referenceProperty;
  private SingleRef referencePayload;

  public ReferenceCreator(Config config) {
    super(config);
  }

  public ReferenceCreator withID(String id) {
    this.id = id;
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
    String path = String.format("/objects/%s/references/%s", this.id, this.referenceProperty);
    Response<Object> resp = sendPostRequest(path, this.referencePayload, Object.class);
    return new Result<>(resp.getStatusCode(), resp.getStatusCode() == 200, resp.getErrors());
  }
}
