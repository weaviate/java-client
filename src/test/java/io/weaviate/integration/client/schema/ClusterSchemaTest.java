package io.weaviate.integration.client.schema;

import io.weaviate.client.Config;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.WeaviateError;
import io.weaviate.client.base.WeaviateErrorMessage;
import io.weaviate.client.v1.batch.model.ObjectGetResponse;
import io.weaviate.client.v1.data.model.WeaviateObject;
import io.weaviate.client.v1.misc.model.ReplicationConfig;
import io.weaviate.client.v1.schema.model.DataType;
import io.weaviate.client.v1.schema.model.WeaviateClass;
import org.apache.http.HttpStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.STRING;


public class ClusterSchemaTest {
  private WeaviateClient client1;
  private WeaviateClient client2;
  private final NestedObjectsUtils utils = new NestedObjectsUtils();

  @ClassRule
  public static DockerComposeContainer<?> compose = new DockerComposeContainer<>(
    new File("src/test/resources/docker-compose-cluster.yaml")
  )
    .withExposedService("weaviate-node-1_1", 8087, Wait.forHttp("/v1/.well-known/ready").forStatusCode(200))
    .withExposedService("weaviate-node-2_1", 8088, Wait.forHttp("/v1/.well-known/ready").forStatusCode(200));

  @Before
  public void before() {
    String host1 = compose.getServiceHost("weaviate-node-1_1", 8087);
    Integer port1 = compose.getServicePort("weaviate-node-1_1", 8087);
    Config config1 = new Config("http", host1 + ":" + port1);
    client1 = new WeaviateClient(config1);

    String host2 = compose.getServiceHost("weaviate-node-2_1", 8088);
    Integer port2 = compose.getServicePort("weaviate-node-2_1", 8088);
    Config config2 = new Config("http", host2 + ":" + port2);
    client2 = new WeaviateClient(config2);
  }

  @After
  public void after() {
    Result<Boolean> deleted = client1.schema().allDeleter().run();
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
    Result<Boolean> createStatus = client1.schema().classCreator().withClass(clazz).run();
    assertThat(createStatus).isNotNull()
      .returns(false, Result::hasErrors)
      .returns(true, Result::getResult);

    // then
    Result<WeaviateClass> classResult = client1.schema().classGetter().withClassName(className).run();
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
    Result<Boolean> createStatus = client1.schema().classCreator().withClass(clazz).run();
    assertThat(createStatus).isNotNull()
      .returns(false, Result::hasErrors)
      .returns(true, Result::getResult);

    // then
    Result<WeaviateClass> classResult = client1.schema().classGetter().withClassName(className).run();
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
    Result<Boolean> createStatus = client1.schema().classCreator().withClass(clazz).run();
    assertThat(createStatus).isNotNull()
      .returns(true, Result::hasErrors)
      .extracting(Result::getError)
      .returns(HttpStatus.SC_UNPROCESSABLE_ENTITY, WeaviateError::getStatusCode)
      .extracting(WeaviateError::getMessages).asList()
      .first()
      .extracting(m -> ((WeaviateErrorMessage) m).getMessage()).asInstanceOf(STRING)
      .contains("not enough storage replicas");
  }@Test
  public void shouldAddObjectsWithNestedProperties_EntireSchema() {
    WeaviateClass schemaClass;
    String className = "ClassWithObjectProperty";
    Map<String, String> expectedProps = new HashMap<String, String>() {{
      put("name", DataType.TEXT);
      put("objectProperty", DataType.OBJECT);
      put("objectProperty.nestedInt", DataType.INT);
      put("objectProperty.nestedNumber", DataType.NUMBER);
      put("objectProperty.nestedText", DataType.TEXT);
      put("objectProperty.nestedObjects", DataType.OBJECT_ARRAY);
      put("objectProperty.nestedObjects.nestedBoolLvl2", DataType.BOOLEAN);
      put("objectProperty.nestedObjects.nestedDateLvl2", DataType.DATE);
      put("objectProperty.nestedObjects.nestedNumbersLvl2", DataType.NUMBER_ARRAY);
    }};

    WeaviateClass wvtClass = utils.nestedClassEntireSchema(className);
    utils.createClass(client1, wvtClass);

    schemaClass = utils.getClass(client1, className);
    utils.assertThatSchemaPropertiesHaveDataTypes(expectedProps, schemaClass);
    schemaClass = utils.getClass(client2, className);
    utils.assertThatSchemaPropertiesHaveDataTypes(expectedProps, schemaClass);

    // schema did not change after adding object 1
    utils.createObject(client1, utils.nestedObject1(className));
    schemaClass = utils.getClass(client1, className);
    utils.assertThatSchemaPropertiesHaveDataTypes(expectedProps, schemaClass);
    schemaClass = utils.getClass(client2, className);
    utils.assertThatSchemaPropertiesHaveDataTypes(expectedProps, schemaClass);

    // schema did not change after adding object 2
    utils.createObject(client1, utils.nestedObject2(className));
    schemaClass = utils.getClass(client1, className);
    utils.assertThatSchemaPropertiesHaveDataTypes(expectedProps, schemaClass);
    schemaClass = utils.getClass(client2, className);
    utils.assertThatSchemaPropertiesHaveDataTypes(expectedProps, schemaClass);
  }

