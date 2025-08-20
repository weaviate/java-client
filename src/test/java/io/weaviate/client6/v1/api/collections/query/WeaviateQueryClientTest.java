package io.weaviate.client6.v1.api.collections.query;

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
import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBase;
import io.weaviate.client6.v1.internal.json.JSON;
import io.weaviate.client6.v1.internal.orm.CollectionDescriptor;
import io.weaviate.testutil.transport.MockGrpcTransport;

@RunWith(JParamsTestRunner.class)
public class WeaviateQueryClientTest {
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
    void apply(WeaviateQueryClient<Map<String, Object>> client) throws Exception;
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
        {
            "get by id",
            (Act) client -> client.byId("test-uuid")
        },
        {
            "fetch objects",
            (Act) client -> client.fetchObjects(ObjectBuilder.identity()),
        },
        {
            "bm25",
            (Act) client -> client.bm25("red ballon"),
        },
        {
            "hybrid",
            (Act) client -> client.hybrid("red ballon"),
        },
        {
            "nearVector",
            (Act) client -> client.nearVector(new float[] {}),
        },
        {
            "nearText",
            (Act) client -> client.nearText("weather in Arizona"),
        },
        {
            "nearObject",
            (Act) client -> client.nearObject("test-uuid"),
        },
        {
            "nearImage",
            (Act) client -> client.nearImage("img.jpeg"),
        },
        {
            "nearAudio",
            (Act) client -> client.nearAudio("song.mp3"),
        },
        {
            "nearVideo",
            (Act) client -> client.nearVideo("clip.mp4"),
        },
        {
            "nearDepth",
            (Act) client -> client.nearDepth("20.000 leagues"),
        },
        {
            "nearThermal",
            (Act) client -> client.nearThermal("Fahrenheit 451"),
        },
        {
            "nearImu",
            (Act) client -> client.nearImu("6 m/s"),
        },
    };
  }

  @Name("0")
  @DataMethod(source = WeaviateQueryClientTest.class, method = "grpcTestCases")
  @Test
  public void test_collectionHandleDefaults_grpc(String __, Act act)
      throws Exception {
    // Arrange
    var collection = CollectionDescriptor.ofMap("Things");
    var defaults = new CollectionHandleDefaults(ConsistencyLevel.ONE);
    var client = new WeaviateQueryClient<Map<String, Object>>(collection, grpc, defaults);

    // Act
    act.apply(client);

    // Assert
    grpc.assertNext(json -> assertJsonHasValue(json, "consistencyLevel",
        WeaviateProtoBase.ConsistencyLevel.CONSISTENCY_LEVEL_ONE.toString()));
  }
}
