package technology.semi.weaviate.client.v1.data.api;

import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import technology.semi.weaviate.client.Config;
import technology.semi.weaviate.client.base.BaseClient;
import technology.semi.weaviate.client.base.Client;
import technology.semi.weaviate.client.base.Response;
import technology.semi.weaviate.client.v1.data.model.Object;

public class ObjectUpdater extends BaseClient<Object> implements Client<Boolean> {

  private String id;
  private String className;
  private Map<String, java.lang.Object> properties;
  private Boolean withMerge;

  public ObjectUpdater(Config config) {
    super(config);
  }

  public ObjectUpdater withClassName(String className) {
    this.className = className;
    return this;
  }

  public ObjectUpdater withID(String id) {
    this.id = id;
    return this;
  }

  public ObjectUpdater withProperties(Map<String, java.lang.Object> properties) {
    this.properties = properties;
    return this;
  }

  public ObjectUpdater withMerge() {
    this.withMerge = true;
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
    String path = String.format("/objects/%s", id);
    if (withMerge != null && withMerge) {
      Response<Object> resp = sendPatchRequest(path, obj, Object.class);
      return resp.getStatusCode() == 204;
    }
    Response<Object> resp = sendPutRequest(path, obj, Object.class);
    return resp.getStatusCode() == 200;
  }
}