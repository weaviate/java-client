package io.weaviate.integration;

import java.io.IOException;

import org.assertj.core.api.Assertions;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import io.weaviate.ConcurrentTest;
import io.weaviate.client6.v1.api.WeaviateClient;
import io.weaviate.client6.v1.api.collections.WeaviateObject;
import io.weaviate.client6.v1.api.collections.batch.BatchContext;
import io.weaviate.containers.Container;
import io.weaviate.containers.Weaviate;

@Ignore
public class BatchITest extends ConcurrentTest {
  private static final WeaviateClient client = Container.WEAVIATE.getClient();

  @BeforeClass
  public static void __() {
    Weaviate.Version.V136.orSkip();
  }

  @Test
  public void test10_000Objects() throws IOException {
    var nsThings = ns("Things");

    var things = client.collections.create(nsThings);

    // Act
    try (BatchContext<?> batch = things.batch.start()) {
      for (int i = 0; i < 10_000; i++) {
        batch.add(WeaviateObject.of());
      }
    } catch (InterruptedException e) {
    }

    // Assert
    Assertions.assertThat(things.size()).isEqualTo(10_000);
  }
}
