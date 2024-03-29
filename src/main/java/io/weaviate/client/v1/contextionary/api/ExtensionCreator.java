package io.weaviate.client.v1.contextionary.api;

import java.util.stream.Collectors;
import java.util.stream.Stream;
import io.weaviate.client.Config;
import io.weaviate.client.base.BaseClient;
import io.weaviate.client.base.ClientResult;
import io.weaviate.client.base.Response;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.WeaviateErrorMessage;
import io.weaviate.client.base.WeaviateErrorResponse;
import io.weaviate.client.base.http.HttpClient;
import io.weaviate.client.v1.contextionary.model.C11yExtension;

public class ExtensionCreator extends BaseClient<Object> implements ClientResult<Boolean> {

  private C11yExtension.C11yExtensionBuilder extension;

  public ExtensionCreator(HttpClient httpClient, Config config) {
    super(httpClient, config);
    this.extension = C11yExtension.builder().weight(1.0f);
  }

  public ExtensionCreator withConcept(String concept) {
    this.extension.concept(concept);
    return this;
  }

  public ExtensionCreator withDefinition(String definition) {
    this.extension.definition(definition);
    return this;
  }

  public ExtensionCreator withWeight(Float weight) {
    this.extension.weight(weight);
    return this;
  }

  @Override
  public Result<Boolean> run() {
    C11yExtension extension = this.extension.build();
    if (extension.getWeight() > 1 || extension.getWeight() < 0) {
      WeaviateErrorMessage errorMessage = WeaviateErrorMessage.builder()
              .message("weight has to be between 0 and 1")
              .build();
      WeaviateErrorResponse errors = WeaviateErrorResponse.builder()
              .error(Stream.of(errorMessage).collect(Collectors.toList()))
              .build();
      return new Result<>(500, false, errors);
    }
    Response<Object> resp = sendPostRequest("/modules/text2vec-contextionary/extensions", extension, Object.class);
    return new Result<>(resp.getStatusCode(), resp.getStatusCode() == 200, resp.getErrors());
  }
}
