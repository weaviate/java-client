package io.weaviate.client.v1.graphql.model;

import com.google.gson.annotations.SerializedName;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@Getter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GraphQLTypedResponse<T> {
  Operation<T> data;
  GraphQLError[] errors;

  @Getter
  @ToString
  @EqualsAndHashCode
  @AllArgsConstructor
  @FieldDefaults(level = AccessLevel.PRIVATE)
  public static class Operation<T> {
    @SerializedName(value = "Get", alternate = {"Aggregate", "Explore"})
    private T objects;
  }
}