  @Test
  public void shouldAddObjectsWithNestedProperties_PartialSchema1() {
    WeaviateClass schemaClass;
    String className = "ClassWithObjectProperty";
    Map<String, String> expectedPropsStep1 = new HashMap<String, String>() {{
      put("name", DataType.TEXT);
      put("objectProperty", DataType.OBJECT);
      put("objectProperty.nestedInt", DataType.INT);
      put("objectProperty.nestedText", DataType.TEXT);
      put("objectProperty.nestedObjects", DataType.OBJECT_ARRAY);
      put("objectProperty.nestedObjects.nestedBoolLvl2", DataType.BOOLEAN);
      put("objectProperty.nestedObjects.nestedNumbersLvl2", DataType.NUMBER_ARRAY);
    }};
    Map<String, String> expectedPropsStep2 = new HashMap<String, String>() {{
      put("name", DataType.TEXT);
      put("objectProperty", DataType.OBJECT);
      put("objectProperty.nestedInt", DataType.INT);
      put("objectProperty.nestedNumber", DataType.NUMBER);
      put("objectProperty.nestedText", DataType.TEXT);
      put("objectProperty.nestedObjects", DataType.OBJECT_ARRAY);
      put("objectProperty.nestedObjects.nestedBoolLvl2", DataType.BOOLEAN);
      put("objectProperty.nestedObjects.nestedDateLvl2", DataType.DATE);
      put("objectProperty.nestedObjects.nestedNumbersLvl2", DataType.NUMBER_ARRAY);
    }};

    WeaviateClass wvtClass = utils.nestedClassPartialSchema1(className);
    utils.createClass(client1, wvtClass);

    schemaClass = utils.getClass(client1, className);
    utils.assertThatSchemaPropertiesHaveDataTypes(expectedPropsStep1, schemaClass);

    // schema did not change after adding object 1
    utils.createObject(client1, utils.nestedObject1(className));
    schemaClass = utils.getClass(client1, className);
    utils.assertThatSchemaPropertiesHaveDataTypes(expectedPropsStep1, schemaClass);
    schemaClass = utils.getClass(client2, className);
    utils.assertThatSchemaPropertiesHaveDataTypes(expectedPropsStep1, schemaClass);

    // schema changed after adding object 2
    utils.createObject(client1, utils.nestedObject2(className));
    schemaClass = utils.getClass(client1, className);
    utils.assertThatSchemaPropertiesHaveDataTypes(expectedPropsStep2, schemaClass);
    schemaClass = utils.getClass(client2, className);
    utils.assertThatSchemaPropertiesHaveDataTypes(expectedPropsStep2, schemaClass);
  }

