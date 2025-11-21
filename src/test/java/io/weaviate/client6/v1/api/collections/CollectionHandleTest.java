package io.weaviate.client6.v1.api.collections;

import java.io.IOException;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.gson.JsonParser;
import com.jparams.junit4.JParamsTestRunner;
import com.jparams.junit4.data.DataMethod;
import com.jparams.junit4.description.Name;

import io.weaviate.client6.v1.api.collections.data.Reference;
import io.weaviate.client6.v1.api.collections.query.ConsistencyLevel;
import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBase;
import io.weaviate.client6.v1.internal.json.JSON;
import io.weaviate.client6.v1.internal.orm.CollectionDescriptor;
import io.weaviate.testutil.transport.MockGrpcTransport;
import io.weaviate.testutil.transport.MockRestTransport;

@RunWith(JParamsTestRunner.class)
public class CollectionHandleTest {
  private static MockRestTransport rest;
  private static MockGrpcTransport grpc;

  @BeforeClass
  public static void setUp() {
    rest = new MockRestTransport();
    grpc = new MockGrpcTransport();
  }

  @AfterClass
  public static void tearDown() throws Exception {
    rest.close();
    grpc.close();
  }

  @FunctionalInterface
  interface Act {
    void apply(CollectionHandle<Map<String, Object>> client) throws Exception;
  }

  /** Which part of the request a parameter should be added to. */
  public static enum Location {
    /** Query string. */
    QUERY,
    /**
     * Request body. {@code RequestT} must implement {@link WithDefaults} for the
     * changes to be applied.
     */
    BODY;
  }

  public static Object[][] restTestCases() {
    return new Object[][] {
        {
            "data::insert single object",
            ConsistencyLevel.ONE, Location.QUERY,
            "john_doe", Location.BODY,
            (Act) c -> c.data.insert(Map.of()),
        },
        {
            "data::replace single object",
            ConsistencyLevel.ONE, Location.QUERY,
            "john_doe", Location.BODY,
            (Act) c -> c.data.replace("test-uuid", ObjectBuilder.identity()),
        },
        {
            "data::update single object",
            ConsistencyLevel.ONE, Location.QUERY,
            "john_doe", Location.BODY,
            (Act) c -> c.data.update("test-uuid", ObjectBuilder.identity()),
        },
        {
            "data::delete by id",
            ConsistencyLevel.ONE, Location.QUERY,
            "john_doe", Location.QUERY,
            (Act) c -> c.data.deleteById("test-uuid"),
        },
        {
            "data::add reference",
            ConsistencyLevel.ONE, Location.QUERY,
            "john_doe", Location.QUERY,
            (Act) c -> c.data.referenceAdd("from-uuid", "from_property", Reference.uuids("to-uuid")),
        },
        {
            "data::add reference many",
            ConsistencyLevel.ONE, Location.QUERY,
            "john_doe", Location.QUERY,
            (Act) c -> c.data.referenceAddMany(),
        },
        {
            "data::replace reference",
            ConsistencyLevel.ONE, Location.QUERY,
            "john_doe", Location.QUERY,
            (Act) c -> c.data.referenceReplace("from-uuid", "from_property", Reference.uuids("to-uuid")),
        },
        {
            "data::delete reference",
            ConsistencyLevel.ONE, Location.QUERY,
            "john_doe", Location.QUERY,
            (Act) c -> c.data.referenceDelete("from-uuid", "from_property", Reference.uuids("to-uuid")),
        },
    };
  }

  @Name("{0}")
  @DataMethod(source = CollectionHandleTest.class, method = "restTestCases")
  @Test
  public void test_collectionHandleDefaults_rest(String __,
      ConsistencyLevel cl, Location clLoc,
      String tenant, Location tenantLoc,
      Act act)
      throws Exception {
    // Arrange
    var collection = CollectionDescriptor.ofMap("Things");
    var defaults = CollectionHandleDefaults.of(d -> d
        .consistencyLevel(cl)
        .tenant(tenant));
    var client = new CollectionHandle<Map<String, Object>>(rest, grpc, collection, defaults);

    // Act
    act.apply(client);

    // Assert
    rest.assertNext((method, requestUrl, body, query) -> {
      switch (clLoc) {
        case QUERY:
          Assertions.assertThat(query).containsEntry("consistency_level", defaults.consistencyLevel());
          break;
        case BODY:
          assertJsonHasValue(body, "consistency_level", defaults.consistencyLevel());
      }

      switch (tenantLoc) {
        case QUERY:
          Assertions.assertThat(query).containsEntry("tenant", defaults.tenant());
          break;
        case BODY:
          assertJsonHasValue(body, "tenant", defaults.tenant());
      }
    });
  }

