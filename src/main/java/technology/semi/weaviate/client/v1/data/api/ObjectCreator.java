package technology.semi.weaviate.client.v1.data.api;

import java.util.Map;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import technology.semi.weaviate.client.Config;
import technology.semi.weaviate.client.base.BaseClient;
import technology.semi.weaviate.client.base.ClientResult;
import technology.semi.weaviate.client.base.Response;
import technology.semi.weaviate.client.base.Result;
import technology.semi.weaviate.client.v1.data.model.Object;

public class ObjectCreator extends BaseClient<Object> implements ClientResult<Object> {

  private String uuid;
  private String className;
  private Map<String, java.lang.Object> properties;

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

  public ObjectCreator withProperties(Map<String, java.lang.Object> properties) {
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
  public Result<Object> run() {
    Object obj = Object.builder()
            .className(className)
            .properties(properties)
            .id(getID())
            .build();
    Response<Object> resp = sendPostRequest("/objects", obj, Object.class);
    return new Result<>(resp);
  }
}
