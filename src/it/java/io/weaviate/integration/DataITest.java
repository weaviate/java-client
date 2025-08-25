package io.weaviate.integration;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.BeforeClass;
import org.junit.Test;

import io.weaviate.ConcurrentTest;
import io.weaviate.client6.v1.api.WeaviateApiException;
import io.weaviate.client6.v1.api.WeaviateClient;
import io.weaviate.client6.v1.api.collections.Property;
import io.weaviate.client6.v1.api.collections.Vectorizers;
import io.weaviate.client6.v1.api.collections.Vectors;
import io.weaviate.client6.v1.api.collections.WeaviateObject;
import io.weaviate.client6.v1.api.collections.data.BatchReference;
import io.weaviate.client6.v1.api.collections.data.DeleteManyResponse;
import io.weaviate.client6.v1.api.collections.data.Reference;
import io.weaviate.client6.v1.api.collections.query.Metadata;
import io.weaviate.client6.v1.api.collections.query.QueryMetadata;
import io.weaviate.client6.v1.api.collections.query.QueryReference;
import io.weaviate.client6.v1.api.collections.query.Where;
import io.weaviate.containers.Container;

public class DataITest extends ConcurrentTest {
  private static WeaviateClient client = Container.WEAVIATE.getClient();
  private static final String COLLECTION = unique("Artists");
  private static final String VECTOR_INDEX = "bring_your_own";

  @BeforeClass
  public static void beforeAll() throws IOException {
    createTestCollections();
  }

  @Test
  public void testCreateGetDelete() throws IOException {
    var artists = client.collections.use(COLLECTION);
    var id = randomUUID();
    float[] vector = { 1, 2, 3 };

    artists.data.insert(Map.of("name", "john doe"),
        metadata -> metadata
            .uuid(id)
            .vectors(Vectors.of(VECTOR_INDEX, vector)));

    var object = artists.query.byId(id, query -> query
        .returnProperties("name")
        .returnMetadata(
            Metadata.UUID, Metadata.VECTOR,
            Metadata.CREATION_TIME_UNIX, Metadata.LAST_UPDATE_TIME_UNIX));

    Assertions.assertThat(artists.data.exists(id))
        .as("object exists after insert").isTrue();
    Assertions.assertThat(object)
        .as("object has correct properties").get()
        .satisfies(obj -> {
          Assertions.assertThat(obj.metadata().uuid())
              .as("object id").isEqualTo(id);

          Assertions.assertThat(obj.metadata().vectors().getSingle(VECTOR_INDEX))
              .containsExactly(vector);

          Assertions.assertThat(obj.properties())
              .as("has expected properties")
              .containsEntry("name", "john doe");

          Assertions.assertThat(obj.metadata().creationTimeUnix())
              .as("creationTimeUnix").isNotNull();
          Assertions.assertThat(obj.metadata().lastUpdateTimeUnix())
              .as("lastUpdateTimeUnix").isNotNull();
        });

    artists.data.delete(id);
    Assertions.assertThat(artists.data.exists(id))
        .as("object not exists after deletion").isFalse();
  }

  @Test
  public void testBlobData() throws IOException {
    var nsCats = ns("Cats");

    client.collections.create(nsCats,
        collection -> collection.properties(
            Property.text("breed"),
            Property.blob("img")));

    var cats = client.collections.use(nsCats);
    var ragdollPng = EncodedMedia.IMAGE;
    var ragdoll = cats.data.insert(Map.of(
        "breed", "ragdoll",
        "img", ragdollPng));

    var got = cats.query.byId(ragdoll.metadata().uuid(),
        cat -> cat.returnProperties("img"));

    Assertions.assertThat(got).get()
        .extracting(WeaviateObject::properties, InstanceOfAssertFactories.MAP)
        .extractingByKey("img").isEqualTo(ragdollPng);
  }

