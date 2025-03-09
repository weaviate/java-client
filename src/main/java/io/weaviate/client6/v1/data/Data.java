package io.weaviate.client6.v1.data;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Optional;
import java.util.function.Consumer;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import io.weaviate.client6.Config;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class Data<T> {
  // TODO: inject singleton as dependency
  private static final Gson gson = new Gson();

  // TODO: this should be wrapped around in some TypeInspector etc.
  private final String collectionName;

  // TODO: hide befind an internal HttpClient
  private final Config config;

  public WeaviateObject<T> insert(T object, Consumer<CustomMetadata> metadata) throws IOException {
    var body = new WeaviateObject<>(collectionName, object, metadata).toRequestObject();
    try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
      ClassicHttpRequest httpPost = ClassicRequestBuilder
          .post(config.baseUrl() + "/objects")
          .setEntity(gson.toJson(body), ContentType.APPLICATION_JSON)
          .build();

      return httpclient.execute(httpPost, response -> {
        var entity = response.getEntity();
        if (response.getCode() != HttpStatus.SC_SUCCESS) { // Does not return 201
          var message = EntityUtils.toString(entity);
          throw new RuntimeException("HTTP " + response.getCode() + ": " + message);
        }

        try (var r = new InputStreamReader(entity.getContent())) {
          WeaviateObject.RequestObject<T> inserted = gson.fromJson(r,
              new TypeToken<WeaviateObject.RequestObject<T>>() {
              }.getType());
          return inserted.toWeaviateObject();
        }
      });
    }
  }

  public Optional<WeaviateObject<T>> get(String id) throws IOException {
    try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
      ClassicHttpRequest httpGet = ClassicRequestBuilder
          .get(config.baseUrl() + "/objects/" + collectionName + "/" + id + "?include=vector")
          .build();

      return httpclient.execute(httpGet, response -> {
        if (response.getCode() == HttpStatus.SC_NOT_FOUND) {
          return Optional.empty();
        }
        var json = EntityUtils.toString(response.getEntity());
        WeaviateObject.RequestObject<T> object = gson.fromJson(json,
            new TypeToken<WeaviateObject.RequestObject<T>>() {
            }.getType());
        if (object == null) {
          return Optional.empty();
        }
        return Optional.of(object.toWeaviateObject());
        // try (var r = new InputStreamReader(response.getEntity().getContent())) {
        // WeaviateObject.RequestObject<T> object = gson.fromJson(r,
        // new TypeToken<WeaviateObject.RequestObject<T>>() {
        // }.getType());
        // if (object == null) {
        // return Optional.empty();
        // }
        // return Optional.of(object.toWeaviateObject());
        // }
      });
    }
  }

  public void delete(String id) throws IOException {
    try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
      ClassicHttpRequest httpGet = ClassicRequestBuilder
          .delete(config.baseUrl() + "/objects/" + collectionName + "/" + id)
          .build();

      httpclient.execute(httpGet, response -> {
        if (response.getCode() != HttpStatus.SC_NO_CONTENT) {
          throw new RuntimeException(EntityUtils.toString(response.getEntity()));
        }
        return null;
      });
    }
  }
}
