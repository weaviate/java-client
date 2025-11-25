package io.weaviate.integration;

import static org.junit.Assume.assumeTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import org.assertj.core.api.Assertions;
import org.junit.Assume;
import org.junit.Test;

import io.weaviate.ConcurrentTest;
import io.weaviate.client6.v1.api.WeaviateClient;
import io.weaviate.client6.v1.api.WeaviateException;
import io.weaviate.client6.v1.api.collections.Property;
import io.weaviate.client6.v1.api.collections.WeaviateObject;
import io.weaviate.client6.v1.api.collections.pagination.PaginationException;
import io.weaviate.client6.v1.api.collections.query.Metadata;
import io.weaviate.containers.Container;

public class PaginationITest extends ConcurrentTest {
  private static final WeaviateClient client = Container.WEAVIATE.getClient();

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
      inserted.add(object.uuid());
    }
    assumeTrue("all objects were inserted", inserted.size() == count);

    var allThings = things.paginate();

    // Act: stream
    var gotStream = allThings.stream().map(WeaviateObject::uuid).toList();

    // Assert
    Assertions.assertThat(gotStream)
        .as("stream fetched all objects")
        .hasSize(inserted.size())
        .containsAll(inserted);

    // Act: for-loop
    var gotLoop = new ArrayList<String>();
    for (var thing : allThings) {
      gotLoop.add(thing.uuid());
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
      inserted.add(object.uuid());
    }

    // Iterate over first 5 objects
    String lastId = things.paginate(p -> p.pageSize(5)).stream()
        .limit(5).map(thing -> thing.uuid())
        .reduce((prev, next) -> next).get();

    // Act
    var remaining = things.paginate(p -> p.fromCursor(lastId)).stream().count();

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
      inserted.add(object.uuid());
    }

    // Act / Assert
    var withSomeProperties = things.paginate(p -> p
        .returnMetadata(Metadata.CREATION_TIME_UNIX)
        .returnProperties("fetch_me"));
    for (var thing : withSomeProperties) {
      Assertions.assertThat(thing.properties())
          .as("uuid=" + thing.uuid())
          .doesNotContainKey("dont_fetch");

      Assertions.assertThat(thing.createdAt()).isNotNull();
    }
  }

  @Test
  public void testAsyncPaginator() throws Exception, InterruptedException, ExecutionException {
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
            .thenAccept(object -> inserted.add(object.uuid()));
      }
      CompletableFuture.allOf(futures).get();

      // Act
      var objectCount = new AtomicInteger();
      var countAll = things.paginate(p -> p.pageSize(5).prefetch(true))
          .forEach(__ -> objectCount.getAndIncrement());

      // Assert
      if (!countAll.isDone()) {
        Assume.assumeTrue("iteration not completed", objectCount.get() < count);
      }

      countAll.get(); // Wait for it to complete.
      Assertions.assertThat(objectCount.get())
          .as("object count after iteration completed")
          .isEqualTo(count);
    }
  }

  @Test(expected = PaginationException.class)
  public void testFailedPagination() throws IOException {
    var things = client.collections.use("Unknown");
    things.paginate().forEach(System.out::println);
  }

  @Test(expected = PaginationException.class)
  public void testFailedAsyncPagination_forEach() throws Throwable {
    try (final var async = client.async()) {
      var things = async.collections.use("Unknown");
      try {
        things.paginate().forEach(__ -> System.out.println("called once")).join();
      } catch (CompletionException e) {
        throw e.getCause(); // CompletableFuture exceptions are always wrapped
      }
    }
  }

  @Test(expected = WeaviateException.class)
  public void testFailedAsyncPagination_forPage() throws Throwable {
    try (final var async = client.async()) {
      var things = async.collections.use("Unknown");
      try {
        things.paginate().forPage(__ -> System.out.println("called once")).join();
      } catch (CompletionException e) {
        throw e.getCause(); // CompletableFuture exceptions are always wrapped
      }
    }
  }
}