  @Test
  public void shouldAddObjectsWithNestedProperties_PartialSchema2() {
    WeaviateClass schemaClass;
    String className = "ClassWithObjectProperty";
    Map<String, String> expectedPropsStep1 = new HashMap<String, String>() {{
      put("name", DataType.TEXT);
      put("objectProperty", DataType.OBJECT);
      put("objectProperty.nestedNumber", DataType.NUMBER);
      put("objectProperty.nestedText", DataType.TEXT);
      put("objectProperty.nestedObjects", DataType.OBJECT_ARRAY);
      put("objectProperty.nestedObjects.nestedDateLvl2", DataType.DATE);
      put("objectProperty.nestedObjects.nestedNumbersLvl2", DataType.NUMBER_ARRAY);
    }};
    Map<String, String> expectedPropsStep2 = new HashMap<String, String>() {{
      put("name", DataType.TEXT);
      put("objectProperty", DataType.OBJECT);
      put("objectProperty.nestedInt", DataType.NUMBER); // autoschema determines type as number
      put("objectProperty.nestedNumber", DataType.NUMBER);
      put("objectProperty.nestedText", DataType.TEXT);
      put("objectProperty.nestedObjects", DataType.OBJECT_ARRAY);
      put("objectProperty.nestedObjects.nestedBoolLvl2", DataType.BOOLEAN);
      put("objectProperty.nestedObjects.nestedDateLvl2", DataType.DATE);
      put("objectProperty.nestedObjects.nestedNumbersLvl2", DataType.NUMBER_ARRAY);
    }};

    WeaviateClass wvtClass = utils.nestedClassPartialSchema2(className);
    utils.createClass(client1, wvtClass);

    schemaClass = utils.getClass(client1, className);
    utils.assertThatSchemaPropertiesHaveDataTypes(expectedPropsStep1, schemaClass);

    // schema did not change after adding object 2
    utils.createObject(client1, utils.nestedObject2(className));
    schemaClass = utils.getClass(client1, className);
    utils.assertThatSchemaPropertiesHaveDataTypes(expectedPropsStep1, schemaClass);
    schemaClass = utils.getClass(client2, className);
    utils.assertThatSchemaPropertiesHaveDataTypes(expectedPropsStep1, schemaClass);

    // schema changed after adding object 1
    utils.createObject(client1, utils.nestedObject1(className));
    schemaClass = utils.getClass(client1, className);
    utils.assertThatSchemaPropertiesHaveDataTypes(expectedPropsStep2, schemaClass);
    schemaClass = utils.getClass(client2, className);
    utils.assertThatSchemaPropertiesHaveDataTypes(expectedPropsStep2, schemaClass);
  }

  @Test
  public void shouldAddObjectsWithNestedProperties_NoSchema1() {
    WeaviateClass schemaClass;
    String className = "ClassWithObjectProperty";
    Map<String, String> expectedPropsStep1 = new HashMap<String, String>() {{
      put("name", DataType.TEXT);
      put("objectProperty", DataType.OBJECT);
      put("objectProperty.nestedInt", DataType.NUMBER); // autoschema determines type as number
      put("objectProperty.nestedText", DataType.TEXT);
      put("objectProperty.nestedObjects", DataType.OBJECT_ARRAY);
      put("objectProperty.nestedObjects.nestedBoolLvl2", DataType.BOOLEAN);
      put("objectProperty.nestedObjects.nestedNumbersLvl2", DataType.NUMBER_ARRAY);
    }};
    Map<String, String> expectedPropsStep2 = new HashMap<String, String>() {{
      put("name", DataType.TEXT);
      put("objectProperty", DataType.OBJECT);
      put("objectProperty.nestedInt", DataType.NUMBER);
      put("objectProperty.nestedNumber", DataType.NUMBER);
      put("objectProperty.nestedText", DataType.TEXT);
      put("objectProperty.nestedObjects", DataType.OBJECT_ARRAY);
      put("objectProperty.nestedObjects.nestedBoolLvl2", DataType.BOOLEAN);
      put("objectProperty.nestedObjects.nestedDateLvl2", DataType.DATE);
      put("objectProperty.nestedObjects.nestedNumbersLvl2", DataType.NUMBER_ARRAY);
    }};

    // schema created after adding object 1
    utils.createObject(client1, utils.nestedObject1(className));
    schemaClass = utils.getClass(client1, className);
    utils.assertThatSchemaPropertiesHaveDataTypes(expectedPropsStep1, schemaClass);
    schemaClass = utils.getClass(client2, className);
    utils.assertThatSchemaPropertiesHaveDataTypes(expectedPropsStep1, schemaClass);

    // schema changed after adding object 2
    utils.createObject(client1, utils.nestedObject2(className));
    schemaClass = utils.getClass(client1, className);
    utils.assertThatSchemaPropertiesHaveDataTypes(expectedPropsStep2, schemaClass);
    schemaClass = utils.getClass(client2, className);
    utils.assertThatSchemaPropertiesHaveDataTypes(expectedPropsStep2, schemaClass);
  }