  private static void createTestCollections() throws IOException {
    var awardsGrammy = unique("Grammy");
    client.collections.create(awardsGrammy);

    var awardsOscar = unique("Oscar");
    client.collections.create(awardsOscar);

    client.collections.create(COLLECTION,
        col -> col
            .properties(
                Property.text("name"),
                Property.integer("age"))
            .references(
                Property.reference("hasAwards", awardsGrammy, awardsOscar))
            .vectors(Vectorizers.none(VECTOR_INDEX)));
  }

  @Test
  public void testReferences_AddReplaceDelete() throws IOException {
    // Arrange
    var nsPersons = ns("Person");

    client.collections.create(nsPersons,
        collection -> collection
            .properties(Property.text("name"))
            .references(Property.reference("hasFriend", nsPersons)));

    var persons = client.collections.use(nsPersons);
    var john = persons.data.insert(Map.of("name", "john"));
    var albie = persons.data.insert(Map.of("name", "albie"));

    // Act: add reference
    persons.data.referenceAdd(
        john.metadata().uuid(),
        "hasFriend",
        Reference.object(albie));

    // Assert
    var johnWithFriends = persons.query.byId(john.metadata().uuid(),
        query -> query.returnReferences(
            QueryReference.single("hasFriend",
                friend -> friend.returnProperties("name"))));

    Assertions.assertThat(johnWithFriends).get()
        .as("friends after ADD")
        .extracting(WeaviateObject::references).extracting("hasFriend")
        .asInstanceOf(InstanceOfAssertFactories.list(WeaviateObject.class))
        .hasSize(1)
        .first().extracting(WeaviateObject::properties, InstanceOfAssertFactories.MAP)
        .returns("albie", friend -> friend.get("name"));

    // Act: replace reference
    var barbara = persons.data.insert(Map.of("name", "barbara"));
    persons.data.referenceReplace(
        john.metadata().uuid(),
        "hasFriend",
        Reference.object(barbara));

    johnWithFriends = persons.query.byId(john.metadata().uuid(),
        query -> query.returnReferences(
            QueryReference.single("hasFriend",
                friend -> friend.returnProperties("name"))));

    Assertions.assertThat(johnWithFriends).get()
        .as("friends after REPLACE")
        .extracting(WeaviateObject::references).extracting("hasFriend")
        .asInstanceOf(InstanceOfAssertFactories.list(WeaviateObject.class))
        .hasSize(1)
        .first().extracting(WeaviateObject::properties, InstanceOfAssertFactories.MAP)
        .returns("barbara", friend -> friend.get("name"));

    // Act: delete reference
    persons.data.referenceDelete(
        john.metadata().uuid(),
        "hasFriend",
        Reference.object(barbara));

    // Assert
    johnWithFriends = persons.query.byId(john.metadata().uuid(),
        query -> query.returnReferences(
            QueryReference.single("hasFriend")));

    Assertions.assertThat(johnWithFriends).get()
        .as("friends after DELETE")
        .extracting(WeaviateObject::references).extracting("hasFriend")
        .asInstanceOf(InstanceOfAssertFactories.list(WeaviateObject.class))
        .isEmpty();
  }

  @Test
  public void testReplace() throws IOException {
    // Arrange
    var nsBooks = ns("Books");

    client.collections.create(nsBooks,
        collection -> collection
            .properties(Property.text("title"), Property.integer("year")));

    // Add 1 book with 'title' only.
    var books = client.collections.use(nsBooks);
    var ivanhoe = books.data.insert(Map.of("title", "ivanhoe"));

    // Act
    books.data.replace(ivanhoe.metadata().uuid(),
        replace -> replace.properties(Map.of("year", 1819)));

    // Assert
    var replacedIvanhoe = books.query.byId(ivanhoe.metadata().uuid());

    Assertions.assertThat(replacedIvanhoe).get()
        .as("has ONLY year property")
        .extracting(WeaviateObject::properties, InstanceOfAssertFactories.MAP)
        .doesNotContain(Map.entry("title", "ivanhoe"))
        .contains(Map.entry("year", 1819L));
  }

