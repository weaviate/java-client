package technology.semi.weaviate.client.v1.data.api;

import java.util.Objects;
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
import technology.semi.weaviate.client.v1.data.util.ObjectsPath;

public class ObjectsChecker extends BaseClient<String> implements ClientResult<Boolean> {

  private final ObjectsPath objectsPath;
  private String id;
  private String className;

  public ObjectsChecker(Config config, ObjectsPath objectsPath) {
    super(config);
    this.objectsPath = Objects.requireNonNull(objectsPath);
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
    String path = objectsPath.buildCheck(ObjectsPath.Params.builder()
            .id(id)
            .className(className)
            .build());
    Response<String> resp = sendHeadRequest(path, String.class);
    return new Result<>(resp.getStatusCode(), resp.getStatusCode() == 204, resp.getErrors());
  }
}