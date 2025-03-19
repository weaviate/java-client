package io.weaviate.client6.v1.data;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Consumer;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;

import com.google.gson.Gson;

import io.weaviate.client6.Config;
import io.weaviate.client6.internal.HttpClient;
import io.weaviate.client6.v1.ObjectMetadata;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class Data<T> {
  // TODO: inject singleton as dependency
  private static final Gson gson = new Gson();

  // TODO: this should be wrapped around in some TypeInspector etc.
  private final String collectionName;

  // TODO: hide befind an internal HttpClient
  private final Config config;
  private final HttpClient httpClient;

  public WeaviateObject<T> insert(T object) throws IOException {
    return insert(object, opt -> {
    });
  }

  public WeaviateObject<T> insert(T object, Consumer<ObjectMetadata.Builder> options) throws IOException {
    var body = new WeaviateObject<>(collectionName, object, options);
    ClassicHttpRequest httpPost = ClassicRequestBuilder
        .post(config.baseUrl() + "/objects")
        .setEntity(body.toJson(gson), ContentType.APPLICATION_JSON)
        .build();

    return httpClient.http.execute(httpPost, response -> {
      var entity = response.getEntity();
      if (response.getCode() != HttpStatus.SC_SUCCESS) { // Does not return 201
        var message = EntityUtils.toString(entity);
        throw new RuntimeException("HTTP " + response.getCode() + ": " + message);
      }

      return WeaviateObject.fromJson(gson, entity.getContent());
    });
  }

  public Optional<WeaviateObject<T>> get(String id) throws IOException {
    return get(id, q -> {
    });
  }

  public Optional<WeaviateObject<T>> get(String id, Consumer<GetParameters> query) throws IOException {
    try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
      ClassicHttpRequest httpGet = ClassicRequestBuilder
          .get(config.baseUrl() + "/objects/" + collectionName + "/" + id + QueryParameters.encodeGet(query))
          .build();

      return httpClient.http.execute(httpGet, response -> {
        if (response.getCode() == HttpStatus.SC_NOT_FOUND) {
          return Optional.empty();
        }

        WeaviateObject<T> object = WeaviateObject.fromJson(
            gson, response.getEntity().getContent());
        return Optional.ofNullable(object);
      });
    }
  }

  public void delete(String id) throws IOException {
    try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
      ClassicHttpRequest httpGet = ClassicRequestBuilder
          .delete(config.baseUrl() + "/objects/" + collectionName + "/" + id)
          .build();

      httpClient.http.execute(httpGet, response -> {
        if (response.getCode() != HttpStatus.SC_NO_CONTENT) {
          throw new RuntimeException(EntityUtils.toString(response.getEntity()));
        }
        return null;
      });
    }
  }
}
