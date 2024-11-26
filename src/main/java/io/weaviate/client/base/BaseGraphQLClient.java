package io.weaviate.client.base;

import io.weaviate.client.Config;
import io.weaviate.client.base.http.HttpClient;
import io.weaviate.client.base.http.HttpResponse;
import io.weaviate.client.v1.graphql.model.GraphQLTypedResponse;

public abstract class BaseGraphQLClient<T> extends BaseClient<T> {
  public BaseGraphQLClient(HttpClient client, Config config) {
    super(client, config);
  }

  private <C> GraphQLTypedResponse<C> toResponseTyped(String response, Class<C> classOfC) {
    return serializer.toGraphQLTypedResponse(response, classOfC);
  }

  protected <C> Response<GraphQLTypedResponse<C>> sendGraphQLTypedRequest(Object payload, Class<C> classOfC) {
    try {
      HttpResponse response = this.sendHttpRequest("/graphql", payload, "POST");
      int statusCode = response.getStatusCode();
      String responseBody = response.getBody();

      if (statusCode < 399) {
        GraphQLTypedResponse<C> body = toResponseTyped(responseBody, classOfC);
        return new Response<>(statusCode, body, null);
      }

      WeaviateErrorResponse error = toResponse(responseBody, WeaviateErrorResponse.class);
      return new Response<>(statusCode, null, error);
    } catch (Exception e) {
      WeaviateErrorResponse errors = getWeaviateErrorResponse(e);
      return new Response<>(0, null, errors);
    }
  }
}