  @Test
  public void shouldAddObjectsWithNestedProperties_NoSchema2() {
    WeaviateClass schemaClass;
    String className = "ClassWithObjectProperty";
    Map<String, String> expectedPropsStep1 = new HashMap<String, String>() {{
      put("name", DataType.TEXT);
      put("objectProperty", DataType.OBJECT);
      put("objectProperty.nestedNumber", DataType.NUMBER);
      put("objectProperty.nestedText", DataType.TEXT);
      put("objectProperty.nestedObjects", DataType.OBJECT_ARRAY);
      put("objectProperty.nestedObjects.nestedDateLvl2", DataType.DATE);
      put("objectProperty.nestedObjects.nestedNumbersLvl2", DataType.NUMBER_ARRAY);
    }};
    Map<String, String> expectedPropsStep2 = new HashMap<String, String>() {{
      put("name", DataType.TEXT);
      put("objectProperty", DataType.OBJECT);
      put("objectProperty.nestedInt", DataType.NUMBER); // autoschema determines type as number
      put("objectProperty.nestedNumber", DataType.NUMBER);
      put("objectProperty.nestedText", DataType.TEXT);
      put("objectProperty.nestedObjects", DataType.OBJECT_ARRAY);
      put("objectProperty.nestedObjects.nestedBoolLvl2", DataType.BOOLEAN);
      put("objectProperty.nestedObjects.nestedDateLvl2", DataType.DATE);
      put("objectProperty.nestedObjects.nestedNumbersLvl2", DataType.NUMBER_ARRAY);
    }};

    // schema created after adding object 2
    utils.createObject(client1, utils.nestedObject2(className));
    schemaClass = utils.getClass(client1, className);
    utils.assertThatSchemaPropertiesHaveDataTypes(expectedPropsStep1, schemaClass);
    schemaClass = utils.getClass(client2, className);
    utils.assertThatSchemaPropertiesHaveDataTypes(expectedPropsStep1, schemaClass);

    // schema changed after adding object 1
    utils.createObject(client1, utils.nestedObject1(className));
    schemaClass = utils.getClass(client1, className);
    utils.assertThatSchemaPropertiesHaveDataTypes(expectedPropsStep2, schemaClass);
    schemaClass = utils.getClass(client2, className);
    utils.assertThatSchemaPropertiesHaveDataTypes(expectedPropsStep2, schemaClass);
  }

  @Test
  public void shouldBatchObjectsWithNestedProperties_EntireSchema() {
    WeaviateClass schemaClass;
    String className = "ClassWithObjectProperty";
    Map<String, String> expectedProps = new HashMap<String, String>() {{
      put("name", DataType.TEXT);
      put("objectProperty", DataType.OBJECT);
      put("objectProperty.nestedInt", DataType.INT);
      put("objectProperty.nestedNumber", DataType.NUMBER);
      put("objectProperty.nestedText", DataType.TEXT);
      put("objectProperty.nestedObjects", DataType.OBJECT_ARRAY);
      put("objectProperty.nestedObjects.nestedBoolLvl2", DataType.BOOLEAN);
      put("objectProperty.nestedObjects.nestedDateLvl2", DataType.DATE);
      put("objectProperty.nestedObjects.nestedNumbersLvl2", DataType.NUMBER_ARRAY);
    }};

    WeaviateClass wvtClass = utils.nestedClassEntireSchema(className);
    utils.createClass(client1, wvtClass);

    schemaClass = utils.getClass(client1, className);
    utils.assertThatSchemaPropertiesHaveDataTypes(expectedProps, schemaClass);

    // schema did not change after adding objects
    utils.batchObjects(client1, utils.nestedObject1(className), utils.nestedObject2(className));
    schemaClass = utils.getClass(client1, className);
    utils.assertThatSchemaPropertiesHaveDataTypes(expectedProps, schemaClass);
    schemaClass = utils.getClass(client2, className);
    utils.assertThatSchemaPropertiesHaveDataTypes(expectedProps, schemaClass);
  }

