package io.weaviate.integration;

import java.io.IOException;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.BeforeClass;
import org.junit.Test;

import io.weaviate.ConcurrentTest;
import io.weaviate.client6.v1.api.WeaviateClient;
import io.weaviate.client6.v1.api.collections.Property;
import io.weaviate.client6.v1.api.collections.Vectors;
import io.weaviate.client6.v1.api.collections.WeaviateObject;
import io.weaviate.client6.v1.api.collections.data.Reference;
import io.weaviate.client6.v1.api.collections.query.Metadata;
import io.weaviate.client6.v1.api.collections.query.QueryMetadata;
import io.weaviate.client6.v1.api.collections.query.QueryReference;
import io.weaviate.client6.v1.api.collections.vectorindex.Hnsw;
import io.weaviate.client6.v1.api.collections.vectorizers.NoneVectorizer;
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
    Float[] vector = { 1f, 2f, 3f };

    artists.data.insert(Map.of("name", "john doe"),
        metadata -> metadata
            .uuid(id)
            .vectors(Vectors.of(VECTOR_INDEX, vector)));

    var object = artists.query.byId(id, query -> query
        .returnProperties("name")
        .returnMetadata(Metadata.ID, Metadata.VECTOR));

    Assertions.assertThat(artists.data.exists(id))
        .as("object exists after insert").isTrue();
    Assertions.assertThat(object)
        .as("object has correct properties").get()
        .satisfies(obj -> {
          Assertions.assertThat(obj.metadata().uuid())
              .as("object id").isEqualTo(id);

          Assertions.assertThat(obj.metadata().vectors()).extracting(v -> v.getSingle(VECTOR_INDEX))
              .asInstanceOf(InstanceOfAssertFactories.array(Float[].class)).containsExactly(vector);

          Assertions.assertThat(obj.properties())
              .as("has expected properties")
              .containsEntry("name", "john doe");
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
            .vectors(named -> named.vector(VECTOR_INDEX, Hnsw.of(new NoneVectorizer()))));
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
  public void testReplace() {
    // Replace (PUT):
    // properties, reference, vectors
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
            .vector(Hnsw.of(new NoneVectorizer())));

    var authors = client.collections.use(nsAuthors);
    var walter = authors.data.insert(Map.of("name", "walter scott"));

    var vector = new Float[] { 1f, 2f, 3f };

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
                    writtenBy -> writtenBy.returnMetadata(Metadata.ID))));

    Assertions.assertThat(updIvanhoe).get()
        .satisfies(book -> {
          Assertions.assertThat(book)
              .as("has year property")
              .extracting(WeaviateObject::properties, InstanceOfAssertFactories.MAP)
              .contains(Map.entry("year", 1819L));

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
}
