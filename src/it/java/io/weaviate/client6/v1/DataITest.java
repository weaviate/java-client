package io.weaviate.client6.v1;

import java.io.IOException;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.BeforeClass;
import org.junit.Test;

import io.weaviate.ConcurrentTest;
import io.weaviate.client6.WeaviateClient;
import io.weaviate.client6.v1.collections.Property;
import io.weaviate.client6.v1.data.Vectors;
import io.weaviate.containers.Container;

public class DataITest extends ConcurrentTest {

  private static WeaviateClient client = Container.WEAVIATE.getClient();
  private static final String COLLECTION = unique("Things");

  @BeforeClass
  public static void beforeAll() throws IOException {
    createTestCollection();
  }

  @Test
  public void testCreateGetDelete() throws IOException {
    var things = client.collections.use(COLLECTION);
    var id = randomUUID();
    Float[] vector = { 1f, 2f, 3f };

    things.data.insert(Map.of("username", "john doe"), metadata -> metadata
        .id(id)
        .vectors(Vectors.of("bring_your_own", vector)));

    var object = things.data.get(id);
    Assertions.assertThat(object)
        .as("object exists after insert").get()
        .satisfies(obj -> {
          Assertions.assertThat(obj.metadata.id)
              .as("object id").isEqualTo(id);

          Assertions.assertThat(obj.metadata.vectors).extracting(Vectors::getSingle)
              .asInstanceOf(InstanceOfAssertFactories.OPTIONAL).as("has single vector").get()
              .asInstanceOf(InstanceOfAssertFactories.array(Float[].class)).containsExactly(vector);

          Assertions.assertThat(obj.properties)
              .as("has expected properties")
              .containsEntry("username", "john doe");
        });

    things.data.delete(id);
    object = things.data.get(id);
    Assertions.assertThat(object).isEmpty().as("object not exists after deletion");
  }

  private static void createTestCollection() throws IOException {
    client.collections.create(COLLECTION,
        col -> col
            .properties(Property.text("username"))
            .vector("bring_your_own"));
  }
}
