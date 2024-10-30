package io.weaviate.client.v1.schema.model;

import com.google.gson.GsonBuilder;
import io.weaviate.client.v1.misc.model.BQConfig;
import io.weaviate.client.v1.misc.model.VectorIndexConfig;
import java.util.HashMap;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.MAP;
import org.junit.Test;

public class WeaviateClassTest {

  @Test
  public void shouldReturnModuleConfigSetWithLowerCase() {
    WeaviateClass clazz = WeaviateClass.builder()
      .moduleConfig(createContextionaryModuleConfig())
      .build();

    Object moduleConfig = clazz.getModuleConfig();

    assertThat(moduleConfig)
      .asInstanceOf(MAP)
      .containsOnlyKeys("text2vec-contextionary");
  }

  @Test
  public void shouldReturnModuleConfigSetWithUpperCase() {
    WeaviateClass clazz = WeaviateClass.builder()
      .ModuleConfig(createContextionaryModuleConfig())
      .build();

    Object moduleConfig = clazz.getModuleConfig();

    assertThat(moduleConfig)
      .asInstanceOf(MAP)
      .containsOnlyKeys("text2vec-contextionary");
  }

  @Test
  public void shouldSerializeClass() {
    WeaviateClass clazz = WeaviateClass.builder()
      .moduleConfig(createContextionaryModuleConfig())
      .className("Band")
      .description("Band that plays and produces music")
      .vectorIndexType("hnsw")
      .vectorizer("text2vec-contextionary")
      .build();

    String result = new GsonBuilder().create().toJson(clazz);

    assertThat(result).isEqualTo("{\"class\":\"Band\",\"description\":\"Band that plays and produces music\"," +
      "\"moduleConfig\":{\"text2vec-contextionary\":{\"vectorizeClassName\":false}},\"vectorIndexType\":\"hnsw\"," +
      "\"vectorizer\":\"text2vec-contextionary\"}");
  }


  @Test
  public void shouldSerializeClassWithFlatIndexType() {
    WeaviateClass clazz = WeaviateClass.builder()
      .moduleConfig(createContextionaryModuleConfig())
      .className("Band")
      .description("Band that plays and produces music")
      .vectorIndexType("flat")
      .vectorIndexConfig(createBqIndexConfig())
      .vectorizer("text2vec-contextionary")
      .build();

    String result = new GsonBuilder().create().toJson(clazz);

    assertThat(result).isEqualTo("{\"class\":\"Band\",\"description\":\"Band that plays and produces music\"," +
      "\"moduleConfig\":{\"text2vec-contextionary\":{\"vectorizeClassName\":false}}," +
      "\"vectorIndexConfig\":{\"bq\":{\"enabled\":true,\"rescoreLimit\":100}},\"vectorIndexType\":\"flat\"," +
      "\"vectorizer\":\"text2vec-contextionary\"}");
  }

  @Test
  public void shouldSerializeClassWithVectorConfig() {
    Map<String, Object> contextionaryVectorizer = new HashMap<>();
    contextionaryVectorizer.put("text2vec-contextionary", "some-setting");

    WeaviateClass.VectorConfig hnswVectorConfig = WeaviateClass.VectorConfig.builder()
      .vectorIndexType("hnsw")
      .vectorizer(contextionaryVectorizer)
      .build();
    WeaviateClass.VectorConfig flatVectorConfig = WeaviateClass.VectorConfig.builder()
      .vectorIndexType("flat")
      .vectorizer(contextionaryVectorizer)
      .vectorIndexConfig(createBqIndexConfig())
      .build();

    Map<String, WeaviateClass.VectorConfig> vectorConfig = new HashMap<>();
    vectorConfig.put("flatVector", flatVectorConfig);
    vectorConfig.put("hnswVector", hnswVectorConfig);

    WeaviateClass clazz = WeaviateClass.builder()
      .moduleConfig(createContextionaryModuleConfig())
      .className("Band")
      .description("Band that plays and produces music")
      .vectorConfig(vectorConfig)
      .build();

    String result = new GsonBuilder().create().toJson(clazz);

    assertThat(result).satisfiesAnyOf(
      serialized -> assertThat(serialized).isEqualTo("{\"class\":\"Band\"," +
        "\"description\":\"Band that plays and produces music\"," +
        "\"moduleConfig\":{\"text2vec-contextionary\":{\"vectorizeClassName\":false}}," +
        "\"vectorConfig\":{" +
        "\"hnswVector\":{\"vectorIndexType\":\"hnsw\",\"vectorizer\":{\"text2vec-contextionary\":\"some-setting\"}}," +
        "\"flatVector\":{\"vectorIndexConfig\":{\"bq\":{\"enabled\":true,\"rescoreLimit\":100}},\"vectorIndexType\":\"flat\"," +
        "\"vectorizer\":{\"text2vec-contextionary\":\"some-setting\"}}" +
        "}}"),
      serialized -> assertThat(serialized).isEqualTo("{\"class\":\"Band\"," +
        "\"description\":\"Band that plays and produces music\"," +
        "\"moduleConfig\":{\"text2vec-contextionary\":{\"vectorizeClassName\":false}}," +
        "\"vectorConfig\":{" +
        "\"flatVector\":{\"vectorIndexConfig\":{\"bq\":{\"enabled\":true,\"rescoreLimit\":100}},\"vectorIndexType\":\"flat\"," +
        "\"vectorizer\":{\"text2vec-contextionary\":\"some-setting\"}}" +
        "\"hnswVector\":{\"vectorIndexType\":\"hnsw\",\"vectorizer\":{\"text2vec-contextionary\":\"some-setting\"}}," +
        "}}")
    );
  }

  private Object createContextionaryModuleConfig() {
    Map<String, Object> text2vecContextionary = new HashMap<>();
    text2vecContextionary.put("vectorizeClassName", false);

    Map<String, Object> moduleConfig = new HashMap<>();
    moduleConfig.put("text2vec-contextionary", text2vecContextionary);

    return moduleConfig;
  }

  private VectorIndexConfig createBqIndexConfig() {
    return VectorIndexConfig.builder()
      .bq(BQConfig.builder()
        .enabled(true)
        .rescoreLimit(100L)
        .build())
      .build();
  }
}
