package technology.semi.weaviate.client.v1.schema.api;

import org.apache.commons.lang3.StringUtils;
import technology.semi.weaviate.client.Config;
import technology.semi.weaviate.client.base.BaseClient;
import technology.semi.weaviate.client.base.Client;
import technology.semi.weaviate.client.base.Response;
import technology.semi.weaviate.client.v1.schema.model.Property;

public class PropertyCreator extends BaseClient<Property> implements Client<Boolean> {

  private String className;
  private Property property;

  public PropertyCreator(Config config) {
    super(config);
  }

  public PropertyCreator withClassName(String className) {
    this.className = className;
    return this;
  }

  public PropertyCreator withProperty(Property property) {
    this.property = property;
    return this;
  }

  @Override
  public Boolean run() {
    if (StringUtils.isEmpty(this.className)) {
      return false;
    }
    String path = String.format("/schema/%s/properties", this.className);
    Response<Property> resp = sendPostRequest(path, property, Property.class);
    return resp.getStatusCode() == 200;
  }
}