package io.weaviate.integration;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import io.weaviate.ConcurrentTest;
import io.weaviate.client6.v1.api.WeaviateApiException;
import io.weaviate.client6.v1.api.WeaviateClient;
import io.weaviate.client6.v1.api.collections.GeoCoordinates;
import io.weaviate.client6.v1.api.collections.PhoneNumber;
import io.weaviate.client6.v1.api.collections.Property;
import io.weaviate.client6.v1.api.collections.ReferenceProperty;
import io.weaviate.client6.v1.api.collections.VectorConfig;
import io.weaviate.client6.v1.api.collections.Vectors;
import io.weaviate.client6.v1.api.collections.WeaviateObject;
import io.weaviate.client6.v1.api.collections.data.BatchReference;
import io.weaviate.client6.v1.api.collections.data.DeleteManyResponse;
import io.weaviate.client6.v1.api.collections.data.Reference;
import io.weaviate.client6.v1.api.collections.query.Filter;
import io.weaviate.client6.v1.api.collections.query.Metadata;
import io.weaviate.client6.v1.api.collections.query.Metadata.MetadataField;
import io.weaviate.client6.v1.api.collections.query.QueryReference;
import io.weaviate.client6.v1.api.collections.tenants.Tenant;
import io.weaviate.containers.Container;

public class DataITest extends ConcurrentTest {
  private static final WeaviateClient client = Container.WEAVIATE.getClient();
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

    var object = artists.query.fetchObjectById(id, query -> query
        .returnProperties("name")
        .returnMetadata(
            MetadataField.VECTOR,
            Metadata.CREATION_TIME_UNIX, Metadata.LAST_UPDATE_TIME_UNIX));

    Assertions.assertThat(artists.data.exists(id))
        .as("object exists after insert").isTrue();
    Assertions.assertThat(object)
        .as("object has correct properties").get()
        .satisfies(obj -> {
          Assertions.assertThat(obj.uuid())
              .as("object id").isEqualTo(id);

          Assertions.assertThat(obj.vectors().getSingle(VECTOR_INDEX))
              .containsExactly(vector);

          Assertions.assertThat(obj.properties())
              .as("has expected properties")
              .containsEntry("name", "john doe");

          Assertions.assertThat(obj.createdAt())
              .as("createdAt").isNotNull();
          Assertions.assertThat(obj.lastUpdatedAt())
              .as("lastUpdatedAt").isNotNull();
        });

    // var write = WriteWeaviateObject.of(null);
    // write.tenant(); // can be null, but that's perfectly fine
    //
    // write.references().get("").getFirst().asWeaviateObject();
    //
    // // Three key changes:
    // var wv = WeaviateObject.write(null); // 1: you can use WeaviateObject, and
    // not WriteWeaviateObject
    // write.queryMetadata(); // 2: This should be called "queryMetadata" to avoid
    // confusion
    // wv.references().forEach((key, references) -> {
    // references.forEach(ref -> {
    // ref.collection();
    // ref.uuid();
    //
    // // get "title" property from a referenced object
    // var title = ref.asWeaviateObject().properties().get("title");
    //
    // ref.asWeaviateObject().references().forEach((__, nestedRefs) -> {
    // nestedRefs.forEach(nref -> {
    // var n_title = ref.asWeaviateObject().properties().get("title");
    // });
    // });
    // });
    // });

    var deleted = artists.data.deleteById(id);
    Assertions.assertThat(deleted)
        .as("object was deleted").isTrue();
    Assertions.assertThat(artists.data.exists(id))
        .as("object not exists after deletion").isFalse();

    deleted = artists.data.deleteById(id);

    // TODO: Change to isFalse() after fixed in Weaviate server
    Assertions.assertThat(deleted)
        .as("object wasn't deleted").isTrue();
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

