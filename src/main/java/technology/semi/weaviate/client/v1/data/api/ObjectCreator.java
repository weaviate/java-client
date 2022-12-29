package technology.semi.weaviate.client.v1.data.api;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import technology.semi.weaviate.client.Config;
import technology.semi.weaviate.client.base.BaseClient;
import technology.semi.weaviate.client.base.ClientResult;
import technology.semi.weaviate.client.base.Response;
import technology.semi.weaviate.client.base.Result;
import technology.semi.weaviate.client.v1.data.model.WeaviateObject;
import technology.semi.weaviate.client.v1.data.util.ObjectsPath;

public class ObjectCreator extends BaseClient<WeaviateObject> implements ClientResult<WeaviateObject> {

  private final ObjectsPath objectsPath;
  private String uuid;
  private String className;
  private Map<String, Object> properties;
  private Float[] vector;

  public ObjectCreator(Config config, ObjectsPath objectsPath) {
    super(config);
    this.objectsPath = Objects.requireNonNull(objectsPath);
  }

  public ObjectCreator withClassName(String className) {
    this.className = className;
    return this;
  }

  public ObjectCreator withID(String uuid) {
    this.uuid = uuid;
    return this;
  }

  public ObjectCreator withProperties(Map<String, Object> properties) {
    this.properties = properties;
    return this;
  }

  public ObjectCreator withVector(Float[] vector) {
    this.vector = vector;
    return this;
  }

  private String getID() {
    if (StringUtils.isEmpty(uuid)) {
      return UUID.randomUUID().toString();
    }
    return uuid;
  }

  @Override
  public Result<WeaviateObject> run() {
    String path = objectsPath.buildCreate(ObjectsPath.Params.builder().build());
    WeaviateObject obj = WeaviateObject.builder()
      .className(className)
      .properties(properties)
      .vector(vector)
      .id(getID())
      .build();
    Response<WeaviateObject> resp = sendPostRequest(path, obj, WeaviateObject.class);
    return new Result<>(resp);
  }
}
