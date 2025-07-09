package io.weaviate.integration.tests.batch;

import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.batch.model.ObjectGetResponse;
import io.weaviate.client.v1.data.model.WeaviateObject;
import io.weaviate.client.v1.misc.model.BQConfig;
import io.weaviate.client.v1.misc.model.SQConfig;
import io.weaviate.client.v1.misc.model.RQConfig;
import io.weaviate.client.v1.misc.model.VectorIndexConfig;
import io.weaviate.client.v1.schema.model.Property;
import io.weaviate.client.v1.schema.model.WeaviateClass;
import io.weaviate.integration.client.WeaviateTestGenerics;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.ARRAY;
import org.junit.platform.commons.util.StringUtils;

public class ClientBatchGrpcCreateTestSuite {

  public static void shouldCreateBatch(WeaviateClient client,
      Function<WeaviateClass, Result<Boolean>> createClass,
      Function<WeaviateObject[], Result<ObjectGetResponse[]>> batchCreate,
      Function<WeaviateObject, Result<List<WeaviateObject>>> fetchObject,
      Function<String, Result<Boolean>> deleteClass) {

    testSuite t = new testSuite(client, createClass, batchCreate, fetchObject, deleteClass);

    t.testCreateBatch();
    t.testCreateBatchWithNested();
    t.testCreateBatchWithNestedAndNestArrayObject();
    t.testCreateBatchWithReferenceWithoutNested();
    t.testCreateBatchWithMultiReferenceWithoutNested();
    t.testCreateBatchWithReferenceWithNested();
    t.testCreateBatchWithMultiReferenceWithNested();
    t.testCreateBatchWithFlatVectorIndex();
    t.testCreateBatchWithHNSWSQVectorIndex();
    t.testCreateBatchWithHNSWRQVectorIndex();
  }

  private static class testSuite {

    private final WeaviateClient client;
    private final Function<WeaviateClass, Result<Boolean>> createClass;
    private final Function<WeaviateObject[], Result<ObjectGetResponse[]>> batchCreate;
    private final Function<WeaviateObject, Result<List<WeaviateObject>>> fetchObject;
    private final Function<String, Result<Boolean>> deleteClass;

    public testSuite(WeaviateClient client, Function<WeaviateClass, Result<Boolean>> createClass,
        Function<WeaviateObject[], Result<ObjectGetResponse[]>> batchCreate,
        Function<WeaviateObject, Result<List<WeaviateObject>>> fetchObject,
        Function<String, Result<Boolean>> deleteClass) {
      this.client = client;
      this.createClass = createClass;
      this.batchCreate = batchCreate;
      this.fetchObject = fetchObject;
      this.deleteClass = deleteClass;
    }

    public void testCreateBatchWithReferenceWithoutNested() {
      WeaviateTestGenerics.AllPropertiesSchema testData =
          new WeaviateTestGenerics.AllPropertiesSchema();
      // create ref class and populate objects
      testData.createRefClassesWithObjects(client);
      // create all properties class
      String className = testData.CLASS_NAME;
      List<Property> properties = testData.propertiesWithCrossReference();
      WeaviateObject[] objects = testData.objectsWithCrossReferences();
      testCreateBatch(className, properties, objects);
      // delete ref class
      testData.deleteRefClasses(client);
    }

    public void testCreateBatchWithMultiReferenceWithoutNested() {
      WeaviateTestGenerics.AllPropertiesSchema testData =
          new WeaviateTestGenerics.AllPropertiesSchema();
      // create ref class and populate objects
      testData.createRefClassesWithObjects(client);
      // create all properties class
      String className = testData.CLASS_NAME;
      List<Property> properties = testData.propertiesWithMultiCrossReference();
      WeaviateObject[] objects = testData.objectsWithMultiCrossReferences();
      testCreateBatch(className, properties, objects);
      // delete ref class
      testData.deleteRefClasses(client);
    }

    public void testCreateBatchWithReferenceWithNested() {
      WeaviateTestGenerics.AllPropertiesSchema testData =
          new WeaviateTestGenerics.AllPropertiesSchema();
      // create ref class and populate objects
      testData.createRefClassesWithObjects(client);
      // create all properties class
      String className = testData.CLASS_NAME;
      List<Property> properties = testData.propertiesWithCrossReferenceWithNestedProperties();
      WeaviateObject[] objects = testData.objectsWithCrossReferencesWithNestedProperties();
      testCreateBatch(className, properties, objects);
      // delete ref class
      testData.deleteRefClasses(client);
    }

