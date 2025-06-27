package io.weaviate.integration;

import static org.junit.Assume.assumeTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import io.weaviate.ConcurrentTest;
import io.weaviate.client6.v1.api.WeaviateClient;
import io.weaviate.client6.v1.api.collections.WeaviateMetadata;
import io.weaviate.client6.v1.api.collections.WeaviateObject;
import io.weaviate.containers.Container;

public class PaginationITest extends ConcurrentTest {
  private static WeaviateClient client = Container.WEAVIATE.getClient();

  @Test
  public void test_stream() throws IOException {
    // Arrange
    var nsThings = ns("Things");
    var count = 10;

    client.collections.create(nsThings);
    var things = client.collections.use(nsThings);

    var inserted = new ArrayList<String>();
    for (var i = 0; i < count; i++) {
      var object = things.data.insert(Collections.emptyMap());
      inserted.add(object.metadata().uuid());
    }
    assumeTrue("all objects were inserted", inserted.size() == count);

    // Act: stream
    var gotStream = things.stream()
        .map(WeaviateObject::metadata).map(WeaviateMetadata::uuid).toList();

    // Assert
    Assertions.assertThat(gotStream)
        .as("stream fetched all objects")
        .hasSize(inserted.size())
        .containsAll(inserted);

    // Act: list
    var gotList = new ArrayList<String>();
    for (var object : things.list()) {
      gotList.add(object.metadata().uuid());
    }

    // Assert
    Assertions.assertThat(gotList)
        .as("list fetched all objects")
        .hasSize(inserted.size())
        .containsAll(inserted);

    Assertions.assertThat(gotStream)
        .as("stream and list return consistent order")
        .containsExactlyElementsOf(gotList);
  }
}
