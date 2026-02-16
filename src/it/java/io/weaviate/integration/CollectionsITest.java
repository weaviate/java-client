package io.weaviate.integration;

import java.io.IOException;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.Test;

import io.weaviate.ConcurrentTest;
import io.weaviate.client6.v1.api.WeaviateApiException;
import io.weaviate.client6.v1.api.WeaviateClient;
import io.weaviate.client6.v1.api.collections.CollectionConfig;
import io.weaviate.client6.v1.api.collections.DataType;
import io.weaviate.client6.v1.api.collections.Generative;
import io.weaviate.client6.v1.api.collections.InvertedIndex;
import io.weaviate.client6.v1.api.collections.ObjectTtl;
import io.weaviate.client6.v1.api.collections.Property;
import io.weaviate.client6.v1.api.collections.Quantization;
import io.weaviate.client6.v1.api.collections.ReferenceProperty;
import io.weaviate.client6.v1.api.collections.Replication;
import io.weaviate.client6.v1.api.collections.VectorConfig;
import io.weaviate.client6.v1.api.collections.config.PropertyIndexType;
import io.weaviate.client6.v1.api.collections.config.Shard;
import io.weaviate.client6.v1.api.collections.config.ShardStatus;
import io.weaviate.client6.v1.api.collections.generative.DummyGenerative;
import io.weaviate.client6.v1.api.collections.query.BaseQueryOptions;
import io.weaviate.client6.v1.api.collections.vectorindex.Hnsw;
import io.weaviate.client6.v1.api.collections.vectorizers.SelfProvidedVectorizer;
import io.weaviate.containers.Container;
import io.weaviate.containers.Weaviate;

public class CollectionsITest extends ConcurrentTest {
  private static WeaviateClient client = Container.WEAVIATE.getClient();

