package technology.semi.weaviate.client.v1.data.api;

import java.util.Map;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import technology.semi.weaviate.client.Config;
import technology.semi.weaviate.client.base.BaseClient;
import technology.semi.weaviate.client.base.ClientResult;
import technology.semi.weaviate.client.base.Response;
import technology.semi.weaviate.client.base.Result;
import technology.semi.weaviate.client.v1.data.model.WeaviateObject;

public class ObjectCreator extends BaseClient<WeaviateObject> implements ClientResult<WeaviateObject> {

  private String uuid;
  private String className;
  private Map<String, Object> properties;

  public ObjectCreator(Config config) {
    super(config);
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

  private String getID() {
    if (StringUtils.isEmpty(uuid)) {
      return UUID.randomUUID().toString();
    }
    return uuid;
  }

  @Override
  public Result<WeaviateObject> run() {
    WeaviateObject obj = WeaviateObject.builder()
            .className(className)
            .properties(properties)
            .id(getID())
            .build();
    Response<WeaviateObject> resp = sendPostRequest("/objects", obj, WeaviateObject.class);
    return new Result<>(resp);
  }
}
