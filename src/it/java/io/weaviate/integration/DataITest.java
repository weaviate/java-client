package io.weaviate.integration;

import java.io.IOException;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.BeforeClass;
import org.junit.Test;

import io.weaviate.ConcurrentTest;
import io.weaviate.client6.WeaviateClient;
import io.weaviate.client6.v1.collections.Property;
import io.weaviate.client6.v1.collections.VectorIndex;
import io.weaviate.client6.v1.collections.VectorIndex.IndexingStrategy;
import io.weaviate.client6.v1.collections.Vectorizer;
import io.weaviate.client6.v1.collections.object.Vectors;
import io.weaviate.client6.v1.collections.object.WeaviateObject;
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

    artists.data.insert(Map.of("name", "john doe"), metadata -> metadata
        .id(id)
        .vectors(Vectors.of(VECTOR_INDEX, vector)));

    var object = artists.data.get(id, query -> query
        .returnProperties("name")
        .includeVector());

    Assertions.assertThat(object)
        .as("object exists after insert").get()
        .satisfies(obj -> {
          Assertions.assertThat(obj.metadata().id())
              .as("object id").isEqualTo(id);

          Assertions.assertThat(obj.metadata().vectors()).extracting(Vectors::getSingle)
              .asInstanceOf(InstanceOfAssertFactories.OPTIONAL).as("has single vector").get()
              .asInstanceOf(InstanceOfAssertFactories.array(Float[].class)).containsExactly(vector);

          Assertions.assertThat(obj.properties())
              .as("has expected properties")
              .containsEntry("name", "john doe");
        });

    artists.data.delete(id);
    object = artists.data.get(id);
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

    var got = cats.data.get(ragdoll.metadata().id(),
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
            .vector(VECTOR_INDEX, new VectorIndex<>(IndexingStrategy.hnsw(), Vectorizer.none())));
  }
}