  @Test
  public void testUpdate() throws IOException {
    // Arrange
    var nsBooks = ns("Books");
    var nsAuthors = ns("Authors");

    client.collections.create(nsAuthors,
        collection -> collection
            .properties(Property.text("name")));

    client.collections.create(nsBooks,
        collection -> collection
            .properties(Property.text("title"), Property.integer("year"))
            .references(Property.reference("writtenBy", nsAuthors))
            .vectors(Vectorizers.none()));

    var authors = client.collections.use(nsAuthors);
    var walter = authors.data.insert(Map.of("name", "walter scott"));

    var vector = new float[] { 1, 2, 3 };

    var books = client.collections.use(nsBooks);

    // Add 1 book without mentioning its author, year published,
    // or supplying a vector.
    var ivanhoe = books.data.insert(Map.of("title", "ivanhoe"));

    // Act
    books.data.update(ivanhoe.metadata().uuid(),
        update -> update
            .properties(Map.of("year", 1819))
            .reference("writtenBy", Reference.objects(walter))
            .vectors(Vectors.of(vector)));

    // Assert
    var updIvanhoe = books.query.byId(
        ivanhoe.metadata().uuid(),
        query -> query
            .returnMetadata(Metadata.VECTOR)
            .returnReferences(
                QueryReference.single("writtenBy",
                    writtenBy -> writtenBy.returnMetadata(Metadata.UUID))));

    Assertions.assertThat(updIvanhoe).get()
        .satisfies(book -> {
          Assertions.assertThat(book)
              .as("has both year and title property")
              .extracting(WeaviateObject::properties, InstanceOfAssertFactories.MAP)
              .contains(Map.entry("title", "ivanhoe"), Map.entry("year", 1819L));

          Assertions.assertThat(book)
              .as("has reference to Authors")
              .extracting(WeaviateObject::references, InstanceOfAssertFactories.MAP)
              .extractingByKey("writtenBy", InstanceOfAssertFactories.list(WeaviateObject.class))
              .first()
              .extracting(WeaviateObject::properties, InstanceOfAssertFactories.MAP)
              .contains(Map.entry("name", "walter scott"));

          Assertions.assertThat(book)
              .as("has a vector")
              .extracting(WeaviateObject::metadata)
              .extracting(QueryMetadata::vectors)
              .returns(vector, Vectors::getDefaultSingle);
        });
  }

  @Test
  public void testDeleteMany() throws IOException {
    // Arrange
    var nsThings = ns("Things");

    client.collections.create(nsThings,
        collection -> collection
            .properties(Property.integer("last_used")));

    var things = client.collections.use(nsThings);
    things.data.insert(Map.of("last_used", 1));
    var delete_1 = things.data.insert(Map.of("last_used", 5)).metadata().uuid();
    var delete_2 = things.data.insert(Map.of("last_used", 9)).metadata().uuid();

    // Act (dry run)
    things.data.deleteMany(
        Where.property("last_used").gte(4),
        opt -> opt.dryRun(true));

    // Assert
    Assertions.assertThat(things.data.exists(delete_1)).as("#1 exists").isTrue();
    Assertions.assertThat(things.data.exists(delete_2)).as("#2 exists").isTrue();

    // Act (live run)
    var deleted = things.data.deleteMany(
        Where.property("last_used").gte(4),
        opt -> opt.verbose(true));

    // Assert
    Assertions.assertThat(deleted)
        .returns(2L, DeleteManyResponse::matches)
        .returns(2L, DeleteManyResponse::successful)
        .returns(0L, DeleteManyResponse::failed)
        .extracting(DeleteManyResponse::objects, InstanceOfAssertFactories.list(DeleteManyResponse.DeletedObject.class))
        .extracting(DeleteManyResponse.DeletedObject::uuid)
        .containsOnly(delete_1, delete_2);

    var count = things.aggregate.overAll(
        cnt -> cnt
            .objectLimit(100)
            .includeTotalCount(true))
        .totalCount();

    Assertions.assertThat(count)
        .as("one object remaining")
        .isEqualTo(1);

  }

