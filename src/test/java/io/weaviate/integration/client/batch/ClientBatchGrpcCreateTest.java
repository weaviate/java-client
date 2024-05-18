package io.weaviate.integration.client.batch;

import io.weaviate.client.Config;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.batch.model.ObjectGetResponse;
import io.weaviate.client.v1.data.model.WeaviateObject;
import io.weaviate.client.v1.misc.model.BQConfig;
import io.weaviate.client.v1.misc.model.VectorIndexConfig;
import io.weaviate.client.v1.schema.model.Property;
import io.weaviate.client.v1.schema.model.WeaviateClass;
import io.weaviate.integration.client.WeaviateDockerCompose;
import io.weaviate.integration.client.WeaviateTestGenerics;
import java.util.List;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.ARRAY;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.platform.commons.util.StringUtils;

public class ClientBatchGrpcCreateTest {

  private static String httpHost;
  private static String grpcHost;

  @ClassRule
  public static WeaviateDockerCompose compose = new WeaviateDockerCompose();

  @Before
  public void before() {
    httpHost = compose.getHttpHostAddress();
    grpcHost = compose.getGrpcHostAddress();
  }


  @Test
  public void shouldCreateBatchUsingGRPC() {
    testCreateBatch(true);
  }

  @Test
  public void shouldCreateBatchWithNestedObjectUsingGRPC() {
    testCreateBatchWithNested(true);
  }

  @Test
  public void shouldCreateBatchWithNestedObjectAndNestedArrayObjectUsingGRPC() {
    testCreateBatchWithNestedAndNestArrayObject(true);
  }

  @Test
  public void shouldCreateBatchWithCrossReferencesUsingGRPC() {
    testCreateBatchWithReferenceWithoutNested(true);
  }

  @Test
  public void shouldCreateBatchWithMultiCrossReferencesUsingGRPC() {
    testCreateBatchWithMultiReferenceWithoutNested(true);
  }

  @Test
  public void shouldCreateBatchWithCrossReferencesWithNestedPropertiesUsingGRPC() {
    testCreateBatchWithReferenceWithNested(true);
  }

  @Test
  public void shouldCreateBatchWithMultiCrossReferencesWithNestedPropertiesUsingGRPC() {
    testCreateBatchWithMultiReferenceWithNested(true);
  }

  @Test
  public void shouldCreateBatchUsingGRPCWithFlatBQConfig() {
    testCreateBatchWithFlatVectorIndex(true);
  }

  @Test
  public void shouldCreateBatchUsingRest() {
    testCreateBatch(false);
  }

  @Test
  public void shouldCreateBatchWithNestedObjectUsingRest() {
    testCreateBatchWithNested(false);
  }

  @Test
  public void shouldCreateBatchWithNestedObjectAndNestedArrayObjectUsingRest() {
    testCreateBatchWithNestedAndNestArrayObject(false);
  }

  @Test
  public void shouldCreateBatchWithCrossReferencesUsingRest() {
    testCreateBatchWithReferenceWithoutNested(false);
  }

  @Test
  public void shouldCreateBatchWithMultiCrossReferencesUsingRest() {
    testCreateBatchWithMultiReferenceWithoutNested(false);
  }

  @Test
  public void shouldCreateBatchWithCrossReferencesWithNestedPropertiesUsingRest() {
    testCreateBatchWithReferenceWithNested(false);
  }

  @Test
  public void shouldCreateBatchWithMultiCrossReferencesWithNestedPropertiesUsingRest() {
    testCreateBatchWithMultiReferenceWithNested(false);
  }

  @Test
  public void shouldCreateBatchUsingRestWithFlatBQConfig() {
    testCreateBatchWithFlatVectorIndex(false);
  }

  private void testCreateBatchWithReferenceWithoutNested(Boolean useGRPC) {
    WeaviateClient client = createClient(useGRPC);
    WeaviateTestGenerics.AllPropertiesSchema testData = new WeaviateTestGenerics.AllPropertiesSchema();
    // create ref class and populate objects
    testData.createRefClassesWithObjects(client);
    // create all properties class
    String className = testData.CLASS_NAME;
    List<Property> properties = testData.propertiesWithCrossReference();
    WeaviateObject[] objects = testData.objectsWithCrossReferences();
    testCreateBatch(client, className, properties, objects);
    // delete ref class
    testData.deleteRefClasses(client);
  }

  private void testCreateBatchWithMultiReferenceWithoutNested(Boolean useGRPC) {
    WeaviateClient client = createClient(useGRPC);
    WeaviateTestGenerics.AllPropertiesSchema testData = new WeaviateTestGenerics.AllPropertiesSchema();
    // create ref class and populate objects
    testData.createRefClassesWithObjects(client);
    // create all properties class
    String className = testData.CLASS_NAME;
    List<Property> properties = testData.propertiesWithMultiCrossReference();
    WeaviateObject[] objects = testData.objectsWithMultiCrossReferences();
    testCreateBatch(client, className, properties, objects);
    // delete ref class
    testData.deleteRefClasses(client);
  }

  private void testCreateBatchWithReferenceWithNested(Boolean useGRPC) {
    WeaviateClient client = createClient(useGRPC);
    WeaviateTestGenerics.AllPropertiesSchema testData = new WeaviateTestGenerics.AllPropertiesSchema();
    // create ref class and populate objects
    testData.createRefClassesWithObjects(client);
    // create all properties class
    String className = testData.CLASS_NAME;
    List<Property> properties = testData.propertiesWithCrossReferenceWithNestedProperties();
    WeaviateObject[] objects = testData.objectsWithCrossReferencesWithNestedProperties();
    testCreateBatch(client, className, properties, objects);
    // delete ref class
    testData.deleteRefClasses(client);
  }

