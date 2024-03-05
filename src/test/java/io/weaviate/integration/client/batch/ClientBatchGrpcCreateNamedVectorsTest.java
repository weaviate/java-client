package io.weaviate.integration.client.batch;

import io.weaviate.client.Config;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.batch.model.ObjectGetResponse;
import io.weaviate.client.v1.data.model.WeaviateObject;
import io.weaviate.client.v1.misc.model.BQConfig;
import io.weaviate.client.v1.misc.model.VectorIndexConfig;
import io.weaviate.client.v1.schema.model.DataType;
import io.weaviate.client.v1.schema.model.Property;
import io.weaviate.client.v1.schema.model.WeaviateClass;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.ARRAY;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.ComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;

public class ClientBatchGrpcCreateNamedVectorsTest {
  private static String host;
  private static Integer port;
  private static String grpcHost;
  private static Integer grpcPort;

  @ClassRule
  public static ComposeContainer compose = new ComposeContainer(
    new File("src/test/resources/docker-compose-test.yaml")
  ).withExposedService("weaviate-1", 8080, Wait.forListeningPorts(8080))
    .withExposedService("weaviate-1", 50051, Wait.forListeningPorts(50051))
    .withTailChildContainers(true);

  @Before
  public void before() {
    host = compose.getServiceHost("weaviate-1", 8080);
    port = compose.getServicePort("weaviate-1", 8080);
    grpcHost = compose.getServiceHost("weaviate-1", 50051);
    grpcPort = compose.getServicePort("weaviate-1", 50051);
  }

  @Test
  public void shouldCreateObjectsWithNamedVectors() {
    WeaviateClient client = createClient();
    String className = "NamedVectors";
    List<Property> properties = Arrays.asList(
      Property.builder()
        .name("name")
        .dataType(Collections.singletonList(DataType.TEXT))
        .build(),
      Property.builder()
        .name("title")
        .dataType(Collections.singletonList(DataType.TEXT))
        .build());
    Map<String, Object> none = new HashMap<>();
    none.put("none", new Object());
    Map<String, Object> text2vecContextionary = new HashMap<>();
    Map<String, Object> text2vecContextionarySettings =  new HashMap<>();
    text2vecContextionarySettings.put("vectorizeClassName", false);
    text2vecContextionarySettings.put("properties", new String[]{"title"});
    text2vecContextionary.put("text2vec-contextionary", text2vecContextionarySettings);
    Map<String, WeaviateClass.VectorConfig> vectorConfig = new HashMap<>();
    vectorConfig.put("hnswVector", WeaviateClass.VectorConfig.builder()
      .vectorIndexType("hnsw")
      .vectorizer(none)
      .build());
    vectorConfig.put("c11y", WeaviateClass.VectorConfig.builder()
      .vectorIndexType("flat")
      .vectorizer(text2vecContextionary)
      .vectorIndexConfig(VectorIndexConfig.builder()
        .bq(BQConfig.builder().enabled(true).build())
        .build())
      .build());
    Result<Boolean> createResult = client.schema().classCreator()
      .withClass(WeaviateClass.builder()
        .className(className)
        .properties(properties)
        .vectorConfig(vectorConfig)
        .build()
      )
      .run();
    assertThat(createResult).isNotNull()
      .returns(false, Result::hasErrors)
      .returns(true, Result::getResult);

    // create object
    String id = "00000000-0000-0000-0000-000000000001";
    Map<String, Object> props = new HashMap<>();
    props.put("name", "some name");
    props.put("title", "The Lord of the Rings");
    Float[] vector = new Float[]{0.11f, 0.22f, 0.33f, 0.123f, -0.900009f, -0.0000000001f};
    Map<String, Float[]> vectors = new HashMap<>();
    vectors.put("hnswVector", vector);
    WeaviateObject obj = WeaviateObject.builder()
      .id(id)
      .className(className)
      .properties(props)
      .vectors(vectors)
      .build();
    Result<ObjectGetResponse[]> result = client.batch().objectsBatcher()
      .withObjects(obj)
      .run();
    assertThat(result).isNotNull()
      .returns(false, Result::hasErrors)
      .extracting(Result::getResult).asInstanceOf(ARRAY)
      .hasSize(1);

    // fetch that object
    Result<List<WeaviateObject>> resultObj = client.data().objectsGetter()
      .withID(obj.getId())
      .withClassName(obj.getClassName())
      .withVector()
      .run();
    assertThat(resultObj).isNotNull()
      .returns(false, Result::hasErrors)
      .extracting(Result::getResult).isNotNull()
      .extracting(r -> r.get(0)).isNotNull()
      .satisfies(o -> {
        assertThat(o.getId()).isEqualTo(obj.getId());
        assertThat(o.getVectors()).isNotEmpty()
          .containsOnlyKeys("hnswVector", "c11y")
          .satisfies(vecs -> {
            assertThat(vecs.get("hnswVector")).isNotNull().isEqualTo(vector);
            assertThat(vecs.get("c11y")).isNotEmpty();
          });
        assertThat(o.getProperties()).isNotNull()
          .extracting(Map::size).isEqualTo(obj.getProperties().size());
        obj.getProperties().keySet().forEach(propName -> {
          assertThat(o.getProperties().get(propName)).isNotNull();
        });
      });

    // clean up
    Result<Boolean> delete = client.schema().classDeleter().withClassName(className).run();
    assertThat(delete).isNotNull()
      .returns(false, Result::hasErrors)
      .returns(true, Result::getResult);
  }

  private WeaviateClient createClient() {
    Config config = new Config("http", host + ":" + port);
    config.setGRPCSecured(false);
    config.setGRPCHost(grpcHost + ":" + grpcPort);
    return new WeaviateClient(config);
  }
}
