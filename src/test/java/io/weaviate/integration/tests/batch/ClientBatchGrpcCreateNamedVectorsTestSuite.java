package io.weaviate.integration.tests.batch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.ARRAY;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import io.weaviate.client.base.Result;
import io.weaviate.client.v1.batch.model.ObjectGetResponse;
import io.weaviate.client.v1.batch.model.ObjectsGetResponseAO2Result;
import io.weaviate.client.v1.data.model.WeaviateObject;
import io.weaviate.client.v1.misc.model.BQConfig;
import io.weaviate.client.v1.misc.model.MultiVectorConfig;
import io.weaviate.client.v1.misc.model.VectorIndexConfig;
import io.weaviate.client.v1.schema.model.DataType;
import io.weaviate.client.v1.schema.model.Property;
import io.weaviate.client.v1.schema.model.WeaviateClass;

public class ClientBatchGrpcCreateNamedVectorsTestSuite {

  public static void shouldCreateObjectsWithNamedVectors(Function<WeaviateClass, Result<Boolean>> classCreate,
      Function<WeaviateObject, Result<ObjectGetResponse[]>> batchCreate,
      Function<WeaviateObject, Result<List<WeaviateObject>>> fetch,
      Function<String, Result<Boolean>> deleteClass) {
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
    Map<String, Object> text2vecContextionarySettings = new HashMap<>();
    text2vecContextionarySettings.put("properties", new String[] { "title" });
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
    WeaviateClass weaviateClass = WeaviateClass.builder()
        .className(className)
        .properties(properties)
        .vectorConfig(vectorConfig)
        .build();
    // Supply
    Result<Boolean> createResult = classCreate.apply(weaviateClass);
    assertThat(createResult).isNotNull()
        .returns(false, Result::hasErrors)
        .returns(true, Result::getResult);

    // create object
    String id = "00000000-0000-0000-0000-000000000001";
    Map<String, Object> props = new HashMap<>();
    props.put("name", "some name");
    props.put("title", "The Lord of the Rings");
    Float[] vector = new Float[] { 0.11f, 0.22f, 0.33f, 0.123f, -0.900009f, -0.0000000001f };
    Map<String, Float[]> vectors = new HashMap<>();
    vectors.put("hnswVector", vector);
    WeaviateObject obj = WeaviateObject.builder()
        .id(id)
        .className(className)
        .properties(props)
        .vectors(vectors)
        .build();
    Result<ObjectGetResponse[]> result = batchCreate.apply(obj);
    assertThat(result).isNotNull()
        .returns(false, Result::hasErrors)
        .extracting(Result::getResult).asInstanceOf(ARRAY)
        .hasSize(1);

    // fetch that object
    Result<List<WeaviateObject>> resultObj = fetch.apply(obj);
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
    Result<Boolean> delete = deleteClass.apply(className);
    assertThat(delete).isNotNull()
        .returns(false, Result::hasErrors)
        .returns(true, Result::getResult);
  }