  private void testCreateBatchWithMultiReferenceWithNested(Boolean useGRPC) {
    WeaviateClient client = createClient(useGRPC);
    WeaviateTestGenerics.AllPropertiesSchema testData = new WeaviateTestGenerics.AllPropertiesSchema();
    // create ref class and populate objects
    testData.createRefClassesWithObjects(client);
    // create all properties class
    String className = testData.CLASS_NAME;
    List<Property> properties = testData.propertiesWithMultiCrossReferenceWithNestedProperties();
    WeaviateObject[] objects = testData.objectsWithMultiCrossReferencesWithNestedProperties();
    testCreateBatch(client, className, properties, objects);
    // delete ref class
    testData.deleteRefClasses(client);
  }

  private void testCreateBatch(Boolean useGRPC, String vectorIndexType, VectorIndexConfig vectorIndexConfig) {
    WeaviateClient client = createClient(useGRPC);
    WeaviateTestGenerics.AllPropertiesSchema testData = new WeaviateTestGenerics.AllPropertiesSchema();
    String className = testData.CLASS_NAME;
    List<Property> properties = testData.properties();
    WeaviateObject[] objects = testData.objects();
    testCreateBatch(client, className, properties, objects, vectorIndexType, vectorIndexConfig);
  }

  private void testCreateBatchWithFlatVectorIndex(Boolean useGRPC) {
    VectorIndexConfig vectorIndexConfig = VectorIndexConfig.builder()
      .bq(BQConfig.builder().enabled(true).build())
      .build();
    testCreateBatch(useGRPC, "flat", vectorIndexConfig);
  }

  private void testCreateBatch(Boolean useGRPC) {
    testCreateBatch(useGRPC, null, null);
  }

  private void testCreateBatchWithNested(Boolean useGRPC) {
    WeaviateClient client = createClient(useGRPC);
    WeaviateTestGenerics.AllPropertiesSchema testData = new WeaviateTestGenerics.AllPropertiesSchema();
    String className = testData.CLASS_NAME;
    List<Property> properties = testData.propertiesWithNestedObject();
    WeaviateObject[] objects = testData.objectsWithNestedObject();
    testCreateBatch(client, className, properties, objects);
  }

  private void testCreateBatchWithNestedAndNestArrayObject(Boolean useGRPC) {
    WeaviateClient client = createClient(useGRPC);
    WeaviateTestGenerics.AllPropertiesSchema testData = new WeaviateTestGenerics.AllPropertiesSchema();
    String className = testData.CLASS_NAME;
    List<Property> properties = testData.propertiesWithNestedObjectAndNestedArrayObject();
    WeaviateObject[] objects = testData.objectsWithNestedObjectAndNestedArrayObject();
    testCreateBatch(client, className, properties, objects);
  }

  private WeaviateClient createClient(Boolean useGRPC) {
    Config config = new Config("http", httpHost);
    if (useGRPC) {
      config.setGRPCSecured(false);
      config.setGRPCHost(grpcHost);
    }
    return new WeaviateClient(config);
  }

  private void testCreateBatch(WeaviateClient client, String className, List<Property> properties, WeaviateObject[] objects) {
    testCreateBatch(client, className, properties, objects, null, null);
  }

  private void testCreateBatch(WeaviateClient client, String className, List<Property> properties, WeaviateObject[] objects,
    String vectorIndexType, VectorIndexConfig vectorIndexConfig) {
    // create schema
    WeaviateClass.WeaviateClassBuilder weaviateClassBuilder = WeaviateClass.builder()
      .className(className)
      .properties(properties);
    if (StringUtils.isNotBlank(vectorIndexType) && vectorIndexConfig != null) {
      weaviateClassBuilder.vectorIndexType(vectorIndexType).vectorIndexConfig(vectorIndexConfig);
    }
    Result<Boolean> createResult = client.schema().classCreator()
      .withClass(weaviateClassBuilder.build())
      .run();
    assertThat(createResult).isNotNull()
      .returns(false, Result::hasErrors)
      .returns(true, Result::getResult);

    Result<ObjectGetResponse[]> result = client.batch().objectsBatcher()
      .withObjects(objects)
      .run();
    assertThat(result).isNotNull()
      .returns(false, Result::hasErrors)
      .extracting(Result::getResult).asInstanceOf(ARRAY)
      .hasSize(objects.length);

    for (WeaviateObject obj : objects) {
      Result<List<WeaviateObject>> resultObj = client.data().objectsGetter()
        .withID(obj.getId()).withClassName(obj.getClassName()).withVector()
        .run();
      assertThat(resultObj).isNotNull()
        .returns(false, Result::hasErrors)
        .extracting(Result::getResult).isNotNull()
        .extracting(r -> r.get(0)).isNotNull()
        .satisfies(o -> {
          assertThat(o.getId()).isEqualTo(obj.getId());
          assertThat(o.getVector()).isNotEmpty();
          assertThat(o.getProperties()).isNotNull()
            .extracting(Map::size).isEqualTo(obj.getProperties().size());
          obj.getProperties().keySet().forEach(propName -> {
            assertThat(o.getProperties().get(propName)).isNotNull();
          });
        });
    }
    // clean up
    Result<Boolean> delete = client.schema().classDeleter().withClassName(className).run();
    assertThat(delete).isNotNull()
      .returns(false, Result::hasErrors)
      .extracting(Result::getResult).isEqualTo(Boolean.TRUE);
  }
}
