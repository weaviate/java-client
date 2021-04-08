package technology.semi.weaviate.client.v1.batch.api;

import java.util.ArrayList;
import java.util.List;
import technology.semi.weaviate.client.Config;
import technology.semi.weaviate.client.base.BaseClient;
import technology.semi.weaviate.client.base.Client;
import technology.semi.weaviate.client.base.Response;
import technology.semi.weaviate.client.v1.batch.model.BatchReference;
import technology.semi.weaviate.client.v1.batch.model.BatchReferenceResponse;

public class ReferencesBatcher extends BaseClient<BatchReferenceResponse[]> implements Client<BatchReferenceResponse[]> {

  private List<BatchReference> references;

  public ReferencesBatcher(Config config) {
    super(config);
    this.references = new ArrayList<>();
  }

  public ReferencesBatcher withReference(BatchReference reference) {
    this.references.add(reference);
    return this;
  }

  @Override
  public BatchReferenceResponse[] run() {
    BatchReference[] payload = references.stream().toArray(BatchReference[]::new);
    Response<BatchReferenceResponse[]> resp = sendPostRequest("/batch/references", payload, BatchReferenceResponse[].class);
    if (resp.getStatusCode() == 200) {
      return resp.getBody();
    }
    return null;
  }
}
