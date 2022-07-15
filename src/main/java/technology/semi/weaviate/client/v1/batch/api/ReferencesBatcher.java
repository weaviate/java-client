package technology.semi.weaviate.client.v1.batch.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import technology.semi.weaviate.client.Config;
import technology.semi.weaviate.client.base.BaseClient;
import technology.semi.weaviate.client.base.ClientResult;
import technology.semi.weaviate.client.base.Response;
import technology.semi.weaviate.client.base.Result;
import technology.semi.weaviate.client.v1.batch.model.BatchReference;
import technology.semi.weaviate.client.v1.batch.model.BatchReferenceResponse;

public class ReferencesBatcher extends BaseClient<BatchReferenceResponse[]> implements ClientResult<BatchReferenceResponse[]> {

  private final List<BatchReference> references;

  public ReferencesBatcher(Config config) {
    super(config);
    this.references = new ArrayList<>();
  }

  public ReferencesBatcher withReference(BatchReference reference) {
    return this.withReferences(reference);
  }

  public ReferencesBatcher withReferences(BatchReference... references) {
    this.references.addAll(Arrays.asList(references));
    return this;
  }

  @Override
  public Result<BatchReferenceResponse[]> run() {
    BatchReference[] payload = references.toArray(new BatchReference[0]);
    Response<BatchReferenceResponse[]> resp = sendPostRequest("/batch/references", payload, BatchReferenceResponse[].class);
    return new Result<>(resp);
  }
}
