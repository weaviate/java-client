package io.weaviate.integration;

import java.io.IOException;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.Test;

import io.weaviate.ConcurrentTest;
import io.weaviate.client6.v1.api.WeaviateClient;
import io.weaviate.client6.v1.api.collections.Property;
import io.weaviate.client6.v1.api.collections.VectorIndex;
import io.weaviate.client6.v1.api.collections.WeaviateCollection;
import io.weaviate.client6.v1.api.collections.vectorindex.Hnsw;
import io.weaviate.client6.v1.api.collections.vectorizers.NoneVectorizer;
import io.weaviate.containers.Container;

public class CollectionsITest extends ConcurrentTest {
  private static WeaviateClient client = Container.WEAVIATE.getClient().apiClient();

  @Test
  public void testCreateGetDelete() throws IOException {
    var collectionName = ns("Things");
    client.collections.create(collectionName,
        col -> col
            .properties(Property.text("username"), Property.integer("age"))
            .vector(Hnsw.of(new NoneVectorizer())));

    var thingsCollection = client.collections.getConfig(collectionName);

    Assertions.assertThat(thingsCollection).get()
        .hasFieldOrPropertyWithValue("name", collectionName)
        .extracting(WeaviateCollection::vectors, InstanceOfAssertFactories.map(String.class, VectorIndex.class))
        .as("default vector").extractingByKey("default")
        .satisfies(defaultVector -> {
          Assertions.assertThat(defaultVector).extracting(VectorIndex::vectorizer)
              .as("has none vectorizer").isInstanceOf(NoneVectorizer.class);
          Assertions.assertThat(defaultVector).extracting(VectorIndex::config)
              .isInstanceOf(Hnsw.class);
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
        col -> col.references(Property.reference("ownedBy", nsOwners)));
    var things = client.collections.use(nsThings);

    // Assert: Things --ownedBy-> Owners
    Assertions.assertThat(things.config.get())
        .as("after create Things").get()
        .satisfies(c -> {
          Assertions.assertThat(c.references())
              .as("ownedBy").filteredOn(p -> p.name().equals("ownedBy")).first()
              .extracting(p -> p.dataTypes(), InstanceOfAssertFactories.LIST)
              .containsOnly(nsOwners);
        });

    // Arrange: Create OnlineStores and Markets collections
    var nsOnlineStores = ns("OnlineStores");
    client.collections.create(nsOnlineStores);

    var nsMarkets = ns("Markets");
    client.collections.create(nsMarkets);

    // Act: Update Things collections to add polymorphic reference
    things.config.addReference("soldIn", nsOnlineStores, nsMarkets);

    // Assert: Things --soldIn-> [OnlineStores, Markets]
    Assertions.assertThat(things.config.get())
        .as("after add property").get()
        .satisfies(c -> {
          Assertions.assertThat(c.references())
              .as("soldIn").filteredOn(p -> p.name().equals("soldIn")).first()
              .extracting(p -> p.dataTypes(), InstanceOfAssertFactories.LIST)
              .containsOnly(nsOnlineStores, nsMarkets);
        });
  }
}
