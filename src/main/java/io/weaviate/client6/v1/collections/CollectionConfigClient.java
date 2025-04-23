package io.weaviate.client6.v1.collections;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Optional;

import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;

import com.google.gson.Gson;

import io.weaviate.client6.Config;
import io.weaviate.client6.internal.DtoTypeAdapterFactory;
import io.weaviate.client6.internal.HttpClient;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class CollectionConfigClient {
  // TODO: hide befind an internal HttpClient
  private final String collectionName;
  private final Config config;
  private final HttpClient httpClient;

  private static final Gson gson = new Gson();
  static {
    DtoTypeAdapterFactory.register(
        Collection.class,
        CollectionDefinitionDTO.class,
        m -> {
          return new CollectionDefinitionDTO(m);
        });
  }

  public Optional<Collection> get() throws IOException {
    ClassicHttpRequest httpGet = ClassicRequestBuilder
        .get(config.baseUrl() + "/schema/" + collectionName)
        .build();

    return httpClient.http.execute(httpGet, response -> {
      if (response.getCode() == HttpStatus.SC_NOT_FOUND) {
        return Optional.empty();
      }
      try (var r = new InputStreamReader(response.getEntity().getContent())) {
        var collection = gson.fromJson(r, Collection.class);
        return Optional.ofNullable(collection);
      }
    });
  }

  public void addProperty(Property property) throws IOException {
    ClassicHttpRequest httpPost = ClassicRequestBuilder
        .post(config.baseUrl() + "/schema/" + collectionName + "/properties")
        .setEntity(gson.toJson(property), ContentType.APPLICATION_JSON)
        .build();

    httpClient.http.execute(httpPost, response -> {
      var entity = response.getEntity();
      if (response.getCode() != HttpStatus.SC_SUCCESS) {
        var message = EntityUtils.toString(entity);
        throw new RuntimeException("HTTP " + response.getCode() + ": " + message);
      }
      return null;
    });
  }
}
