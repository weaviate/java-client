package technology.semi.weaviate.client.v1.data.api;

import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import technology.semi.weaviate.client.Config;
import technology.semi.weaviate.client.base.BaseClient;
import technology.semi.weaviate.client.base.ClientResult;
import technology.semi.weaviate.client.base.Response;
import technology.semi.weaviate.client.base.Result;
import technology.semi.weaviate.client.base.WeaviateErrorMessage;
import technology.semi.weaviate.client.base.WeaviateErrorResponse;

public class ObjectDeleter extends BaseClient<String> implements ClientResult<Boolean> {

  private String id;

  public ObjectDeleter(Config config) {
    super(config);
  }

  public ObjectDeleter withID(String id) {
    this.id = id;
    return this;
  }

  @Override
  public Result<Boolean> run() {
    if (StringUtils.isEmpty(this.id)) {
      WeaviateErrorMessage errorMessage = WeaviateErrorMessage.builder()
              .message("id cannot be empty").build();
      WeaviateErrorResponse errors = WeaviateErrorResponse.builder()
              .error(Stream.of(errorMessage).collect(Collectors.toList())).build();
      return new Result<>(500, false, errors);
    }
    String path = String.format("/objects/%s", this.id);
    Response<String> resp = sendDeleteRequest(path, String.class);
    return new Result<>(resp.getStatusCode(), resp.getStatusCode() == 204, resp.getErrors());
  }
}