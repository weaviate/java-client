package technology.semi.weaviate.client.v1.schema.api;

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

public class ClassDeleter extends BaseClient<String> implements ClientResult<Boolean> {

  private String className;

  public ClassDeleter(Config config) {
    super(config);
  }

  public ClassDeleter withClassName(String className) {
    this.className = className;
    return this;
  }

  @Override
  public Result<Boolean> run() {
    if (StringUtils.isEmpty(this.className)) {
      WeaviateErrorMessage errorMessage = WeaviateErrorMessage.builder()
        .message("classname cannot be empty").build();
      WeaviateErrorResponse errors = WeaviateErrorResponse.builder()
        .error(Stream.of(errorMessage).collect(Collectors.toList())).build();
      return new Result<>(500, false, errors);
    }
    String path = String.format("/schema/%s", this.className);
    Response<String> resp = sendDeleteRequest(path, null, String.class);
    return new Result<>(resp.getStatusCode(), resp.getStatusCode() == 200, resp.getErrors());
  }
}