  @Test
  public void shouldBatchObjectsWithNestedProperties_PartialSchema1() {
    WeaviateClass schemaClass;
    String className = "ClassWithObjectProperty";
    Map<String, String> expectedPropsStep1 = new HashMap<String, String>() {{
      put("name", DataType.TEXT);
      put("objectProperty", DataType.OBJECT);
      put("objectProperty.nestedInt", DataType.INT);
      put("objectProperty.nestedText", DataType.TEXT);
      put("objectProperty.nestedObjects", DataType.OBJECT_ARRAY);
      put("objectProperty.nestedObjects.nestedBoolLvl2", DataType.BOOLEAN);
      put("objectProperty.nestedObjects.nestedNumbersLvl2", DataType.NUMBER_ARRAY);
    }};
    Map<String, String> expectedPropsStep2 = new HashMap<String, String>() {{
      put("name", DataType.TEXT);
      put("objectProperty", DataType.OBJECT);
      put("objectProperty.nestedInt", DataType.INT);
      put("objectProperty.nestedNumber", DataType.NUMBER);
      put("objectProperty.nestedText", DataType.TEXT);
      put("objectProperty.nestedObjects", DataType.OBJECT_ARRAY);
      put("objectProperty.nestedObjects.nestedBoolLvl2", DataType.BOOLEAN);
      put("objectProperty.nestedObjects.nestedDateLvl2", DataType.DATE);
      put("objectProperty.nestedObjects.nestedNumbersLvl2", DataType.NUMBER_ARRAY);
    }};

    WeaviateClass wvtClass = utils.nestedClassPartialSchema1(className);
    utils.createClass(client1, wvtClass);

    schemaClass = utils.getClass(client1, className);
    utils.assertThatSchemaPropertiesHaveDataTypes(expectedPropsStep1, schemaClass);

    // schema changed after adding objects
    utils.batchObjects(client1, utils.nestedObject1(className), utils.nestedObject2(className));
    schemaClass = utils.getClass(client1, className);
    utils.assertThatSchemaPropertiesHaveDataTypes(expectedPropsStep2, schemaClass);
    schemaClass = utils.getClass(client2, className);
    utils.assertThatSchemaPropertiesHaveDataTypes(expectedPropsStep2, schemaClass);
  }

