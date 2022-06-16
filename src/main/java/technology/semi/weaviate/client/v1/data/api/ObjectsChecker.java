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
import technology.semi.weaviate.client.v1.data.util.ObjectsPathBuilder;

public class ObjectsChecker extends BaseClient<String> implements ClientResult<Boolean> {

  private final String version;
  private String id;
  private String className;

  public ObjectsChecker(Config config, String version) {
    super(config);
    this.version = version;
  }

  public ObjectsChecker withID(String id) {
    this.id = id;
    return this;
  }

  public ObjectsChecker withClassName(String className) {
    this.className = className;
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
    String path = getPath(this.id, this.className);
    Response<String> resp = sendHeadRequest(path, String.class);
    return new Result<>(resp.getStatusCode(), resp.getStatusCode() == 204, resp.getErrors());
  }

  private String getPath(String id, String className) {
    return ObjectsPathBuilder.builder().id(id).className(className).build().buildPath(this.version);
  }
}