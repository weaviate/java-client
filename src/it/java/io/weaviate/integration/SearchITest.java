package io.weaviate.integration;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TestRule;

import io.weaviate.ConcurrentTest;
import io.weaviate.client6.v1.api.WeaviateApiException;
import io.weaviate.client6.v1.api.WeaviateClient;
import io.weaviate.client6.v1.api.collections.Property;
import io.weaviate.client6.v1.api.collections.Vectorizers;
import io.weaviate.client6.v1.api.collections.Vectors;
import io.weaviate.client6.v1.api.collections.WeaviateMetadata;
import io.weaviate.client6.v1.api.collections.WeaviateObject;
import io.weaviate.client6.v1.api.collections.data.Reference;
import io.weaviate.client6.v1.api.collections.query.GroupBy;
import io.weaviate.client6.v1.api.collections.query.Metadata;
import io.weaviate.client6.v1.api.collections.query.QueryMetadata;
import io.weaviate.client6.v1.api.collections.query.QueryResponseGroup;
import io.weaviate.client6.v1.api.collections.query.SortBy;
import io.weaviate.client6.v1.api.collections.query.Where;
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
  private static float[] searchVector;

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
            .returnMetadata(Metadata.DISTANCE));

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
   * @return IDs of inserted objects and their corresponding vectors.
   */
  private static Map<String, float[]> populateTest(int n) throws IOException {
    var created = new HashMap<String, float[]>();

    var things = client.collections.use(COLLECTION);
    for (int i = 0; i < n; i++) {
      var vector = randomVector(10, -.01f, .001f);
      var object = things.data.insert(
          Map.of("category", CATEGORIES.get(i % CATEGORIES.size())),
          metadata -> metadata
              .uuid(randomUUID())
              .vectors(Vectors.of(VECTOR_INDEX, vector)));

      created.put(object.metadata().uuid(), vector);
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
        .vectors(Vectorizers.selfProvided(VECTOR_INDEX)));
  }

  @Test
  public void testNearText() throws IOException {
    var nsSongs = ns("Songs");
    client.collections.create(nsSongs,
        col -> col
            .properties(Property.text("title"))
            .vectors(Vectorizers.text2vecContextionary()));

    var songs = client.collections.use(nsSongs);
    var submarine = songs.data.insert(Map.of("title", "Yellow Submarine"));
    songs.data.insert(Map.of("title", "Run Through The Jungle"));
    songs.data.insert(Map.of("title", "Welcome To The Jungle"));

    var result = songs.query.nearText("forest",
        opt -> opt
            .distance(0.5f)
            .moveTo(.98f, to -> to.concepts("tropical"))
            .moveAway(.4f, away -> away.uuids(submarine.metadata().uuid()))
            .returnProperties("title"));

    Assertions.assertThat(result.objects()).hasSize(2)
        .extracting(WeaviateObject::properties).allSatisfy(
            properties -> Assertions.assertThat(properties)
                .allSatisfy((_k, v) -> Assertions.assertThat((String) v).contains("Jungle")));
  }

  @Test
  public void testNearText_groupBy() throws IOException {
    var vectorizer = Vectorizers.text2vecContextionary();

    var nsArtists = ns("Artists");
    client.collections.create(nsArtists,
        col -> col
            .properties(Property.text("name"))
            .vectors(vectorizer));

    var artists = client.collections.use(nsArtists);
    var beatles = artists.data.insert(Map.of("name", "Beatles"));
    var ccr = artists.data.insert(Map.of("name", "CCR"));

    var nsSongs = ns("Songs");
    client.collections.create(nsSongs,
        col -> col
            .properties(Property.text("title"))
            .references(Property.reference("performedBy", nsArtists))
            .vectors(vectorizer));

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
            "weaviate://localhost/%s/%s".formatted(nsArtists, beatles.metadata().uuid()),
            "weaviate://localhost/%s/%s".formatted(nsArtists, ccr.metadata().uuid()));
  }

  @Test
  public void testNearImage() throws IOException {
    var nsCats = ns("Cats");

    client.collections.create(nsCats,
        collection -> collection
            .properties(
                Property.text("breed"),
                Property.blob("img"))
            .vectors(Vectorizers.img2vecNeural(
                i2v -> i2v.imageFields("img"))));

    var cats = client.collections.use(nsCats);
    cats.data.insert(Map.of(
        "breed", "ragdoll",
        "img", EncodedMedia.IMAGE));

    var got = cats.query.nearImage(EncodedMedia.IMAGE,
        opt -> opt.returnProperties("breed"));

    Assertions.assertThat(got.objects()).hasSize(1).first()
        .extracting(WeaviateObject::properties, InstanceOfAssertFactories.MAP)
        .extractingByKey("breed").isEqualTo("ragdoll");
  }

  @Test
  public void testFetchObjectsWithFilters() throws IOException {
    var nsHats = ns("Hats");

    client.collections.create(nsHats,
        collection -> collection
            .properties(
                Property.text("colour"),
                Property.integer("size")));

    var hats = client.collections.use(nsHats);

    /* blackHat */ hats.data.insert(Map.of("colour", "black", "size", 6));
    var redHat = hats.data.insert(Map.of("colour", "red", "size", 5));
    var greenHat = hats.data.insert(Map.of("colour", "green", "size", 1));
    var hugeHat = hats.data.insert(Map.of("colour", "orange", "size", 40));

    var got = hats.query.fetchObjects(
        query -> query.where(
            Where.or(
                Where.property("colour").eq("orange"),
                Where.and(
                    Where.property("size").gte(1),
                    Where.property("size").lt(6)))));

    Assertions.assertThat(got.objects())
        .extracting(hat -> hat.metadata().uuid())
        .containsOnly(
            redHat.metadata().uuid(),
            greenHat.metadata().uuid(),
            hugeHat.metadata().uuid());

  }

  @Test
  public void testFetchObjectsWithSort() throws Exception {
    var nsNumbers = ns("Numbers");

    // Arrange
    client.collections.create(nsNumbers,
        c -> c.properties(Property.integer("value")));

    var numbers = client.collections.use(nsNumbers);

    var one = numbers.data.insert(Map.of("value", 1L));
    var two = numbers.data.insert(Map.of("value", 2L));
    var three = numbers.data.insert(Map.of("value", 3L));

    // Act: sort ascending
    var asc = numbers.query.fetchObjects(
        q -> q.sort(SortBy.property("value")));

    Assertions.assertThat(asc.objects())
        .as("value asc")
        .hasSize(3)
        .extracting(WeaviateObject::properties)
        .extracting(object -> object.get("value"))
        .containsExactly(1L, 2L, 3L);

    // Act: sort descending
    var desc = numbers.query.fetchObjects(
        q -> q.sort(SortBy.property("value").desc()));

    Assertions.assertThat(desc.objects())
        .as("value desc")
        .hasSize(3)
        .extracting(WeaviateObject::properties)
        .extracting(object -> object.get("value"))
        .containsExactly(3L, 2L, 1L);

    // Act: sort by creation time asc
    var created = numbers.query.fetchObjects(
        q -> q.sort(SortBy.creationTime()));

    Assertions.assertThat(created.objects())
        .as("create time asc")
        .hasSize(3)
        .extracting(WeaviateObject::uuid)
        .containsExactly(one.uuid(), two.uuid(), three.uuid());

    // Act: sort by updated time desc
    numbers.data.update(one.uuid(), upd -> upd.properties(Map.of("value", -1L)));
    Thread.sleep(10);
    numbers.data.update(two.uuid(), upd -> upd.properties(Map.of("value", -2L)));
    Thread.sleep(10);
    numbers.data.update(three.uuid(), upd -> upd.properties(Map.of("value", -3L)));

    var updated = numbers.query.fetchObjects(
        q -> q.sort(
            // Both sort operators imply ordering 3-2-1
            SortBy.lastUpdateTime().desc(),
            SortBy.property("value").asc()));

    Assertions.assertThat(updated.objects())
        .as("last update time desc + value asc")
        .hasSize(3)
        .extracting(WeaviateObject::uuid)
        .containsExactly(three.uuid(), two.uuid(), one.uuid());
  }

  @Test
  public void testBm25() throws IOException, InterruptedException, ExecutionException {
    var nsWords = ns("Words");

    client.collections.create(nsWords,
        collection -> collection
            .properties(
                Property.text("relevant"),
                Property.text("irrelevant")));

    var words = client.collections.use(nsWords);

    /* notWant */ words.data.insert(Map.of("relevant", "elefant", "irrelevant", "dollar bill"));
    var want = words.data.insert(Map.of("relevant", "a dime a dollar", "irrelevant", "euro"));

    var dollarWorlds = words.query.bm25(
        "dollar",
        bm25 -> bm25.queryProperties("relevant"));

    Assertions.assertThat(dollarWorlds.objects())
        .hasSize(1)
        .extracting(WeaviateObject::metadata).extracting(QueryMetadata::uuid)
        .containsOnly(want.metadata().uuid());
  }

  /**
   * Minimal test to verify async functionality works as expected.
   * We will extend our testing framework at a later stage to automatically
   * test both sync/async clients.
   */
  @Test
  public void testBm25_async() throws Exception, InterruptedException, ExecutionException {
    var nsWords = ns("Words");

    try (final var async = client.async()) {
      async.collections.create(nsWords,
          collection -> collection
              .properties(
                  Property.text("relevant"),
                  Property.text("irrelevant")))
          .get();

      var words = async.collections.use(nsWords);

      /* notWant */ words.data.insert(Map.of("relevant", "elefant", "irrelevant", "dollar bill")).get();
      var want = words.data.insert(Map.of("relevant", "a dime a dollar", "irrelevant", "euro")).get();

      var dollarWorlds = words.query.bm25(
          "dollar",
          bm25 -> bm25.queryProperties("relevant")).get();

      Assertions.assertThat(dollarWorlds.objects())
          .hasSize(1)
          .extracting(WeaviateObject::metadata).extracting(QueryMetadata::uuid)
          .containsOnly(want.metadata().uuid());
    }
  }

  @Test
  public void testNearObject() throws IOException {
    // Arrange
    var nsAnimals = ns("Animals");

    client.collections.create(nsAnimals,
        collection -> collection
            .properties(Property.text("kind"))
            .vectors(Vectorizers.text2vecContextionary()));

    var animals = client.collections.use(nsAnimals);

    // Terrestrial animals
    var cat = animals.data.insert(Map.of("kind", "cat"));
    var lion = animals.data.insert(Map.of("kind", "lion"));
    // Aquatic animal
    animals.data.insert(Map.of("kind", "dolphin"));

    // Act
    var terrestrial = animals.query.nearObject(cat.metadata().uuid(),
        q -> q.excludeSelf().limit(1));

    // Assert
    Assertions.assertThat(terrestrial.objects())
        .hasSize(1)
        .extracting(WeaviateObject::metadata).extracting(WeaviateMetadata::uuid)
        .containsOnly(lion.metadata().uuid());
  }

  @Test
  public void testHybrid() throws IOException {
    // Arrange
    var nsHobbies = ns("Hobbies");

    client.collections.create(nsHobbies,
        collection -> collection
            .properties(Property.text("name"), Property.text("description"))
            .vectors(Vectorizers.text2vecContextionary()));

    var hobbies = client.collections.use(nsHobbies);

    var skiing = hobbies.data.insert(Map.of("name", "skiing", "description", "winter sport"));
    hobbies.data.insert(Map.of("name", "jetskiing", "description", "water sport"));

    // Act
    var winterSport = hobbies.query.hybrid("winter",
        hybrid -> hybrid
            .returnMetadata(Metadata.SCORE, Metadata.EXPLAIN_SCORE));

    // Assert
    Assertions.assertThat(winterSport.objects())
        .hasSize(1)
        .extracting(WeaviateObject::metadata).extracting(WeaviateMetadata::uuid)
        .containsOnly(skiing.metadata().uuid());

    var first = winterSport.objects().get(0);
    Assertions.assertThat(first.metadata().score())
        .as("metadata::score").isNotNull();
    Assertions.assertThat(first.metadata().explainScore())
        .as("metadata::explainScore").isNotNull();
  }

  @Test(expected = WeaviateApiException.class)
  public void testBadRequest() throws IOException {
    // Arrange
    var nsThings = ns("Things");

    client.collections.create(nsThings,
        collection -> collection
            .properties(Property.text("name"))
            .vectors(Vectorizers.text2vecContextionary()));

    var things = client.collections.use(nsThings);
    var balloon = things.data.insert(Map.of("name", "balloon"));

    things.query.nearObject(balloon.uuid(), q -> q.limit(-1));
  }

  @Test(expected = WeaviateApiException.class)
  public void testBadRequest_async() throws Throwable {
    // Arrange
    var nsThings = ns("Things");

    try (final var async = client.async()) {
      async.collections.create(nsThings,
          collection -> collection
              .properties(Property.text("name"))
              .vectors(Vectorizers.text2vecContextionary()))
          .join();

      var things = async.collections.use(nsThings);
      var balloon = things.data.insert(Map.of("name", "balloon")).join();

      try {
        things.query.nearObject(balloon.uuid(), q -> q.limit(-1)).join();
      } catch (CompletionException e) {
        throw e.getCause(); // CompletableFuture exceptions are always wrapped
      }
    }
  }
}
