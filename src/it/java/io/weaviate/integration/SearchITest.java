package io.weaviate.integration;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TestRule;

import io.weaviate.ConcurrentTest;
import io.weaviate.client6.WeaviateClient;
import io.weaviate.client6.v1.collections.Property;
import io.weaviate.client6.v1.collections.VectorIndex;
import io.weaviate.client6.v1.collections.VectorIndex.IndexingStrategy;
import io.weaviate.client6.v1.collections.Vectorizer;
import io.weaviate.client6.v1.collections.object.Vectors;
import io.weaviate.client6.v1.collections.query.GroupedQueryResult;
import io.weaviate.client6.v1.collections.query.MetadataField;
import io.weaviate.client6.v1.collections.query.NearVector;
import io.weaviate.containers.Container;
import io.weaviate.containers.Container.ContainerGroup;
import io.weaviate.containers.Weaviate;

public class SearchITest extends ConcurrentTest {
  private static final ContainerGroup compose = Container.compose(
      Weaviate.custom().withContextionary().build(),
      Container.CONTEXTIONARY);
  @ClassRule // Bind containers to lifetime to the test
  public static final TestRule _rule = compose.asTestRule();
  private static final WeaviateClient client = compose.getClient();

  private static final String COLLECTION = unique("Things");
  private static final String VECTOR_INDEX = "bring_your_own";
  private static final List<String> CATEGORIES = List.of("red", "green");

  /**
   * One of the inserted vectors which will be used as target vector for search.
   */
  private static Float[] searchVector;

  @BeforeClass
  public static void beforeAll() throws IOException {
    createTestCollection();
    var created = populateTest(10);
    searchVector = created.values().iterator().next();
  }

  @Test
  public void testNearVector() {
    // TODO: test that we return the results in the expected order
    // Because re-ranking should work correctly
    var things = client.collections.use(COLLECTION);
    var result = things.query.nearVector(searchVector,
        opt -> opt
            .distance(2f)
            .limit(3)
            .returnMetadata(MetadataField.DISTANCE));

    Assertions.assertThat(result.objects).hasSize(3);
    float maxDistance = Collections.max(result.objects,
        Comparator.comparing(obj -> obj.metadata.distance())).metadata.distance();
    Assertions.assertThat(maxDistance).isLessThanOrEqualTo(2f);
  }

  @Test
  public void testNearVector_groupBy() {
    // TODO: test that we return the results in the expected order
    // Because re-ranking should work correctly
    var things = client.collections.use(COLLECTION);
    var result = things.query.nearVector(searchVector,
        new NearVector.GroupBy("category", 2, 5),
        opt -> opt.distance(10f));

    Assertions.assertThat(result.groups)
        .as("group per category").containsOnlyKeys(CATEGORIES)
        .hasSizeLessThanOrEqualTo(2)
        .allSatisfy((category, group) -> {
          Assertions.assertThat(group)
              .as("group name").returns(category, GroupedQueryResult.Group::name);
          Assertions.assertThat(group.numberOfObjects())
              .as("[%s] has 1+ object", category).isLessThanOrEqualTo(5L);
        });

    Assertions.assertThat(result.objects)
        .as("object belongs a group")
        .allMatch(obj -> result.groups.get(obj.belongsToGroup).objects().contains(obj));
  }

  /**
   * Insert 10 objects with random vectors.
   *
   * @returns IDs of inserted objects and their corresponding vectors.
   */
  private static Map<String, Float[]> populateTest(int n) throws IOException {
    var created = new HashMap<String, Float[]>();

    var things = client.collections.use(COLLECTION);
    for (int i = 0; i < n; i++) {
      var vector = randomVector(10, -.01f, .001f);
      var object = things.data.insert(
          Map.of("category", CATEGORIES.get(i % CATEGORIES.size())),
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
        .properties(Property.text("category"))
        .vector(VECTOR_INDEX, new VectorIndex<>(IndexingStrategy.hnsw(), Vectorizer.none())));
  }

  @Test
  public void testNearText() throws IOException {
    var nsSongs = ns("Songs");
    client.collections.create(nsSongs,
        col -> col
            .properties(Property.text("title"))
            .vector(new VectorIndex<>(IndexingStrategy.hnsw(), Vectorizer.contextionary())));

    var songs = client.collections.use(nsSongs);
    songs.data.insert(Map.of("title", "Yellow Submarine"));
    songs.data.insert(Map.of("title", "Run Through The Jungle"));
    songs.data.insert(Map.of("title", "Welcome To The Jungle"));

    var result = songs.query.nearText("forest",
        opt -> opt
            .distance(0.5f)
            .returnProperties("title"));

    Assertions.assertThat(result.objects).hasSize(2)
        .extracting(obj -> obj.properties).allSatisfy(
            properties -> Assertions.assertThat(properties)
                .allSatisfy((_k, v) -> Assertions.assertThat((String) v).contains("Jungle")));
  }
}
