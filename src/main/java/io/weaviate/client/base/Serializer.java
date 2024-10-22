package io.weaviate.client.base;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Serializer {
  private Gson gson;

  public Serializer() {
    this.gson = new GsonBuilder().disableHtmlEscaping().create();
  }

  public <T> T toObject(String response, Class<T> classOfT) {
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
      T obj = toObject(body, classOfT);
      return new Response<>(statusCode, obj, null);
    }
    return new Response<>(statusCode, null, toWeaviateError(body));
  }

  public WeaviateErrorResponse toWeaviateError(String body) {
    return toObject(body, WeaviateErrorResponse.class);
  }
}
