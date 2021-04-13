package technology.semi.weaviate.client.v1.batch.api;

import java.util.ArrayList;
import java.util.List;
import technology.semi.weaviate.client.Config;
import technology.semi.weaviate.client.base.BaseClient;
import technology.semi.weaviate.client.base.ClientResult;
import technology.semi.weaviate.client.base.Response;
import technology.semi.weaviate.client.base.Result;
import technology.semi.weaviate.client.v1.batch.model.ObjectGetResponse;
import technology.semi.weaviate.client.v1.batch.model.ObjectsBatchRequestBody;
import technology.semi.weaviate.client.v1.data.model.Object;

public class ObjectsBatcher extends BaseClient<ObjectGetResponse[]> implements ClientResult<ObjectGetResponse[]> {

  private List<Object> objects;

  public ObjectsBatcher(Config config) {
    super(config);
    this.objects = new ArrayList<>();
  }

  public ObjectsBatcher withObject(Object object) {
    this.objects.add(object);
    return this;
  }

  @Override
  public Result<ObjectGetResponse[]> run() {
    ObjectsBatchRequestBody batchRequest = ObjectsBatchRequestBody.builder()
            .objects(objects.stream().toArray(Object[]::new))
            .fields(new String[]{"ALL"})
            .build();
    Response<ObjectGetResponse[]> resp = sendPostRequest("/batch/objects", batchRequest, ObjectGetResponse[].class);
    return new Result<>(resp);
  }
}
