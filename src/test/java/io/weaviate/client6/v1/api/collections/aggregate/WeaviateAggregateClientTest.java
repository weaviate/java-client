package io.weaviate.client6.v1.api.collections.aggregate;

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
import io.weaviate.client6.v1.api.collections.query.ConsistencyLevel;
import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.json.JSON;
import io.weaviate.client6.v1.internal.orm.CollectionDescriptor;
import io.weaviate.testutil.transport.MockGrpcTransport;

@RunWith(JParamsTestRunner.class)
public class WeaviateAggregateClientTest {
  private static MockGrpcTransport grpc;

  @BeforeClass
  public static void setUp() {
    grpc = new MockGrpcTransport();
  }

  @AfterClass
  public static void tearDown() throws Exception {
    grpc.close();
  }

  @FunctionalInterface
  interface Act {
    void apply(WeaviateAggregateClient client) throws Exception;
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
        { "over all", (Act) client -> client.overAll(ObjectBuilder.identity()) },
        { "hybrid", (Act) client -> client.hybrid("red balloon", ObjectBuilder.identity()) },
        { "nearVector", (Act) client -> client.nearVector(new float[] {}, ObjectBuilder.identity()) },
        { "nearText", (Act) client -> client.nearText("red balloon", ObjectBuilder.identity()) },
        { "nearObject", (Act) client -> client.nearObject("test-uuid", ObjectBuilder.identity()) },
        { "nearImage", (Act) client -> client.nearImage("img.jpeg", ObjectBuilder.identity()) },
        { "nearAudio", (Act) client -> client.nearAudio("song.mp3", ObjectBuilder.identity()) },
        { "nearVideo", (Act) client -> client.nearVideo("clip.mp4", ObjectBuilder.identity()) },
        { "nearDepth", (Act) client -> client.nearDepth("20.000 leagues", ObjectBuilder.identity()) },
        { "nearThermal", (Act) client -> client.nearThermal("Fahrenheit 451", ObjectBuilder.identity()) },
        { "nearImu", (Act) client -> client.nearImu("6 m/s", ObjectBuilder.identity()) },
    };
  }

  @Name("{0}")
  @DataMethod(source = WeaviateAggregateClientTest.class, method = "grpcTestCases")
  @Test
  public void test_collectionHandleDefaults_grpc(String __, Act act)
      throws Exception {
    // Arrange
    var collection = CollectionDescriptor.ofMap("Things");
    var defaults = CollectionHandleDefaults.of(d -> d
        .consistencyLevel(ConsistencyLevel.ONE)
        .tenant("john_doe"));
    var client = new WeaviateAggregateClient(collection, grpc, defaults);

    // Act
    act.apply(client);

    // Assert
    grpc.assertNext(json -> assertJsonHasValue(json, "tenant", "john_doe"));
  }
}
