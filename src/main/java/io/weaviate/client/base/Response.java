package io.weaviate.client.base;

import io.weaviate.client.v1.graphql.model.GraphQLResponse;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class Response<T> {
  int statusCode;
  T body;
  WeaviateErrorResponse errors;

  public Response(int statusCode, T body, WeaviateErrorResponse errors) {
    this.statusCode = statusCode;
    this.body = body;
    if (body instanceof GraphQLResponse) {
      this.errors = getWeaviateGraphQLErrorResponse((GraphQLResponse) body, statusCode);;
    } else {
      this.errors = errors;
    }
  }

  /**
   * Extract errors from {@link WeaviateErrorResponse} from a GraphQL response body.
   *
   * @param gql  GraphQL response body.
   * @param code HTTP status code to pass in the {@link WeaviateErrorResponse}.
   * @return Error response to be returned to the caller.
   */
  private WeaviateErrorResponse getWeaviateGraphQLErrorResponse(GraphQLResponse gql, int code) {
    List<WeaviateErrorMessage> messages = gql.errorMessages();
    if (messages == null || messages.isEmpty()) {
      return null;
    }
    return WeaviateErrorResponse.builder().code(code).error(gql.errorMessages()).build();
  }
}
