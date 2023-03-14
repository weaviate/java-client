package io.weaviate.client.v1.schema.api;

import io.weaviate.client.v1.schema.model.Property;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import io.weaviate.client.Config;
import io.weaviate.client.base.BaseClient;
import io.weaviate.client.base.ClientResult;
import io.weaviate.client.base.Response;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.WeaviateErrorMessage;
import io.weaviate.client.base.WeaviateErrorResponse;
import io.weaviate.client.base.http.HttpClient;

public class PropertyCreator extends BaseClient<Property> implements ClientResult<Boolean> {

  private String className;
  private Property property;

  public PropertyCreator(HttpClient httpClient, Config config) {
    super(httpClient, config);
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
  public Result<Boolean> run() {
    if (StringUtils.isEmpty(this.className)) {
      WeaviateErrorMessage errorMessage = WeaviateErrorMessage.builder()
              .message("classname cannot be empty").build();
      WeaviateErrorResponse errors = WeaviateErrorResponse.builder()
              .error(Stream.of(errorMessage).collect(Collectors.toList())).build();
      return new Result<>(500, false, errors);
    }
    String path = String.format("/schema/%s/properties", this.className);
    Response<Property> resp = sendPostRequest(path, property, Property.class);
    return new Result<>(resp.getStatusCode(), resp.getStatusCode() == 200, resp.getErrors());
  }
}