  public static void shouldCreateObjectsWithNamedMultiVectors(Function<WeaviateClass, Result<Boolean>> classCreate,
      Function<WeaviateObject, Result<ObjectGetResponse[]>> batchCreate,
      Function<WeaviateObject, Result<List<WeaviateObject>>> fetch,
      Function<String, Result<Boolean>> deleteClass) {

    // Arrange: Configure collection and create it
    String className = "NamedMultiVectors";
    WeaviateClass weaviateClass = WeaviateClass.builder()
        .className(className)
        .properties(Arrays.asList(
            Property.builder()
                .name("name")
                .dataType(Collections.singletonList(DataType.TEXT))
                .build(),
            Property.builder()
                .name("title")
                .dataType(Collections.singletonList(DataType.TEXT))
                .build()))
        .vectorConfig(new HashMap<String, WeaviateClass.VectorConfig>() {
          {
            this.put("regular", WeaviateClass.VectorConfig.builder()
                .vectorizer(new HashMap<String, Object>() {
                  {
                    this.put("none", new Object());
                  }
                })
                .vectorIndexType("hnsw")
                .build());
            this.put("colbert", WeaviateClass.VectorConfig.builder()
                .vectorizer(new HashMap<String, Object>() {
                  {
                    this.put("none", new Object());
                  }
                })
                .vectorIndexConfig(VectorIndexConfig.builder()
                    .multiVector(MultiVectorConfig.builder().build())
                    .build())
                .vectorIndexType("hnsw")
                .build());
          }
        })
        .build();

    Result<Boolean> createResult = classCreate.apply(weaviateClass);
    assertThat(createResult).isNotNull()
        .returns(null, Result::getError).as("create class error")
        .returns(true, Result::getResult).as("create class successful");

    // Arrange: Prepare test object
    String id = UUID.randomUUID().toString();
    Float[][] colbertVector = new Float[][] {
        { 0.11f, 0.22f, 0.33f, 0.123f, -0.900009f, -0.0000000001f },
        { 0.11f, 0.22f, 0.33f, 0.123f, -0.900009f, -0.0000000001f },
    };
    WeaviateObject testObject = WeaviateObject.builder()
        .id(id).className(className)
        .properties(new HashMap<String, Object>() {
          {
            this.put("name", "TestObject-1");
            this.put("title", "The Lord of the Rings");
          }
        })
        .vectors(new HashMap<String, Float[]>() {
          {
            this.put("regular", colbertVector[0]);
          }
        })
        .multiVectors(new HashMap<String, Float[][]>() {
          {
            this.put("colbert", colbertVector);
          }
        })
        .build();

    // Act: Run batch insert
    Result<ObjectGetResponse[]> result = batchCreate.apply(testObject);
    assertThat(result).isNotNull()
        .returns(null, Result::getError).as("batch insert error")
        .extracting(Result::getResult).asInstanceOf(ARRAY)
        .hasSize(1)
        .satisfies(obj -> {
          ObjectGetResponse response = ((ObjectGetResponse) obj[0]);

          assertThat(response).extracting(ObjectGetResponse::getResult)
              .satisfies(r -> {
                assertThat(r).extracting(ObjectsGetResponseAO2Result::getStatus)
                    .isEqualTo("SUCCESS").as("gRPC response status");
                assertThat(r).extracting(ObjectsGetResponseAO2Result::getErrors)
                    .as("gRPC errors").isNull();
              });
          assertThat(response.getMultiVectors()).containsKey("colbert");
        });

    // Assert: Retrieve object and check its dimensions
    Result<List<WeaviateObject>> resultObj = fetch.apply(testObject);
    assertThat(resultObj).isNotNull()
        .returns(null, Result::getError).as("fetch object error")
        .extracting(Result::getResult).isNotNull().as("result not null")
        .extracting(r -> r.get(0)).isNotNull().as("first object")
        .satisfies(o -> {
          assertThat(o.getId()).isEqualTo(id).as("ids match");

          // 1d vectors under "vectors"
          assertThat(o.getVectors()).isNotEmpty()
              .containsOnlyKeys("regular");

          // ColBERT vectors under "multiVectors"
          assertThat(o.getMultiVectors()).isNotEmpty()
              .containsOnlyKeys("colbert")
              .satisfies(vecs -> {
                assertThat(vecs.get("colbert")).isEqualTo(colbertVector)
                    .as("colbert vector");
              }).as("has expected vectors");

          assertThat(o.getProperties()).isNotNull()
              .extracting(Map::size).isEqualTo(testObject.getProperties().size())
              .as("has expected properties");

          testObject.getProperties().keySet().forEach(propName -> {
            assertThat(o.getProperties().get(propName))
                .isNotNull().as(propName);
          });
        }).as("expected object metadata");

    // clean up
    Result<Boolean> delete = deleteClass.apply(className);
    assertThat(delete).isNotNull()
        .returns(false, Result::hasErrors)
        .returns(true, Result::getResult);
  }
}
