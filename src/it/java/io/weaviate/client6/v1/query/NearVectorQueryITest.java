package io.weaviate.client6.v1.query;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.BeforeClass;
import org.junit.Test;

import io.weaviate.ConcurrentTest;
import io.weaviate.client6.WeaviateClient;
import io.weaviate.client6.v1.Vectors;
import io.weaviate.client6.v1.collections.VectorIndex;
import io.weaviate.containers.Container;

public class NearVectorQueryITest extends ConcurrentTest {
  private static final WeaviateClient client = Container.WEAVIATE.getClient();

  private static final String COLLECTION = unique("Things");
  private static final String VECTOR_INDEX = "bring_your_own";

  /**
   * One of the inserted vectors which will be used as target vector for search.
   */
  private static Float[] searchVector;

  @BeforeClass
  public static void beforeAll() throws IOException {
    createTestCollection();
    var created = createVectors(10);
    searchVector = created.values().iterator().next();
  }

  @Test
  public void testNearVector() {
    // TODO: test that we return the results in the expected order
    // Because re-ranking should work correctly
    var things = client.collections.use(COLLECTION);
    QueryResult<Map<String, Object>> result = things.query.nearVector(searchVector,
        opt -> opt
            .distance(2f)
            .limit(3)
            .returnMetadata(MetadataField.DISTANCE));

    Assertions.assertThat(result.objects).hasSize(3);
    float maxDistance = Collections.max(result.objects,
        Comparator.comparing(obj -> obj.metadata.distance)).metadata.distance;
    Assertions.assertThat(maxDistance).isLessThanOrEqualTo(2f);
  }

  /**
   * Insert 10 objects with random vectors.
   *
   * @returns IDs of inserted objects and their corresponding vectors.
   */
  private static Map<String, Float[]> createVectors(int n) throws IOException {
    var created = new HashMap<String, Float[]>();

    var things = client.collections.use(COLLECTION);
    for (int i = 0; i < n; i++) {
      var vector = randomVector(10, -.01f, .001f);
      var object = things.data.insert(
          Map.of(),
          metadata -> metadata
              .id(randomUUID())
              .vectors(Vectors.of(VECTOR_INDEX, vector)));

      created.put(object.metadata().id(), vector);
    }

    return created;
  }

  /**
   * Create {@link COLLECTION} with {@link VECTOR_INDEX} vector index.
   *
   * @throws IOException
   */
  private static void createTestCollection() throws IOException {
    client.collections.create(COLLECTION, cfg -> cfg
        .vector(VECTOR_INDEX, VectorIndex.hnsw()));
  }
}
