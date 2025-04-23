package io.weaviate.integration;

import java.io.IOException;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.Test;

import io.weaviate.ConcurrentTest;
import io.weaviate.client6.WeaviateClient;
import io.weaviate.client6.v1.collections.Collection;
import io.weaviate.client6.v1.collections.NoneVectorizer;
import io.weaviate.client6.v1.collections.Property;
import io.weaviate.client6.v1.collections.VectorIndex;
import io.weaviate.client6.v1.collections.VectorIndex.IndexType;
import io.weaviate.client6.v1.collections.VectorIndex.IndexingStrategy;
import io.weaviate.client6.v1.collections.Vectorizer;
import io.weaviate.client6.v1.collections.Vectors;
import io.weaviate.containers.Container;

public class CollectionsITest extends ConcurrentTest {
  private static WeaviateClient client = Container.WEAVIATE.getClient();

  @Test
  public void testCreateGetDelete() throws IOException {
    var collectionName = ns("Things");
    client.collections.create(collectionName,
        col -> col
            .properties(Property.text("username"), Property.integer("age"))
            .vector(new VectorIndex<>(IndexingStrategy.hnsw(), Vectorizer.none())));

    var thingsCollection = client.collections.getConfig(collectionName);

    Assertions.assertThat(thingsCollection).get()
        .hasFieldOrPropertyWithValue("name", collectionName)
        .extracting(Collection::vectors).extracting(Vectors::getDefault)
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

  @Test
  public void testCrossReferences() throws IOException {
    // Arrange: Create Owners collection
    var nsOwners = ns("Owners");
    client.collections.create(nsOwners);

    // Act: Create Things collection with owner -> owners
    var nsThings = ns("Things");
    client.collections.create(nsThings,
        col -> col.properties(Property.reference("ownedBy", nsOwners)));
    var things = client.collections.use(nsThings);

    // Assert: Things --ownedBy-> Owners
    Assertions.assertThat(things.config.get())
        .as("after create Things").get()
        .satisfies(c -> {
          Assertions.assertThat(c.properties())
              .as("ownedBy").filteredOn(p -> p.name().equals("ownedBy")).first()
              .extracting(p -> p.dataTypes()).asInstanceOf(InstanceOfAssertFactories.LIST)
              .containsOnly(nsOwners);
        });

    // Arrange: Create OnlineStores and Markets collections
    var nsOnlineStores = ns("OnlineStores");
    client.collections.create(nsOnlineStores);

    var nsMarkets = ns("Markets");
    client.collections.create(nsMarkets);

    // Act: Update Things collections to add polymorphic reference
    things.config.addProperty(Property.reference("soldIn", nsOnlineStores, nsMarkets));

    // Assert: Things --soldIn-> [OnlineStores, Markets]
    Assertions.assertThat(things.config.get())
        .as("after add property").get()
        .satisfies(c -> {
          Assertions.assertThat(c.properties())
              .as("soldIn").filteredOn(p -> p.name().equals("soldIn")).first()
              .extracting(p -> p.dataTypes()).asInstanceOf(InstanceOfAssertFactories.LIST)
              .containsOnly(nsOnlineStores, nsMarkets);
        });
  }
}
