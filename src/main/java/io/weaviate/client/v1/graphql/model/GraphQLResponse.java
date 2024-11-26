package io.weaviate.client.v1.graphql.model;

import io.weaviate.client.base.WeaviateErrorMessage;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@ToString
@EqualsAndHashCode
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GraphQLResponse<T> {
  T data;
  GraphQLError[] errors;


  /**
   * Extract the 'message' portion of every error in the response, omitting 'path' and 'location'.
   *
   * @return Non-throwable WeaviateErrorMessages
   */
  public List<WeaviateErrorMessage> errorMessages() {
    if (errors == null || errors.length == 0) {
      return null;
    }
    return Arrays.stream(errors)
      .map(err -> new WeaviateErrorMessage(err.getMessage(), null))
      .collect(Collectors.toList());
  }
}