    public void testCreateBatchWithMultiReferenceWithNested() {
      WeaviateTestGenerics.AllPropertiesSchema testData =
          new WeaviateTestGenerics.AllPropertiesSchema();
      // create ref class and populate objects
      testData.createRefClassesWithObjects(client);
      // create all properties class
      String className = testData.CLASS_NAME;
      List<Property> properties = testData.propertiesWithMultiCrossReferenceWithNestedProperties();
      WeaviateObject[] objects = testData.objectsWithMultiCrossReferencesWithNestedProperties();
      testCreateBatch(className, properties, objects);
      // delete ref class
      testData.deleteRefClasses(client);
    }

    public void testCreateBatch(String vectorIndexType, VectorIndexConfig vectorIndexConfig) {
      WeaviateTestGenerics.AllPropertiesSchema testData =
          new WeaviateTestGenerics.AllPropertiesSchema();
      String className = testData.CLASS_NAME;
      List<Property> properties = testData.properties();
      WeaviateObject[] objects = testData.objects();
      testCreateBatch(className, properties, objects, vectorIndexType, vectorIndexConfig);
    }

    public void testCreateBatchWithFlatVectorIndex() {
      VectorIndexConfig vectorIndexConfig =
          VectorIndexConfig.builder().bq(BQConfig.builder().enabled(true).build()).build();
      testCreateBatch("flat", vectorIndexConfig);
    }

    public void testCreateBatchWithHNSWSQVectorIndex() {
      VectorIndexConfig vectorIndexConfig =
          VectorIndexConfig.builder().sq(SQConfig.builder().enabled(true).build()).build();
      testCreateBatch("hnsw", vectorIndexConfig);
    }

    public void testCreateBatchWithHNSWRQVectorIndex() {
      VectorIndexConfig vectorIndexConfig =
          VectorIndexConfig.builder().rq(RQConfig.builder().enabled(true).build()).build();
      testCreateBatch("hnsw", vectorIndexConfig);
    }

    public void testCreateBatch() {
      testCreateBatch(null, null);
    }

    public void testCreateBatchWithNested() {
      WeaviateTestGenerics.AllPropertiesSchema testData =
          new WeaviateTestGenerics.AllPropertiesSchema();
      String className = testData.CLASS_NAME;
      List<Property> properties = testData.propertiesWithNestedObject();
      WeaviateObject[] objects = testData.objectsWithNestedObject();
      testCreateBatch(className, properties, objects);
    }

    public void testCreateBatchWithNestedAndNestArrayObject() {
      WeaviateTestGenerics.AllPropertiesSchema testData =
          new WeaviateTestGenerics.AllPropertiesSchema();
      String className = testData.CLASS_NAME;
      List<Property> properties = testData.propertiesWithNestedObjectAndNestedArrayObject();
      WeaviateObject[] objects = testData.objectsWithNestedObjectAndNestedArrayObject();
      testCreateBatch(className, properties, objects);
    }

    public void testCreateBatch(String className, List<Property> properties,
        WeaviateObject[] objects) {
      testCreateBatch(className, properties, objects, null, null);
    }

    public void testCreateBatch(String className, List<Property> properties,
        WeaviateObject[] objects, String vectorIndexType, VectorIndexConfig vectorIndexConfig) {
      // create schema
      WeaviateClass.WeaviateClassBuilder weaviateClassBuilder =
          WeaviateClass.builder().className(className).properties(properties);
      if (StringUtils.isNotBlank(vectorIndexType) && vectorIndexConfig != null) {
        weaviateClassBuilder.vectorIndexType(vectorIndexType).vectorIndexConfig(vectorIndexConfig);
      }
      Result<Boolean> createResult = this.createClass.apply(weaviateClassBuilder.build());
      assertThat(createResult).isNotNull().returns(false, Result::hasErrors).returns(true,
          Result::getResult);

      Result<ObjectGetResponse[]> result = this.batchCreate.apply(objects);
      assertThat(result).isNotNull().returns(false, Result::hasErrors).extracting(Result::getResult)
          .asInstanceOf(ARRAY).hasSize(objects.length);

      for (WeaviateObject obj : objects) {
        Result<List<WeaviateObject>> resultObj = fetchObject.apply(obj);
        assertThat(resultObj).isNotNull().returns(false, Result::hasErrors)
            .extracting(Result::getResult).isNotNull().extracting(r -> r.get(0)).isNotNull()
            .satisfies(o -> {
              assertThat(o.getId()).isEqualTo(obj.getId());
              assertThat(o.getVector()).isNotEmpty();
              assertThat(o.getProperties()).isNotNull().extracting(Map::size)
                  .isEqualTo(obj.getProperties().size());
              obj.getProperties().keySet().forEach(propName -> {
                assertThat(o.getProperties().get(propName)).isNotNull();
              });
            });
      }
      // clean up
      Result<Boolean> delete = this.deleteClass.apply(className);
      assertThat(delete).isNotNull().returns(false, Result::hasErrors).extracting(Result::getResult)
          .isEqualTo(Boolean.TRUE);
    }
  }
}
