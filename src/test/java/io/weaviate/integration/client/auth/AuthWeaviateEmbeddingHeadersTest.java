package io.weaviate.integration.client.auth;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import io.weaviate.client.Config;
import io.weaviate.client.WeaviateAuthClient;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.batch.api.ObjectsBatcher;
import io.weaviate.client.v1.data.model.WeaviateObject;
import io.weaviate.client.v1.schema.model.WeaviateClass;

/**
 * Local test for authentication to Weaviate Embeddings service.
 * To run it, remove {@code @Ignore} annotation below and set these
 * environment variables:
 *
 * <pre>{@code
 *  export WEAVIATE_REST_HOST=""
 *  export WEAVIATE_GRPC_HOST=""
 *  export WEAVIATE_API_KEY=""
 * }</pre>
 */
@Ignore
public class AuthWeaviateEmbeddingHeadersTest {

  private static final String apiKey = System.getenv("WEAVIATE_API_KEY");
  private static final String REST_HOST = System.getenv("WEAVIATE_REST_HOST");
  private static final String GRPC_HOST = System.getenv("WEAVIATE_GRPC_HOST");
  private static final String EMBEDDING_MODEL = "Snowflake/snowflake-arctic-embed-l-v2.0";
  private static final String DEMO_COLLECTION = "DemoCollection";

  private WeaviateClient client;

  @Before
  public void beforeEach() {
    Config config = new Config("https", REST_HOST, true, GRPC_HOST);
    client = assertDoesNotThrow(() -> WeaviateAuthClient.apiKey(config, apiKey), "create auth client");

    // Delete DemoCollection
    client.schema().classDeleter().withClassName(DEMO_COLLECTION).run();
  }

  /**
   * Following this guide:
   * https://weaviate.io/developers/wcs/embeddings/quickstart#requirements we
   * expect that there will be no errors.
   */
  @Test
  public void testWeaviateHeaders() {
    createCollection(client);
    assertDoesNotThrow(() -> importObjects(client));
  }

  private void createCollection(WeaviateClient client) {
    Map<String, Object> text2vecWeaviate = new HashMap<>();
    Map<String, Object> text2vecWeaviateSettings = new HashMap<>();

    text2vecWeaviateSettings.put("properties", new String[] { "title" });
    text2vecWeaviateSettings.put("model", new String[] { EMBEDDING_MODEL });
    text2vecWeaviateSettings.put("dimensions", new Integer[] { 1024 }); // 1024, 256
    text2vecWeaviateSettings.put("base_url", new String[] { REST_HOST });
    text2vecWeaviate.put("text2vec-weaviate", text2vecWeaviateSettings);

    // Define the vector configurations
    Map<String, WeaviateClass.VectorConfig> vectorConfig = new HashMap<>();
    vectorConfig.put("title_vector", WeaviateClass.VectorConfig.builder()
        .vectorIndexType("hnsw")
        .vectorizer(text2vecWeaviate)
        .build());

    // Create the collection "DemoCollection"
    WeaviateClass clazz = WeaviateClass.builder()
        .className(DEMO_COLLECTION)
        .vectorConfig(vectorConfig)
        .build();

    Result<Boolean> result = client.schema().classCreator().withClass(clazz).run();
    assertNull("successfully created DemoCollection", result.getError());
  }

  private void importObjects(WeaviateClient client) {
    Map<String, String> object1 = new HashMap<String, String>() {
      {
        this.put("title", "Object One");
      }
    };

    List<Map<String, String>> sourceObjects = new ArrayList<Map<String, String>>() {
      {
        this.add(object1);
      }
    };
    List<HashMap<String, Object>> objects = new ArrayList<>();
    for (Map<String, String> sourceObject : sourceObjects) {
      HashMap<String, Object> schema = new HashMap<>();
      schema.put("title", sourceObject.get("title"));
      schema.put("description", sourceObject.get("description"));
      objects.add(schema);
    }

    // Batch write items
    ObjectsBatcher batcher = client.batch().objectsBatcher();
    for (Map<String, Object> properties : objects) {
      batcher.withObject(WeaviateObject.builder()
          .className(DEMO_COLLECTION)
          .properties(properties)
          .build());
    }

    // Flush
    Result<?> result = batcher.run();
    assertNull("successfully imported objects", result.getError());
  }
}
