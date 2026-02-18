package io.weaviate.integration;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.junit.BeforeClass;
import org.junit.Test;

import io.weaviate.ConcurrentTest;
import io.weaviate.client6.v1.api.WeaviateClient;
import io.weaviate.client6.v1.api.collections.Property;
import io.weaviate.client6.v1.api.collections.WeaviateObject;
import io.weaviate.containers.Container;
import io.weaviate.containers.Weaviate;

public class BatchITest extends ConcurrentTest {
  private static final WeaviateClient client = Container.WEAVIATE.getClient();

  @BeforeClass
  public static void __() {
    Weaviate.Version.V136.orSkip();
  }

  @Test
  public void test() throws IOException {
    var nsThings = ns("Things");

    var things = client.collections.create(
        nsThings,
        c -> c.properties(Property.text("letter")));

    // Act
    try (var batch = things.batch.start()) {
      for (int i = 0; i < 10_000; i++) {
        String uuid = UUID.randomUUID().toString();
        batch.add(WeaviateObject.of(builder -> builder
            .uuid(uuid)
            .properties(Map.of("letter", uuid.substring(0, 1)))));
      }
    } catch (InterruptedException e) {
    }

    // Assert
    Assertions.assertThat(things.size()).isEqualTo(10_000);
  }
}
