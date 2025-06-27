package io.weaviate.integration;

import java.io.IOException;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.Test;

import io.weaviate.ConcurrentTest;
import io.weaviate.client6.v1.api.WeaviateClient;
import io.weaviate.client6.v1.api.collections.CollectionConfig;
import io.weaviate.client6.v1.api.collections.InvertedIndex;
import io.weaviate.client6.v1.api.collections.Property;
import io.weaviate.client6.v1.api.collections.Replication;
import io.weaviate.client6.v1.api.collections.VectorIndex;
import io.weaviate.client6.v1.api.collections.vectorindex.Hnsw;
import io.weaviate.client6.v1.api.collections.vectorizers.NoneVectorizer;
import io.weaviate.containers.Container;

public class CollectionsITest extends ConcurrentTest {
  private static WeaviateClient client = Container.WEAVIATE.getClient();

  @Test
  public void testCreateGetDelete() throws IOException {
    var collectionName = ns("Things");
    client.collections.create(collectionName,
        col -> col
            .properties(Property.text("username"), Property.integer("age"))
            .vector(Hnsw.of(new NoneVectorizer())));

    var thingsCollection = client.collections.getConfig(collectionName);

    Assertions.assertThat(thingsCollection).get()
        .hasFieldOrPropertyWithValue("collectionName", collectionName)
        .extracting(CollectionConfig::vectors, InstanceOfAssertFactories.map(String.class, VectorIndex.class))
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
              .as("ownedBy").filteredOn(p -> p.propertyName().equals("ownedBy")).first()
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
              .as("soldIn").filteredOn(p -> p.propertyName().equals("soldIn")).first()
              .extracting(p -> p.dataTypes(), InstanceOfAssertFactories.LIST)
              .containsOnly(nsOnlineStores, nsMarkets);
        });
  }

  @Test
  public void testListDeleteAll() throws IOException {
    var nsA = ns("A");
    var nsB = ns("B");
    var nsC = ns("C");

    client.collections.create(nsA);
    client.collections.create(nsB);
    client.collections.create(nsC);

    Assertions.assertThat(client.collections.exists(nsA)).isTrue();
    Assertions.assertThat(client.collections.exists(nsB)).isTrue();
    Assertions.assertThat(client.collections.exists(nsC)).isTrue();
    Assertions.assertThat(client.collections.exists(ns("X"))).isFalse();

    var all = client.collections.list();
    Assertions.assertThat(all)
        .hasSizeGreaterThanOrEqualTo(3)
        .extracting(CollectionConfig::collectionName)
        .contains(nsA, nsB, nsC);

    client.collections.deleteAll();

    all = client.collections.list();
    Assertions.assertThat(all.isEmpty());

  }

  @Test
  public void testUpdateCollection() throws IOException {
    var nsBoxes = ns("Boxes");
    var nsThings = ns("Things");

    client.collections.create(nsBoxes);

    client.collections.create(nsThings,
        collection -> collection
            .description("Things stored in boxes")
            .properties(
                Property.text("name"),
                Property.integer("width",
                    w -> w.description("how wide this thing is")))
            .invertedIndex(idx -> idx.cleanupIntervalSeconds(10))
            .replication(repl -> repl.asyncEnabled(true)));

    var things = client.collections.use(nsThings);

    // Act
    things.config.update(nsThings, collection -> collection
        .description("Things stored on shelves")
        .propertyDescription("width", "not height")
        .invertedIndex(idx -> idx.cleanupIntervalSeconds(30))
        .replication(repl -> repl.asyncEnabled(false)));

    // Assert
    var updated = things.config.get();
    Assertions.assertThat(updated).get()
        .returns("Things stored on shelves", CollectionConfig::description)
        .satisfies(collection -> {
          Assertions.assertThat(collection)
              .extracting(CollectionConfig::properties, InstanceOfAssertFactories.list(Property.class))
              .extracting(Property::description).contains("not height");

          Assertions.assertThat(collection)
              .extracting(CollectionConfig::invertedIndex).returns(30, InvertedIndex::cleanupIntervalSeconds);

          Assertions.assertThat(collection)
              .extracting(CollectionConfig::replication).returns(false, Replication::asyncEnabled);
        });
  }
}
