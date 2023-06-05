package io.weaviate.integration.client.schema;

import io.weaviate.client.Config;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.WeaviateError;
import io.weaviate.client.base.WeaviateErrorMessage;
import io.weaviate.client.v1.misc.model.ReplicationConfig;
import io.weaviate.client.v1.schema.model.WeaviateClass;
import org.apache.http.HttpStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.STRING;


public class ClusterSchemaTest {
  private WeaviateClient client;

  @ClassRule
  public static DockerComposeContainer compose = new DockerComposeContainer(
    new File("src/test/resources/docker-compose-cluster.yaml")
  ).withExposedService("weaviate-node-1_1", 8087, Wait.forHttp("/v1/.well-known/ready").forStatusCode(200));

  @Before
  public void before() {
    String host = compose.getServiceHost("weaviate-node-1_1", 8087);
    Integer port = compose.getServicePort("weaviate-node-1_1", 8087);
    Config config = new Config("http", host + ":" + port);

    client = new WeaviateClient(config);
  }

  @After
  public void after() {
    Result<Boolean> deleted = client.schema().allDeleter().run();
    assertThat(deleted.hasErrors()).isFalse();
  }

  @Test
  public void shouldCreateClassWithImplicitReplicationFactor() {
    // given
    int replicationFactor = 1;

    String className = "Band";
    WeaviateClass clazz = WeaviateClass.builder()
      .className(className)
      .description("Band that plays and produces music")
      .vectorIndexType("hnsw")
      .vectorizer("text2vec-contextionary")
      .build();

    // when
    Result<Boolean> createStatus = client.schema().classCreator().withClass(clazz).run();
    assertThat(createStatus).isNotNull()
      .returns(false, Result::hasErrors)
      .returns(true, Result::getResult);

    // then
    Result<WeaviateClass> classResult = client.schema().classGetter().withClassName(className).run();
    assertThat(classResult).isNotNull()
      .returns(false, Result::hasErrors)
      .extracting(Result::getResult).isNotNull()
      .extracting(WeaviateClass::getReplicationConfig)
      .isNotNull()
      .extracting(ReplicationConfig::getFactor)
      .isEqualTo(replicationFactor);
  }

  @Test
  public void shouldCreateClassWithExplicitReplicationFactor() {
    // given
    int replicationFactor = 2;

    String className = "Band";
    WeaviateClass clazz = WeaviateClass.builder()
      .className(className)
      .description("Band that plays and produces music")
      .vectorIndexType("hnsw")
      .vectorizer("text2vec-contextionary")
      .replicationConfig(ReplicationConfig.builder().factor(replicationFactor).build())
      .build();

    // when
    Result<Boolean> createStatus = client.schema().classCreator().withClass(clazz).run();
    assertThat(createStatus).isNotNull()
      .returns(false, Result::hasErrors)
      .returns(true, Result::getResult);

    // then
    Result<WeaviateClass> classResult = client.schema().classGetter().withClassName(className).run();
    assertThat(classResult).isNotNull()
      .returns(false, Result::hasErrors)
      .extracting(Result::getResult).isNotNull()
      .extracting(WeaviateClass::getReplicationConfig)
      .isNotNull()
      .extracting(ReplicationConfig::getFactor)
      .isEqualTo(replicationFactor);
  }

  @Test
  public void shouldNotCreateClassWithTooHighFactor() {
    // given
    int replicationFactor = 3;

    String className = "Band";
    WeaviateClass clazz = WeaviateClass.builder()
      .className(className)
      .description("Band that plays and produces music")
      .vectorIndexType("hnsw")
      .vectorizer("text2vec-contextionary")
      .replicationConfig(ReplicationConfig.builder().factor(replicationFactor).build())
      .build();

    // when
    Result<Boolean> createStatus = client.schema().classCreator().withClass(clazz).run();
    assertThat(createStatus).isNotNull()
      .returns(true, Result::hasErrors)
      .extracting(Result::getError)
      .returns(HttpStatus.SC_UNPROCESSABLE_ENTITY, WeaviateError::getStatusCode)
      .extracting(WeaviateError::getMessages).asList()
      .first()
      .extracting(m -> ((WeaviateErrorMessage) m).getMessage()).asInstanceOf(STRING)
      .contains("not enough replicas");
  }
}
