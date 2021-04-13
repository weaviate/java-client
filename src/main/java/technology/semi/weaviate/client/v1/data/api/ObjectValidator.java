package technology.semi.weaviate.client.v1.data.api;

import java.util.Map;
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
import technology.semi.weaviate.client.v1.data.model.Object;

public class ObjectValidator extends BaseClient<Object> implements ClientResult<Boolean> {

  private String id;
  private String className;
  private Map<String, java.lang.Object> properties;

  public ObjectValidator(Config config) {
    super(config);
  }

  public ObjectValidator withID(String id) {
    this.id = id;
    return this;
  }

  public ObjectValidator withClassName(String className) {
    this.className = className;
    return this;
  }

  public ObjectValidator withSchema(Map<String, java.lang.Object> properties) {
    this.properties = properties;
    return this;
  }

  @Override
  public Result<Boolean> run() {
    if (StringUtils.isEmpty(id)) {
      WeaviateErrorMessage errorMessage = WeaviateErrorMessage.builder()
              .message("id cannot be empty").build();
      WeaviateErrorResponse errors = WeaviateErrorResponse.builder()
              .error(Stream.of(errorMessage).collect(Collectors.toList())).build();
      return new Result<>(500, false, errors);
    }
    Object obj = Object.builder()
            .className(className)
            .properties(properties)
            .id(id)
            .build();
    Response<Object> resp = sendPostRequest("/objects/validate", obj, Object.class);
    return new Result<>(resp.getStatusCode(), resp.getStatusCode() == 200, resp.getErrors());
  }
}