  @Test
  public void testInsertMany() throws IOException {
    // Arrange
    var nsThings = ns("Things");

    client.collections.create(nsThings);

    var things = client.collections.use(nsThings);

    // Act
    things.data.insertMany(Map.of(), Map.of(), Map.of(), Map.of(), Map.of());

    // Assert
    var count = things.aggregate.overAll(
        cnt -> cnt
            .objectLimit(100)
            .includeTotalCount(true))
        .totalCount();

    Assertions.assertThat(count)
        .as("collection has 5 objects")
        .isEqualTo(5);
  }

  @Test
  public void testReferenceAddMany() throws IOException {
    // Arrange
    var nsCities = ns("Cities");
    var nsAirports = ns("Airports");

    client.collections.create(nsAirports);
    client.collections.create(nsCities, c -> c
        .references(Property.reference("hasAirports", nsAirports)));

    var airports = client.collections.use(nsAirports);
    var cities = client.collections.use(nsCities);

    var alpha = airports.data.insert(Map.of()).uuid();
    var goodburg = cities.data.insert(Map.of(), city -> city
        .reference("hasAirports", Reference.uuids(alpha)));

    // Act
    var newAirports = airports.data.insertMany(Map.of(), Map.of());
    var bravo = newAirports.responses().get(0).uuid();
    var charlie = newAirports.responses().get(1).uuid();

    var response = cities.data.referenceAddMany(BatchReference.uuids(goodburg, "hasAirports", bravo, charlie));

    // Assert
    Assertions.assertThat(response.errors()).isEmpty();

    var goodburgAirports = cities.query.byId(goodburg.metadata().uuid(),
        city -> city.returnReferences(
            QueryReference.single("hasAirports",
                airport -> airport.returnMetadata(Metadata.UUID))));

    Assertions.assertThat(goodburgAirports).get()
        .as("Goodburg has 3 airports")
        .extracting(WeaviateObject::references)
        .extracting(references -> references.get("hasAirports"), InstanceOfAssertFactories.list(WeaviateObject.class))
        .extracting(WeaviateObject::uuid)
        .contains(alpha, bravo, charlie);
  }

  @Test(expected = WeaviateApiException.class)
  public void testDuplicateUuid() throws IOException {
    // Arrange
    var nsThings = ns("Things");

    client.collections.create(nsThings);
    var things = client.collections.use(nsThings);
    var thing_1 = things.data.insert(Map.of());

    // Act
    things.data.insert(Map.of(), thing -> thing.uuid(thing_1.uuid()));
  }

  @Test
  public void testDataTypes() throws IOException {
    // Arrange
    var nsDataTypes = ns("DataTypes");

    // BLOB type is omitted because a base64-encoded image
    // isn't doing the failure message any favours.
    // It's tested in other test cases above.
    client.collections.create(
        nsDataTypes, c -> c
            .properties(
                Property.text("prop_text"),
                Property.integer("prop_integer"),
                Property.number("prop_number"),
                Property.bool("prop_bool"),
                Property.date("prop_date"),
                Property.uuid("prop_uuid"),
                Property.uuidArray("prop_uuid_array"),
                Property.textArray("prop_text_array")));

    var types = client.collections.use(nsDataTypes);

    var now = OffsetDateTime.now();
    var uuid = UUID.randomUUID();

    Map<String, Object> want = Map.of(
        "prop_text", "Hello, World!",
        "prop_integer", 1L,
        "prop_number", 1D,
        "prop_bool", true,
        "prop_date", now,
        "prop_uuid", uuid,
        "prop_uuid_array", List.of(uuid, uuid),
        "prop_text_array", List.of("a", "b", "c"));
    var returnProperties = want.keySet().toArray(String[]::new);

    // Act
    var object = types.data.insert(want);
    var got = types.query.byId(object.uuid(),
        q -> q.returnProperties(returnProperties));

    // Assert
    Assertions.assertThat(got).get()
        .extracting(WeaviateObject::properties)
        .asInstanceOf(InstanceOfAssertFactories.map(String.class, Object.class))
        .containsAllEntriesOf(want);

  }
}