  @Test
  public void testCreateGetDelete() throws IOException {
    var collectionName = ns("Things");
    client.collections.create(collectionName,
        col -> col
            .properties(Property.text("username"), Property.integer("age"))
            .vectorConfig(VectorConfig.selfProvided()));

    var thingsCollection = client.collections.getConfig(collectionName);

    Assertions.assertThat(thingsCollection).get()
        .hasFieldOrPropertyWithValue("collectionName", collectionName)
        .extracting(CollectionConfig::vectors, InstanceOfAssertFactories.map(String.class, VectorConfig.class))
        .as("default vector").extractingByKey("default")
        .satisfies(defaultVector -> {
          Assertions.assertThat(defaultVector)
              .as("has none vectorizer").isInstanceOf(SelfProvidedVectorizer.class);
          Assertions.assertThat(defaultVector).extracting(VectorConfig::vectorIndex)
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
        col -> col.references(ReferenceProperty.to("ownedBy", nsOwners)));
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
  public void testListDeleteAll() throws Exception {
    // Use a separate container for this test so as not to interfere
    // with other tests.
    try (final var _client = Weaviate.createDefault().getBareClient()) {
      var nsA = ns("A");
      var nsB = ns("B");
      var nsC = ns("C");

      _client.collections.create(nsA);
      _client.collections.create(nsB);
      _client.collections.create(nsC);

      Assertions.assertThat(_client.collections.exists(nsA)).isTrue();
      Assertions.assertThat(_client.collections.exists(nsB)).isTrue();
      Assertions.assertThat(_client.collections.exists(nsC)).isTrue();
      Assertions.assertThat(_client.collections.exists(ns("X"))).isFalse();

      var all = _client.collections.list();
      Assertions.assertThat(all)
          .hasSizeGreaterThanOrEqualTo(3)
          .extracting(CollectionConfig::collectionName)
          .contains(nsA, nsB, nsC);

      _client.collections.deleteAll();

      all = _client.collections.list();
      Assertions.assertThat(all.isEmpty());
    }
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
    things.config.update(c -> c
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

  @Test
  public void testShards() throws IOException {
    var nsShatteredCups = ns("ShatteredCups");
    client.collections.create(nsShatteredCups);
    var cups = client.collections.use(nsShatteredCups);

    // Act: get initial shard state
    var shards = cups.config.getShards();

    Assertions.assertThat(shards).as("single-tenant collections has 1 shard").hasSize(1);
    var singleShard = shards.get(0);

    // Act: flip the status
    var wantStatus = singleShard.status().equals("READY") ? ShardStatus.READONLY : ShardStatus.READY;
    var updated = cups.config.updateShards(wantStatus, singleShard.name());

    Assertions.assertThat(updated)
        .as("shard status changed")
        .hasSize(1)
        .extracting(Shard::status)
        .containsOnly(wantStatus.name());
  }

  @Test(expected = WeaviateApiException.class)
  public void testInvalidCollectionName() throws IOException {
    client.collections.create("^collection@weaviate.io$");
  }

  @Test
  public void testNestedProperties() throws IOException {
    var nsBuildings = ns("Buildings");

    client.collections.create(
        nsBuildings, c -> c.properties(
            Property.object("address", p -> p.nestedProperties(
                Property.text("street"),
                Property.integer("building_nr"),
                Property.bool("isOneWay"))),
            Property.objectArray("apartments", p -> p.nestedProperties(
                Property.integer("door_nr"),
                Property.number("area")))));

    var config = client.collections.getConfig(nsBuildings);

    var properties = Assertions.assertThat(config).get()
        .extracting(CollectionConfig::properties, InstanceOfAssertFactories.list(Property.class))
        .hasSize(2).actual();

    Assertions.assertThat(properties.get(0))
        .returns("address", Property::propertyName)
        .returns(DataType.OBJECT, p -> p.dataTypes().get(0))
        .extracting(Property::nestedProperties, InstanceOfAssertFactories.list(Property.class))
        .extracting(Property::dataTypes).extracting(types -> types.get(0))
        .containsExactly(DataType.TEXT, DataType.INT, DataType.BOOL);

    Assertions.assertThat(properties.get(1))
        .returns("apartments", Property::propertyName)
        .returns(DataType.OBJECT_ARRAY, p -> p.dataTypes().get(0))
        .extracting(Property::nestedProperties, InstanceOfAssertFactories.list(Property.class))
        .extracting(Property::dataTypes).extracting(types -> types.get(0))
        .containsExactly(DataType.INT, DataType.NUMBER);
  }

  @Test
  public void test_updateQuantization_uncompressed() throws IOException {
    // Arrange
    var nsThings = ns("Things");

    var things = client.collections.create(nsThings,
        c -> c.vectorConfig(VectorConfig.selfProvided(
            self -> self.quantization(Quantization.uncompressed()))));

    // Act
    things.config.update(
        c -> c.vectorConfig(VectorConfig.selfProvided(
            self -> self.quantization(Quantization.bq()))));

    // Assert
    var config = things.config.get();
    Assertions.assertThat(config).get()
        .extracting(CollectionConfig::vectors)
        .extracting("default", InstanceOfAssertFactories.type(VectorConfig.class))
        .extracting(VectorConfig::quantization)
        .returns(Quantization.Kind.BQ, Quantization::_kind);
  }

  @Test
  public void test_updateGenerative() throws IOException {
    // Arrange
    var nsThings = ns("Things");

    var things = client.collections.create(nsThings,
        c -> c.vectorConfig(VectorConfig.selfProvided()));

    // Act
    things.config.update(c -> c.generativeModule(new DummyGenerative()));

    // Assert
    var config = things.config.get();
    Assertions.assertThat(config).get()
        .extracting(CollectionConfig::generativeModule).isNotNull()
        .returns(Generative.Kind.DUMMY, Generative::_kind);
  }

  @Test
  public void test_objectTtl() throws IOException {
    Weaviate.Version.V135.orSkip();

    // Arrange
    var nsThings = ns("Things");

    // Act: create collection
    var things = client.collections.create(nsThings,
        c -> c.objectTtl(ttl -> ttl
            .deleteByCreationTime()
            .defaultTtlSeconds(120)));

    // Assert: correct Object TTL config
    var created = things.config.get();

    Assertions.assertThat(created).get()
        .as("created collection")
        .extracting(CollectionConfig::objectTtl).isNotNull()
        .returns(true, ObjectTtl::enabled)
        .returns(BaseQueryOptions.CREATION_TIME_PROPERTY, ObjectTtl::deleteOn)
        .returns(120, ObjectTtl::defaultTtlSeconds);

    // Act: update TTL config
    things.config.update(
        c -> c.objectTtl(ttl -> ttl
            .deleteByUpdateTime()
            .defaultTtlSeconds(400)));

    // Assert: correct Object TTL config
    var updated = things.config.get();

    Assertions.assertThat(updated).get()
        .as("updated collection")
        .extracting(CollectionConfig::objectTtl).isNotNull()
        .returns(true, ObjectTtl::enabled)
        .returns(BaseQueryOptions.LAST_UPDATE_TIME_PROPERTY, ObjectTtl::deleteOn)
        .returns(400, ObjectTtl::defaultTtlSeconds);

    // Act: disable TTL config
    things.config.update(c -> c.objectTtl(ttl -> ttl.enabled(false)));

    // Assert: correct Object TTL config
    var disabled = things.config.get();

    Assertions.assertThat(disabled).get()
        .as("disabled object TTL")
        .extracting(CollectionConfig::objectTtl).isNotNull()
        .returns(false, ObjectTtl::enabled);
  }

  @Test
  public void test_dropPropertyIndex() throws IOException {
    Weaviate.Version.V136.orSkip();

    // Arrange
    var nsThings = ns("Things");
    var things = client.collections.create(nsThings,
        c -> c.properties(
            Property.text("title", p -> p
                .indexFilterable(true)
                .indexSearchable(true)),
            Property.integer("size", p -> p
                .indexRangeFilters(true))));

    var config = things.config.get();
    Assertions.assertThat(config).get()
        .extracting(CollectionConfig::properties, InstanceOfAssertFactories.list(Property.class))
        .allSatisfy(property -> {
          boolean isNumeric = property.dataTypes().contains(DataType.INT);

          Assertions.assertThat(property)
              .returns(true, Property::indexFilterable)
              .returns(!isNumeric, Property::indexSearchable)
              .returns(isNumeric, Property::indexRangeFilters);
        });

    things.config.dropPropertyIndex("title", PropertyIndexType.FILTERABLE);
    things.config.dropPropertyIndex("title", PropertyIndexType.SEARCHABLE);

    things.config.dropPropertyIndex("size", PropertyIndexType.FILTERABLE);
    things.config.dropPropertyIndex("size", PropertyIndexType.RANGE_FILTERS);

    Assertions.assertThat(config).get()
        .extracting(CollectionConfig::properties, InstanceOfAssertFactories.list(Property.class))
        .allSatisfy(property -> Assertions.assertThat(property)
            .returns(false, Property::indexFilterable)
            .returns(false, Property::indexSearchable)
            .returns(false, Property::indexRangeFilters));
  }
}
