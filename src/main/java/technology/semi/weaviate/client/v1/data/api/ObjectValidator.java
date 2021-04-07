package technology.semi.weaviate.client.v1.data.api;

import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import technology.semi.weaviate.client.Config;
import technology.semi.weaviate.client.base.BaseClient;
import technology.semi.weaviate.client.base.Client;
import technology.semi.weaviate.client.base.Response;
import technology.semi.weaviate.client.v1.data.model.Object;

public class ObjectValidator extends BaseClient<Object> implements Client<Boolean> {

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
  public Boolean run() {
    if (StringUtils.isEmpty(id)) {
      return false;
    }
    Object obj = Object.builder()
            .className(className)
            .properties(properties)
            .id(id)
            .build();
    Response<Object> resp = sendPostRequest("/objects/validate", obj, Object.class);
    return resp.getStatusCode() == 200;
  }
}