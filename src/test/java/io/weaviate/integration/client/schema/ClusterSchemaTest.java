package io.weaviate.integration.client.schema;

import io.weaviate.client.Config;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.WeaviateError;
import io.weaviate.client.base.WeaviateErrorMessage;
import io.weaviate.client.v1.misc.model.ReplicationConfig;
import io.weaviate.client.v1.schema.model.DataType;
import io.weaviate.client.v1.schema.model.WeaviateClass;
import io.weaviate.integration.client.WeaviateDockerComposeCluster;
import java.util.HashMap;
import java.util.Map;
import org.apache.http.HttpStatus;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.STRING;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;


public class ClusterSchemaTest {
  private WeaviateClient client1;
  private WeaviateClient client2;
  private final NestedObjectsUtils utils = new NestedObjectsUtils();

  @ClassRule
  public static WeaviateDockerComposeCluster compose = new WeaviateDockerComposeCluster();

  @Before
  public void before() {
    Config config1 = new Config("http", compose.getHttpHost0Address());
    client1 = new WeaviateClient(config1);

    Config config2 = new Config("http", compose.getHttpHost1Address());
    client2 = new WeaviateClient(config2);
  }

  @After
  public void after() {
    Result<Boolean> deleted = client1.schema().allDeleter().run();
    assertThat(deleted.hasErrors()).isFalse();
  }

  @Test
  public void shouldCreateClassWithImplicitReplicationFactor() {
    assertClassReplicationSettings(1, null);
  }

  @Test
  public void shouldCreateClassWithExplicitReplicationFactor() {
    assertClassReplicationSettings(2, null);
  }

  @Test
  public void shouldCreateClassWithExplicitAsyncReplicationWithImplicitReplicationFactor() {
    assertClassReplicationSettings(1, false);
  }

  @Test
  public void shouldCreateClassWithExplicitAsyncReplicationAndExplicitReplicationFactor() {
    assertClassReplicationSettings(2, true);
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

  private void assertClassReplicationSettings(int replicationFactor, Boolean asyncEnabled) {
    // given
    ReplicationConfig.ReplicationConfigBuilder replicationConfigBuilder = ReplicationConfig.builder().factor(replicationFactor);
    if (asyncEnabled != null) {
      replicationConfigBuilder = replicationConfigBuilder.asyncEnabled(asyncEnabled);
    }
    String className = "Band";
    WeaviateClass clazz = WeaviateClass.builder()
      .className(className)
      .description("Band that plays and produces music")
      .vectorIndexType("hnsw")
      .vectorizer("text2vec-contextionary")
      .replicationConfig(replicationConfigBuilder.build())
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
      .satisfies(rc -> {
        assertThat(rc.getFactor()).isEqualTo(replicationFactor);
        if (asyncEnabled != null) {
          assertThat(rc.getAsyncEnabled()).isEqualTo(asyncEnabled);
        } else {
          assertThat(rc.getAsyncEnabled()).isEqualTo(false);
        }
      });
  }
}
