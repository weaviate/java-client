package io.weaviate.client6.v1.api.collections.data;

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

import io.weaviate.client6.v1.api.collections.CollectionHandleDefaults;
import io.weaviate.client6.v1.api.collections.CollectionHandleDefaults.Location;
import io.weaviate.client6.v1.api.collections.query.ConsistencyLevel;
import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBase;
import io.weaviate.client6.v1.internal.json.JSON;
import io.weaviate.client6.v1.internal.orm.CollectionDescriptor;
import io.weaviate.testutil.transport.MockGrpcTransport;
import io.weaviate.testutil.transport.MockRestTransport;

@RunWith(JParamsTestRunner.class)
public class WeaviateDataClientTest {
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
    void apply(WeaviateDataClient<Map<String, Object>> client) throws Exception;
  }

  public static Object[][] restTestCases() {
    return new Object[][] {
        {
            "insert single object",
            ConsistencyLevel.ONE, Location.QUERY,
            (Act) client -> client.insert(Map.of()),
        },
        {
            "replace single object",
            ConsistencyLevel.ONE, Location.QUERY,
            (Act) client -> client.replace("test-uuid", ObjectBuilder.identity()),
        },
        {
            "update single object",
            ConsistencyLevel.ONE, Location.QUERY,
            (Act) client -> client.update("test-uuid", ObjectBuilder.identity()),
        },
        {
            "delete by id",
            ConsistencyLevel.ONE, Location.QUERY,
            (Act) client -> client.delete("test-uuid"),
        },
        {
            "add reference",
            ConsistencyLevel.ONE, Location.QUERY,
            (Act) client -> client.referenceAdd("from-uuid", "from_property", Reference.uuids("to-uuid")),
        },
        {
            "add reference many",
            ConsistencyLevel.ONE, Location.QUERY,
            (Act) client -> client.referenceAddMany(),
        },
        {
            "replace reference",
            ConsistencyLevel.ONE, Location.QUERY,
            (Act) client -> client.referenceReplace("from-uuid", "from_property", Reference.uuids("to-uuid")),
        },
        {
            "delete reference",
            ConsistencyLevel.ONE, Location.QUERY,
            (Act) client -> client.referenceDelete("from-uuid", "from_property", Reference.uuids("to-uuid")),
        },
    };
  }

  @Name("0")
  @DataMethod(source = WeaviateDataClientTest.class, method = "restTestCases")
  @Test
  public void test_collectionHandleDefaults_rest(String __, ConsistencyLevel cl, Location clLoc, Act act)
      throws Exception {
    // Arrange
    var collection = CollectionDescriptor.ofMap("Things");
    var defaults = new CollectionHandleDefaults(cl);
    var client = new WeaviateDataClient<Map<String, Object>>(
        collection, rest, null, defaults);

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
    });
  }

  private <T> void assertJsonHasValue(String json, String key, T value) {
    var gotJson = JsonParser.parseString(json).getAsJsonObject();
    Assertions.assertThat(gotJson.has(key))
        .describedAs("missing key \"%s\" in %s", key, json)
        .isTrue();

    var wantValue = JsonParser.parseString(JSON.serialize(value));
    Assertions.assertThat(gotJson.get(key)).isEqualTo(wantValue);
  }

  public static Object[][] grpcTestCases() {
    return new Object[][] {
        { "object exists", (Act) client -> client.exists("test-uuid") },
        { "insert many", (Act) client -> client.insertMany() },
        { "delete many", (Act) client -> client.deleteMany() },
    };
  }

  @Name("0")
  @DataMethod(source = WeaviateDataClientTest.class, method = "grpcTestCases")
  @Test
  public void test_collectionHandleDefaults_grpc(String __, Act act)
      throws Exception {
    // Arrange
    var collection = CollectionDescriptor.ofMap("Things");
    var defaults = new CollectionHandleDefaults(ConsistencyLevel.ONE);
    var client = new WeaviateDataClient<Map<String, Object>>(
        collection, null, grpc, defaults);

    // Act
    act.apply(client);

    // Assert
    grpc.assertNext(json -> assertJsonHasValue(json, "consistencyLevel",
        WeaviateProtoBase.ConsistencyLevel.CONSISTENCY_LEVEL_ONE.toString()));
  }
}
