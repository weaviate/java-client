package io.weaviate.client6.v1;

import java.io.IOException;
import java.util.Map;
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
import io.weaviate.client6.v1.collections.CollectionDefinition;
import io.weaviate.client6.v1.collections.dto.CollectionDefinitionDTO;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class Collections {
  // TODO: hide befind an internal HttpClient
  private final Config config;

  private static final Gson gson = new Gson();

  public void create(String name, Consumer<CollectionDefinition.Configuration> options) throws IOException {
    var collection = new CollectionDefinition(name, options);
    var body = new CollectionDefinitionDTO(collection);

    try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
      String jsonBody = gson.toJson(body);
      ClassicHttpRequest httpPost = ClassicRequestBuilder
          .post(config.baseUrl() + "/schema")
          .setEntity(jsonBody, ContentType.APPLICATION_JSON)
          .build();

      httpclient.execute(httpPost, response -> {
        var entity = response.getEntity();
        if (response.getCode() != HttpStatus.SC_SUCCESS) { // Does not return 201
          var message = EntityUtils.toString(entity);
          throw new RuntimeException("HTTP " + response.getCode() + ": " + message);
        }
        return null;
      });
    }
  }

  public Collection<Map<String, Object>> use(String name) {
    return new Collection<>(config, name);
  }
}