  @Test
  public void shouldBatchObjectsWithNestedProperties_PartialSchema2() {
    WeaviateClass schemaClass;
    String className = "ClassWithObjectProperty";
    Map<String, String> expectedPropsStep1 = new HashMap<String, String>() {{
      put("name", DataType.TEXT);
      put("objectProperty", DataType.OBJECT);
      put("objectProperty.nestedNumber", DataType.NUMBER);
      put("objectProperty.nestedText", DataType.TEXT);
      put("objectProperty.nestedObjects", DataType.OBJECT_ARRAY);
      put("objectProperty.nestedObjects.nestedDateLvl2", DataType.DATE);
      put("objectProperty.nestedObjects.nestedNumbersLvl2", DataType.NUMBER_ARRAY);
    }};
    Map<String, String> expectedPropsStep2 = new HashMap<String, String>() {{
      put("name", DataType.TEXT);
      put("objectProperty", DataType.OBJECT);
      put("objectProperty.nestedInt", DataType.NUMBER); // autoschema determines type as number
      put("objectProperty.nestedNumber", DataType.NUMBER);
      put("objectProperty.nestedText", DataType.TEXT);
      put("objectProperty.nestedObjects", DataType.OBJECT_ARRAY);
      put("objectProperty.nestedObjects.nestedBoolLvl2", DataType.BOOLEAN);
      put("objectProperty.nestedObjects.nestedDateLvl2", DataType.DATE);
      put("objectProperty.nestedObjects.nestedNumbersLvl2", DataType.NUMBER_ARRAY);
    }};

    WeaviateClass wvtClass = utils.nestedClassPartialSchema2(className);
    utils.createClass(client1, wvtClass);

    schemaClass = utils.getClass(client1, className);
    utils.assertThatSchemaPropertiesHaveDataTypes(expectedPropsStep1, schemaClass);

    // schema changed after adding objects
    utils.batchObjects(client1, utils.nestedObject1(className), utils.nestedObject2(className));
    schemaClass = utils.getClass(client1, className);
    utils.assertThatSchemaPropertiesHaveDataTypes(expectedPropsStep2, schemaClass);
    schemaClass = utils.getClass(client2, className);
    utils.assertThatSchemaPropertiesHaveDataTypes(expectedPropsStep2, schemaClass);
  }

  @Test
  public void shouldBatchObjectsWithNestedProperties_NoSchema1() {
    WeaviateClass schemaClass;
    String className = "ClassWithObjectProperty";
    Map<String, String> expectedProps = new HashMap<String, String>() {{
      put("name", DataType.TEXT);
      put("objectProperty", DataType.OBJECT);
      put("objectProperty.nestedInt", DataType.NUMBER); // autoschema determines type as number
      put("objectProperty.nestedNumber", DataType.NUMBER);
      put("objectProperty.nestedText", DataType.TEXT);
      put("objectProperty.nestedObjects", DataType.OBJECT_ARRAY);
      put("objectProperty.nestedObjects.nestedBoolLvl2", DataType.BOOLEAN);
      put("objectProperty.nestedObjects.nestedDateLvl2", DataType.DATE);
      put("objectProperty.nestedObjects.nestedNumbersLvl2", DataType.NUMBER_ARRAY);
    }};

    // schema created after adding objects
    utils.batchObjects(client1, utils.nestedObject1(className), utils.nestedObject2(className));
    schemaClass = utils.getClass(client1, className);
    utils.assertThatSchemaPropertiesHaveDataTypes(expectedProps, schemaClass);
    schemaClass = utils.getClass(client2, className);
    utils.assertThatSchemaPropertiesHaveDataTypes(expectedProps, schemaClass);
  }

  @Test
  public void shouldBatchObjectsWithNestedProperties_NoSchema2() {
    WeaviateClass schemaClass;
    String className = "ClassWithObjectProperty";
    Map<String, String> expectedProps = new HashMap<String, String>() {{
      put("name", DataType.TEXT);
      put("objectProperty", DataType.OBJECT);
      put("objectProperty.nestedInt", DataType.NUMBER); // autoschema determines type as number
      put("objectProperty.nestedNumber", DataType.NUMBER);
      put("objectProperty.nestedText", DataType.TEXT);
      put("objectProperty.nestedObjects", DataType.OBJECT_ARRAY);
      put("objectProperty.nestedObjects.nestedBoolLvl2", DataType.BOOLEAN);
      put("objectProperty.nestedObjects.nestedDateLvl2", DataType.DATE);
      put("objectProperty.nestedObjects.nestedNumbersLvl2", DataType.NUMBER_ARRAY);
    }};

    // schema created after adding objects
    utils.batchObjects(client1, utils.nestedObject2(className), utils.nestedObject1(className));
    schemaClass = utils.getClass(client1, className);
    utils.assertThatSchemaPropertiesHaveDataTypes(expectedProps, schemaClass);
    schemaClass = utils.getClass(client2, className);
    utils.assertThatSchemaPropertiesHaveDataTypes(expectedProps, schemaClass);
  }
}
