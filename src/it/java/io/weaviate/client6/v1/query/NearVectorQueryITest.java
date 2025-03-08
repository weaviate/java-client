package io.weaviate.client6.v1.query;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.stream.IntStream;

import org.assertj.core.api.Assertions;
import org.junit.BeforeClass;
import org.junit.Test;

import io.weaviate.client6.Config;
import io.weaviate.client6.WeaviateClient;
import io.weaviate.client6.v1.data.Vectors;
import io.weaviate.containers.Container;

public class NearVectorQueryITest {
  private static final WeaviateClient client = new WeaviateClient(
      new Config("http", Container.WEAVIATE.getHttpHostAddress()));

  private static final String COLLECTION = "Things";
  private static final Random rand = new Random();

  private static Float[] searchVector;

  @BeforeClass
  public static void beforeAll() throws IOException {
    var created = createVectors(10);
    searchVector = created.values().iterator().next();
  }

  @Test
  public void testNearVector() {
    var things = client.collections.use(COLLECTION);
    SearchResult<Map<String, Object>> result = things.query.nearVector(searchVector,
        opt -> opt
            .distance(.002f)
            .limit(3));

    Assertions.assertThat(result.objects).hasSize(3);
    float maxDistance = Collections.max(result.objects,
        Comparator.comparing(obj -> obj.metadata.distance)).metadata.distance;
    Assertions.assertThat(maxDistance).isLessThanOrEqualTo(.002f);
  }

  static Map<String, Float[]> createVectors(int n) throws IOException {
    var created = new HashMap<String, Float[]>();

    var things = client.collections.use(COLLECTION);
    for (int i = 0; i < n; i++) {
      Float[] vector = IntStream.range(0, 9).<Float>mapToObj(f -> rand.nextFloat(-0.01f, 0.001f))
          .toArray(Float[]::new);
      var object = things.data.insert(
          Map.of(),
          metadata -> metadata
              .id(UUID.randomUUID().toString())
              .vectors(Vectors.of(vector)));

      created.put(object.metadata.id, vector);
    }

    return created;
  }
}
