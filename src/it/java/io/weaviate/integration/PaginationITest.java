package io.weaviate.integration;

import static org.junit.Assume.assumeTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import io.weaviate.ConcurrentTest;
import io.weaviate.client6.v1.api.WeaviateClient;
import io.weaviate.client6.v1.api.collections.Property;
import io.weaviate.client6.v1.api.collections.WeaviateMetadata;
import io.weaviate.client6.v1.api.collections.WeaviateObject;
import io.weaviate.containers.Container;

public class PaginationITest extends ConcurrentTest {
  private static WeaviateClient client = Container.WEAVIATE.getClient();

  @Test
  public void testIterateAll() throws IOException {
    // Arrange
    var nsThings = ns("Things");
    var count = 150;

    client.collections.create(nsThings);
    var things = client.collections.use(nsThings);

    var inserted = new ArrayList<String>();
    for (var i = 0; i < count; i++) {
      var object = things.data.insert(Collections.emptyMap());
      inserted.add(object.metadata().uuid());
    }
    assumeTrue("all objects were inserted", inserted.size() == count);

    var allThings = things.paginate();

    // Act: stream
    var gotStream = allThings.stream()
        .map(WeaviateObject::metadata).map(WeaviateMetadata::uuid).toList();

    // Assert
    Assertions.assertThat(gotStream)
        .as("stream fetched all objects")
        .hasSize(inserted.size())
        .containsAll(inserted);

    // Act: for-loop
    var gotLoop = new ArrayList<String>();
    for (var thing : allThings) {
      gotLoop.add(thing.metadata().uuid());
    }

    // Assert
    Assertions.assertThat(gotLoop)
        .as("list fetched all objects")
        .hasSize(inserted.size())
        .containsAll(inserted);

    Assertions.assertThat(gotStream)
        .as("stream and list return consistent order")
        .containsExactlyElementsOf(gotLoop);
  }

  @Test
  public void testResumePagination() throws IOException {
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

    // Iterate over first 5 objects
    String lastId = things.paginate(p -> p.pageSize(5)).stream()
        .limit(5).map(thing -> thing.metadata().uuid())
        .reduce((prev, next) -> next).get();

    // Act
    var remaining = things.paginate(p -> p.resumeFrom(lastId)).stream().count();

    // Assert
    Assertions.assertThat(remaining).isEqualTo(5);
  }

  @Test
  public void testWithQueryOptions() throws IOException {
    // Arrange
    var nsThings = ns("Things");
    var count = 10;

    client.collections.create(nsThings,
        c -> c.properties(
            Property.text("fetch_me"),
            Property.integer("dont_fetch")));

    var things = client.collections.use(nsThings);
    var inserted = new ArrayList<String>();
    for (var i = 0; i < count; i++) {
      var object = things.data.insert(Collections.emptyMap());
      inserted.add(object.metadata().uuid());
    }

    // Act / Assert
    var withSomeProperties = things.paginate(p -> p.returnProperties("fetch_me"));
    for (var thing : withSomeProperties) {
      Assertions.assertThat(thing.properties())
          .as("uuid=" + thing.metadata().uuid())
          .doesNotContainKey("dont_fetch");
    }
  }

  @Test
  public void testStreamAsync() throws IOException, InterruptedException, ExecutionException {
    // Arrange
    var nsThings = ns("Things");
    var count = 10;

    client.collections.create(nsThings);

    try (final var async = client.async()) {
      var things = async.collections.use(nsThings);

      var futures = new CompletableFuture<?>[count];
      var inserted = new ArrayList<String>();
      for (var i = 0; i < count; i++) {
        futures[i] = things.data.insert(Collections.emptyMap())
            .thenAccept(object -> inserted.add(object.metadata().uuid()));
      }
      CompletableFuture.allOf(futures).get();

      var asyncPaginator = things.paginate();
    }
  }
}
