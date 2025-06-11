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
import io.weaviate.client6.v1.api.collections.query.MetadataField;
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
        .returnMetadata(MetadataField.ID, MetadataField.VECTOR));

    Assertions.assertThat(object)
        .as("object exists after insert").get()
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
    object = artists.query.byId(id);
    Assertions.assertThat(object).isEmpty().as("object not exists after deletion");
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
}