  public static Object[][] grpcTestCases() {
    return new Object[][] {
        { "data::object exists", (Act) c -> c.data.exists("test-uuid") },
        { "data::delete many", (Act) c -> c.data.deleteMany() },

        { "query::get by id", (Act) c -> c.query.fetchObjectById("test-uuid") },
        { "query::fetch objects", (Act) c -> c.query.fetchObjects(ObjectBuilder.identity()) },
        { "query::bm25", (Act) c -> c.query.bm25("red ballon") },
        { "query::hybrid", (Act) c -> c.query.hybrid("red ballon") },
        { "query::nearVector", (Act) c -> c.query.nearVector(new float[] {}) },
        { "query::nearText", (Act) c -> c.query.nearText("weather in Arizona") },
        { "query::nearObject", (Act) c -> c.query.nearObject("test-uuid") },
        { "query::nearImage", (Act) c -> c.query.nearImage("img.jpeg") },
        { "query::nearAudio", (Act) c -> c.query.nearAudio("song.mp3") },
        { "query::nearVideo", (Act) c -> c.query.nearVideo("clip.mp4") },
        { "query::nearDepth", (Act) c -> c.query.nearDepth("20.000 leagues") },
        { "query::nearThermal", (Act) c -> c.query.nearThermal("Fahrenheit 451") },
        { "query::nearImu", (Act) c -> c.query.nearImu("6 m/s") },

        { "aggregate::over all", (Act) c -> c.aggregate.overAll(ObjectBuilder.identity()), true },
        { "aggregate::hybrid", (Act) c -> c.aggregate.hybrid("red balloon", ObjectBuilder.identity()), true },
        { "aggregate::nearVector", (Act) c -> c.aggregate.nearVector(new float[] {}, ObjectBuilder.identity()), true },
        { "aggregate::nearText", (Act) c -> c.aggregate.nearText("red balloon", ObjectBuilder.identity()), true },
        { "aggregate::nearObject", (Act) c -> c.aggregate.nearObject("test-uuid", ObjectBuilder.identity()), true },
        { "aggregate::nearImage", (Act) c -> c.aggregate.nearImage("img.jpeg", ObjectBuilder.identity()), true },
        { "aggregate::nearAudio", (Act) c -> c.aggregate.nearAudio("song.mp3", ObjectBuilder.identity()), true },
        { "aggregate::nearVideo", (Act) c -> c.aggregate.nearVideo("clip.mp4", ObjectBuilder.identity()), true },
        { "aggregate::nearDepth", (Act) c -> c.aggregate.nearDepth("20.000 leagues", ObjectBuilder.identity()), true },
        { "aggregate::nearThermal", (Act) c -> c.aggregate.nearThermal("Fahrenheit 451", ObjectBuilder.identity()),
            true },
        { "aggregate::nearImu", (Act) c -> c.aggregate.nearImu("6 m/s", ObjectBuilder.identity()), true },
    };
  }

  @Name("{0}")
  @DataMethod(source = CollectionHandleTest.class, method = "grpcTestCases")
  @Test
  public void test_collectionHandleDefaults_grpc(String __, Act act, Boolean skipConsistency) throws Exception {
    // Arrange
    var collection = CollectionDescriptor.ofMap("Things");
    var defaults = CollectionHandleDefaults.of(d -> d
        .consistencyLevel(ConsistencyLevel.ONE)
        .tenant("john_doe"));
    var client = new CollectionHandle<Map<String, Object>>(rest, grpc, collection, defaults);

    // Act
    act.apply(client);

    // Assert
    grpc.assertNext(json -> {
      assertJsonHasValue(json, "tenant", "john_doe");

      if (skipConsistency != null && !skipConsistency) {
        assertJsonHasValue(json, "consistencyLevel",
            WeaviateProtoBase.ConsistencyLevel.CONSISTENCY_LEVEL_ONE.toString());
      }
    });
  }

  @Test
  public void test_defaultTenant_getShards() throws IOException {
    // Arrange
    var collection = CollectionDescriptor.ofMap("Things");
    var defaults = CollectionHandleDefaults.of(d -> d
        .tenant("john_doe"));
    var client = new CollectionHandle<Map<String, Object>>(rest, grpc, collection, defaults);

    // Act
    client.config.getShards();

    // Assert
    rest.assertNext((method, requestUrl, body, query) -> {
      Assertions.assertThat(query).containsEntry("tenant", defaults.tenant());
    });
  }

  @Test
  public void test_defaultTenant_insertMany() {
    // Arrange
    var collection = CollectionDescriptor.ofMap("Things");
    var defaults = CollectionHandleDefaults.of(d -> d
        .consistencyLevel(ConsistencyLevel.ONE)
        .tenant("john_doe"));
    var client = new CollectionHandle<Map<String, Object>>(rest, grpc, collection, defaults);

    // Act
    client.data.insertMany(Map.of());

    // Assert
    grpc.assertNext(json -> {
      // Tenant is nested in each of the batch objects
      Assertions.assertThat(json).containsSequence("\"tenant\": \"john_doe\"");
      assertJsonHasValue(json, "consistencyLevel",
          WeaviateProtoBase.ConsistencyLevel.CONSISTENCY_LEVEL_ONE.toString());
    });
  }

  private static <T> void assertJsonHasValue(String json, String key, T value) {
    var gotJson = JsonParser.parseString(json).getAsJsonObject();
    Assertions.assertThat(gotJson.has(key))
        .describedAs("missing key \"%s\" in %s", key, json)
        .isTrue();

    var wantValue = JsonParser.parseString(JSON.serialize(value));
    Assertions.assertThat(gotJson.get(key)).isEqualTo(wantValue);
  }
}
