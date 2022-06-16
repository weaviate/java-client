package technology.semi.weaviate.client.v1.data.api;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import technology.semi.weaviate.client.Config;
import technology.semi.weaviate.client.base.BaseClient;
import technology.semi.weaviate.client.base.ClientResult;
import technology.semi.weaviate.client.base.Response;
import technology.semi.weaviate.client.base.Result;
import technology.semi.weaviate.client.base.WeaviateErrorMessage;
import technology.semi.weaviate.client.base.WeaviateErrorResponse;
import technology.semi.weaviate.client.v1.data.model.WeaviateObject;
import technology.semi.weaviate.client.v1.data.util.ObjectsPathBuilder;

public class ObjectUpdater extends BaseClient<WeaviateObject> implements ClientResult<Boolean> {

  private final String version;
  private String id;
  private String className;
  private Map<String, Object> properties;
  private Boolean withMerge;

  public ObjectUpdater(Config config, String version) {
    super(config);
    this.version = version;
  }

  public ObjectUpdater withClassName(String className) {
    this.className = className;
    return this;
  }

  public ObjectUpdater withID(String id) {
    this.id = id;
    return this;
  }

  public ObjectUpdater withProperties(Map<String, Object> properties) {
    this.properties = properties;
    return this;
  }

  public ObjectUpdater withMerge() {
    this.withMerge = true;
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
    WeaviateObject obj = WeaviateObject.builder()
            .className(className)
            .properties(properties)
            .id(id)
            .build();
    String path = getPath(id, className);
    if (BooleanUtils.isTrue(withMerge)) {
      Response<WeaviateObject> resp = sendPatchRequest(path, obj, WeaviateObject.class);
      return new Result<>(resp.getStatusCode(), resp.getStatusCode() == 204, resp.getErrors());
    }
    Response<WeaviateObject> resp = sendPutRequest(path, obj, WeaviateObject.class);
    return new Result<>(resp.getStatusCode(), resp.getStatusCode() == 200, resp.getErrors());
  }

  private String getPath(String id, String className) {
    return ObjectsPathBuilder.builder().id(id).className(className).build().buildPath(this.version);
  }
}