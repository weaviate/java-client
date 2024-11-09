package io.weaviate.integration.tests.batch;

import io.weaviate.client.base.Result;
import io.weaviate.client.v1.batch.model.ObjectGetResponse;
import io.weaviate.client.v1.data.model.WeaviateObject;
import io.weaviate.client.v1.misc.model.BQConfig;
import io.weaviate.client.v1.misc.model.VectorIndexConfig;
import io.weaviate.client.v1.schema.model.DataType;
import io.weaviate.client.v1.schema.model.Property;
import io.weaviate.client.v1.schema.model.WeaviateClass;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.ARRAY;

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
    Float[] vector = new Float[]{0.11f, 0.22f, 0.33f, 0.123f, -0.900009f, -0.0000000001f};
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
}