    var got = cats.query.fetchObjectById(ragdoll.metadata().uuid(),
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
                ReferenceProperty.to("hasAwards", awardsGrammy, awardsOscar))
            .vectorConfig(VectorConfig.selfProvided(VECTOR_INDEX)));
  }

  @Test
  public void testReferences_AddReplaceDelete() throws IOException {
    // Arrange
    var nsPersons = ns("Person");

    client.collections.create(nsPersons,
        collection -> collection
            .properties(Property.text("name"))
            .references(ReferenceProperty.to("hasFriend", nsPersons)));

    var persons = client.collections.use(nsPersons);
    var john = persons.data.insert(Map.of("name", "john"));
    var albie = persons.data.insert(Map.of("name", "albie"));

    // Act: add reference
    persons.data.referenceAdd(
        john.uuid(),
        "hasFriend",
        Reference.object(albie));

    // Assert
    var johnWithFriends = persons.query.fetchObjectById(john.metadata().uuid(),
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
        john.uuid(),
        "hasFriend",
        Reference.object(barbara));

    johnWithFriends = persons.query.fetchObjectById(john.metadata().uuid(),
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
        john.uuid(),
        "hasFriend",
        Reference.object(barbara));

    // Assert
    johnWithFriends = persons.query.fetchObjectById(john.metadata().uuid(),
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
    books.data.replace(ivanhoe.uuid(),
        replace -> replace.properties(Map.of("year", 1819)));

    // Assert
    var replacedIvanhoe = books.query.fetchObjectById(ivanhoe.metadata().uuid());

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
            .references(ReferenceProperty.to("writtenBy", nsAuthors))
            .vectorConfig(VectorConfig.selfProvided()));

    var authors = client.collections.use(nsAuthors);
    var walter = authors.data.insert(Map.of("name", "walter scott"));

    var vector = new float[] { 1, 2, 3 };

    var books = client.collections.use(nsBooks);

    // Add 1 book without mentioning its author, year published,
    // or supplying a vector.
    var ivanhoe = books.data.insert(Map.of("title", "ivanhoe"));

    // Act
    books.data.update(ivanhoe.uuid(),
        update -> update
            .properties(Map.of("year", 1819))
            .reference("writtenBy", Reference.objects(walter))
            .vectors(Vectors.of(vector)));

    // Assert
    var updIvanhoe = books.query.fetchObjectById(
        ivanhoe.metadata().uuid(),
        query -> query
            .includeVector()
            .returnReferences(QueryReference.single("writtenBy")));

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
              .extracting(WeaviateObject::vectors)
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
    var delete_1 = things.data.insert(Map.of("last_used", 5)).uuid();
    var delete_2 = things.data.insert(Map.of("last_used", 9)).uuid();

    // Act (dry run)
    things.data.deleteMany(
        Filter.property("last_used").gte(4),
        opt -> opt.dryRun(true));

    // Assert
    Assertions.assertThat(things.data.exists(delete_1)).as("#1 exists").isTrue();
    Assertions.assertThat(things.data.exists(delete_2)).as("#2 exists").isTrue();

    // Act (live run)
    var deleted = things.data.deleteMany(
        Filter.property("last_used").gte(4),
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
        .references(ReferenceProperty.to("hasAirports", nsAirports)));

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

    var goodburgAirports = cities.query.fetchObjectById(goodburg.metadata().uuid(),
        city -> city.returnReferences(
            QueryReference.single("hasAirports")));

    Assertions.assertThat(goodburgAirports).get()
        .as("Goodburg has 3 airports")
        .extracting(WeaviateObject::references)
        .extracting(references -> references.get("hasAirports"),
            InstanceOfAssertFactories.list(WeaviateObject.class))
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
                Property.integerArray("prop_integer_array"),
                Property.numberArray("prop_number_array"),
                Property.boolArray("prop_bool_array"),
                Property.dateArray("prop_date_array"),
                Property.uuidArray("prop_uuid_array"),
                Property.textArray("prop_text_array"),
                Property.phoneNumber("prop_phone_number"),
                Property.geoCoordinates("prop_geo_coordinates"),
                Property.object("prop_object",
                    p -> p.nestedProperties(
                        Property.text("marco"))),
                Property.objectArray("prop_object_array",
                    p -> p.nestedProperties(
                        Property.text("marco")))));

    var types = client.collections.use(nsDataTypes);

    var now = OffsetDateTime.now();
    var uuid = UUID.randomUUID();

    Map<String, Object> want = Map.ofEntries(
        Map.entry("prop_text", "Hello, World!"),
        Map.entry("prop_integer", 1L),
        Map.entry("prop_number", 1D),
        Map.entry("prop_bool", true),
        Map.entry("prop_date", now),
        Map.entry("prop_uuid", uuid),
        Map.entry("prop_integer_array", List.of(1L, 2L, 3L)),
        Map.entry("prop_number_array", List.of(1D, 2D, 3D)),
        Map.entry("prop_bool_array", List.of(true, false)),
        Map.entry("prop_date_array", List.of(now, now)),
        Map.entry("prop_uuid_array", List.of(uuid, uuid)),
        Map.entry("prop_text_array", List.of("a", "b", "c")),
        Map.entry("prop_phone_number", PhoneNumber.international("+380 95 1433336")),
        Map.entry("prop_geo_coordinates", new GeoCoordinates(1f, 2f)),
        Map.entry("prop_object", Map.of("marco", "polo")),
        Map.entry("prop_object_array", List.of(Map.of("marco", "polo"))));

    // Act
    var object = types.data.insert(want);
    var got = types.query.fetchObjectById(object.uuid()); // return all properties

    // Assert
    Assertions.assertThat(got).get()
        .extracting(WeaviateObject::properties)
        .asInstanceOf(InstanceOfAssertFactories.map(String.class, Object.class))
        // Most of PhoneNumber fields are only present on read and are null on write.
        .usingRecursiveComparison()
        .withComparatorForType(ORMITest::comparePhoneNumbers, PhoneNumber.class)
        .isEqualTo(want);
  }

  record Address(
      String street,
      @io.weaviate.client6.v1.api.collections.annotations.Property("building_nr") int buildingNr,
      @io.weaviate.client6.v1.api.collections.annotations.Property("isOneWay") boolean oneWay) {
  }

  @Test
  public void testNestedProperties_insertMany() throws IOException {
    // Arrange
    var nsBuildings = ns("Buildings");

    client.collections.create(
        nsBuildings, c -> c.properties(
            Property.object("address", p -> p.nestedProperties(
                Property.text("street"),
                Property.integer("building_nr"),
                Property.bool("isOneWay"))),
            Property.objectArray("apartments", p -> p.nestedProperties(
                Property.integer("door_nr"),
                Property.number("area")))));

    var buildings = client.collections.use(nsBuildings);

    Map<String, Object> house_1 = Map.of(
        "address", Map.of(
            "street", "Burggasse",
            "building_nr", 51,
            "isOneWay", true),
        "apartments", List.of(
            Map.of("door_nr", 11, "area", 42.2),
            Map.of("door_nr", 12, "area", 26.7)));
    Map<String, Object> house_2 = Map.of(
        "address", new Address(
            "Port Mariland St.",
            111,
            false),
        "apartments", new Map[] {
            Map.of("door_nr", 21, "area", 42.2),
            Map.of("door_nr", 22, "area", 26.7),
        });

    // Act
    var result = buildings.data.insertMany(house_1, house_2);

    // Assert
    Assertions.assertThat(result.errors()).isEmpty();
  }

  @Ignore("Making Emails collection multi-tenant causes ReferencesITest::testNestedReferences to fail")
  @Test
  public void test_multiTenant() throws IOException {
    // Arrange
    var nsEmails = ns("Emails");
    var emails = client.collections.create(nsEmails,
        c -> c.multiTenancy(mt -> mt.enabled(true)));

    var johndoe = "john-doe";
    emails.tenants.create(Tenant.active(johndoe));
    emails = emails.withTenant(johndoe);

    // Act
    var inserted = emails.data.insert(Map.of("subject", "McDonald's Xmas Bonanza"));

    // Assert
    Assertions.assertThat(inserted).returns(johndoe, WeaviateObject::tenant);
  }
}
