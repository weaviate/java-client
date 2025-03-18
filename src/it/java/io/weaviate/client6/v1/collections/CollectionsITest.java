package io.weaviate.client6.v1.collections;

import java.io.IOException;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import io.weaviate.ConcurrentTest;
import io.weaviate.client6.WeaviateClient;
import io.weaviate.client6.v1.collections.VectorIndex.IndexType;
import io.weaviate.client6.v1.collections.VectorIndex.IndexingStrategy;
import io.weaviate.containers.Container;

public class CollectionsITest extends ConcurrentTest {
  private static WeaviateClient client = Container.WEAVIATE.getClient();

  @Test
  public void testCreateGetDelete() throws IOException {
    var collectionName = ns("Things_1");
    client.collections.create(collectionName,
        col -> col
            .properties(Property.text("username"), Property.integer("age"))
            .vector(new VectorIndex<>(IndexingStrategy.hnsw(), Vectorizer.none())));

    var thingsCollection = client.collections.getConfig(collectionName);

    Assertions.assertThat(thingsCollection).get()
        .hasFieldOrPropertyWithValue("name", collectionName)
        .extracting(CollectionDefinition::vectors).extracting(Vectors::getDefault)
        .as("default vector").satisfies(defaultVector -> {
          Assertions.assertThat(defaultVector).extracting(VectorIndex::vectorizer)
              .as("has none vectorizer").isInstanceOf(NoneVectorizer.class);
          Assertions.assertThat(defaultVector).extracting(VectorIndex::configuration)
              .as("has hnsw index").returns(IndexType.HNSW, IndexingStrategy::type);
        });

    client.collections.delete(collectionName);
    var noCollection = client.collections.getConfig(collectionName);
    Assertions.assertThat(noCollection).as("after delete").isEmpty();
  }
}
