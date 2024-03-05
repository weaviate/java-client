package io.weaviate.client.base;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import io.weaviate.client.v1.graphql.model.GraphQLTypedResponse;
import java.lang.reflect.Type;

public class Serializer {
  private Gson gson;

  public Serializer() {
    this.gson = new GsonBuilder().disableHtmlEscaping().create();
  }

  public <C> GraphQLTypedResponse<C> toGraphQLTypedResponse(String response, Class<C> classOfT) {
    return gson.fromJson(response,
      TypeToken.getParameterized(GraphQLTypedResponse.class, classOfT).getType());
  }

  public <C> C toResponse(String response, Type typeOfT) {
    return gson.fromJson(response, typeOfT);
  }

  public <T> T toResponse(String response, Class<T> classOfT) {
    return gson.fromJson(response, classOfT);
  }

  public String toJsonString(Object object) {
    return (object != null) ? gson.toJson(object) : null;
  }

  public <T> Result<T> toResult(int statusCode, String body, Class<T> classOfT) {
    if (statusCode < 399) {
      return new Result<>(toResponse(statusCode, body, classOfT));
    }
    return new Result<>(statusCode, null, toWeaviateError(body));
  }

  public <T> Response<T> toResponse(int statusCode, String body, Class<T> classOfT) {
    if (statusCode < 399) {
      T obj = toResponse(body, classOfT);
      return new Response<>(statusCode, obj, null);
    }
    return new Response<>(statusCode, null, toWeaviateError(body));
  }

  public <C> Response<GraphQLTypedResponse<C>> toGraphQLTypedResponse(int statusCode, String body, Class<C> classOfC) {
    if (statusCode < 399) {
      GraphQLTypedResponse<C> obj = toGraphQLTypedResponse(body, classOfC);
      return new Response<>(statusCode, obj, null);
    }
    return new Response<>(statusCode, null, toWeaviateError(body));
  }

  public <C> Result<GraphQLTypedResponse<C>> toGraphQLTypedResult(int statusCode, String body, Class<C> classOfC) {
    if (statusCode < 399) {
      return new Result<>(toGraphQLTypedResponse(statusCode, body, classOfC));
    }
    return new Result<>(statusCode, null, toWeaviateError(body));
  }

  public WeaviateErrorResponse toWeaviateError(String body) {
    return toResponse(body, WeaviateErrorResponse.class);
  }
}
