package io.weaviate.integration;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TestRule;

import io.weaviate.ConcurrentTest;
import io.weaviate.client6.WeaviateClient;
import io.weaviate.client6.v1.api.collections.query.GroupBy;
import io.weaviate.client6.v1.api.collections.query.MetadataField;
import io.weaviate.client6.v1.api.collections.query.QueryObject;
import io.weaviate.client6.v1.api.collections.query.QueryResponseGroup;
import io.weaviate.client6.v1.collections.Property;
import io.weaviate.client6.v1.collections.Reference;
import io.weaviate.client6.v1.collections.VectorIndex;
import io.weaviate.client6.v1.collections.VectorIndex.IndexingStrategy;
import io.weaviate.client6.v1.collections.Vectorizer;
import io.weaviate.client6.v1.collections.object.Vectors;
import io.weaviate.containers.Container;
import io.weaviate.containers.Container.ContainerGroup;
import io.weaviate.containers.Contextionary;
import io.weaviate.containers.Img2VecNeural;
import io.weaviate.containers.Weaviate;

public class SearchITest extends ConcurrentTest {
  private static final ContainerGroup compose = Container.compose(
      Weaviate.custom()
          .withContextionaryUrl(Contextionary.URL)
          .withImageInference(Img2VecNeural.URL, Img2VecNeural.MODULE)
          .build(),
      Container.IMG2VEC_NEURAL,
      Container.CONTEXTIONARY);
  @ClassRule // Bind containers to the lifetime of the test
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
    var things = client.collections.use(COLLECTION);
    var result = things.query.nearVector(searchVector,
        opt -> opt
            .distance(2f)
            .limit(3)
            .returnMetadata(MetadataField.DISTANCE));

    Assertions.assertThat(result.objects()).hasSize(3);
    float maxDistance = Collections.max(result.objects(),
        Comparator.comparing(obj -> obj.metadata().distance())).metadata().distance();
    Assertions.assertThat(maxDistance).isLessThanOrEqualTo(2f);
  }

  @Test
  public void testNearVector_groupBy() {
    var things = client.collections.use(COLLECTION);
    var result = things.query.nearVector(searchVector,
        opt -> opt.distance(10f),
        GroupBy.property("category", 2, 5));

    Assertions.assertThat(result.groups())
        .as("group per category").containsOnlyKeys(CATEGORIES)
        .hasSizeLessThanOrEqualTo(2)
        .allSatisfy((category, group) -> {
          Assertions.assertThat(group)
              .as("group name").returns(category, QueryResponseGroup::name);
          Assertions.assertThat(group.numberOfObjects())
              .as("[%s] has 1+ object", category).isLessThanOrEqualTo(5L);
        });

    Assertions.assertThat(result.objects())
        .as("object belongs a group")
        .allMatch(obj -> result.groups().get(obj.belongsToGroup()).objects().contains(obj));
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
            .vector(new VectorIndex<>(IndexingStrategy.hnsw(), Vectorizer.text2vecContextionary())));

    var songs = client.collections.use(nsSongs);
    var submarine = songs.data.insert(Map.of("title", "Yellow Submarine"));
    songs.data.insert(Map.of("title", "Run Through The Jungle"));
    songs.data.insert(Map.of("title", "Welcome To The Jungle"));

    var result = songs.query.nearText("forest",
        opt -> opt
            .distance(0.5f)
            .moveTo(.98f, to -> to.concepts("tropical"))
            .moveAway(.4f, away -> away.uuids(submarine.metadata().id()))
            .returnProperties("title"));

    Assertions.assertThat(result.objects()).hasSize(2)
        .extracting(QueryObject::properties).allSatisfy(
            properties -> Assertions.assertThat(properties)
                .allSatisfy((_k, v) -> Assertions.assertThat((String) v).contains("Jungle")));
  }

  @Test
  public void testNearText_groupBy() throws IOException {
    var vectorIndex = new VectorIndex<>(IndexingStrategy.hnsw(), Vectorizer.text2vecContextionary());

    var nsArtists = ns("Artists");
    client.collections.create(nsArtists,
        col -> col
            .properties(Property.text("name"))
            .vector(vectorIndex));

    var artists = client.collections.use(nsArtists);
    var beatles = artists.data.insert(Map.of("name", "Beatles"));
    var ccr = artists.data.insert(Map.of("name", "CCR"));

    var nsSongs = ns("Songs");
    client.collections.create(nsSongs,
        col -> col
            .properties(Property.text("title"))
            .references(Property.reference("performedBy", nsArtists))
            .vector(vectorIndex));

    var songs = client.collections.use(nsSongs);
    songs.data.insert(Map.of("title", "Yellow Submarine"),
        s -> s.reference("performedBy", Reference.objects(beatles)));
    songs.data.insert(Map.of("title", "Run Through The Jungle"),
        s -> s.reference("performedBy", Reference.objects(ccr)));

    var result = songs.query.nearText("nature",
        opt -> opt.returnProperties("title"),
        GroupBy.property("performedBy", 2, 1));

    Assertions.assertThat(result.groups()).hasSize(2)
        .containsOnlyKeys(
            "weaviate://localhost/%s/%s".formatted(nsArtists, beatles.metadata().id()),
            "weaviate://localhost/%s/%s".formatted(nsArtists, ccr.metadata().id()));
  }

  @Test
  // @Ignore("no fitting image to test with")
  public void testNearImage() throws IOException {
    System.out.println("start test");
    var nsCats = ns("Cats");

    client.collections.create(nsCats,
        collection -> collection
            .properties(
                Property.text("breed"),
                Property.blob("img"))
            .vector(new VectorIndex<>(
                IndexingStrategy.hnsw(),
                Vectorizer.img2VecNeuralVectorizer(
                    i2v -> i2v.imageFields("img")))));
    System.out.println("created collection");

    var cats = client.collections.use(nsCats);
    cats.data.insert(Map.of(
        "breed", "ragdoll",
        "img", EncodedMedia.IMAGE));

    System.out.println("inserted data");
    var got = cats.query.nearImage(EncodedMedia.IMAGE,
        opt -> opt.returnProperties("breed"));

    System.out.println("searched");
    Assertions.assertThat(got.objects()).hasSize(1).first()
        .extracting(QueryObject::properties, InstanceOfAssertFactories.MAP)
        .extractingByKey("breed").isEqualTo("ragdoll");
  }
}
