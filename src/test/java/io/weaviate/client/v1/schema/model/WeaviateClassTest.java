package io.weaviate.client.v1.schema.model;

import com.google.gson.GsonBuilder;
import io.weaviate.client.v1.misc.model.BQConfig;
import io.weaviate.client.v1.misc.model.VectorIndexConfig;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.MAP;

public class WeaviateClassTest {

  @Test
  public void shouldReturnModuleConfigSetWithLowerCase() {
    WeaviateClass clazz = WeaviateClass.builder()
      .moduleConfig(createDummyModuleConfig())
      .build();

    Object moduleConfig = clazz.getModuleConfig();

    assertThat(moduleConfig)
      .asInstanceOf(MAP)
      .containsOnlyKeys("text2vec-contextionary");
  }

  @Test
  public void shouldReturnModuleConfigSetWithUpperCase() {
    WeaviateClass clazz = WeaviateClass.builder()
      .ModuleConfig(createDummyModuleConfig())
      .build();

    Object moduleConfig = clazz.getModuleConfig();

    assertThat(moduleConfig)
      .asInstanceOf(MAP)
      .containsOnlyKeys("text2vec-contextionary");
  }

  @Test
  public void shouldSerializeClass() {
    WeaviateClass clazz = WeaviateClass.builder()
      .moduleConfig(createDummyModuleConfig())
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
    BQConfig bqConfig = BQConfig.builder().enabled(true).rescoreLimit(100l).build();
    VectorIndexConfig vectorIndexConfig = VectorIndexConfig.builder()
      .bq(bqConfig)
      .build();
    WeaviateClass clazz = WeaviateClass.builder()
      .moduleConfig(createDummyModuleConfig())
      .className("Band")
      .description("Band that plays and produces music")
      .vectorIndexType("flat")
      .vectorIndexConfig(vectorIndexConfig)
      .vectorizer("text2vec-contextionary")
      .build();

    String result = new GsonBuilder().create().toJson(clazz);

    assertThat(result).isEqualTo("{\"class\":\"Band\",\"description\":\"Band that plays and produces music\"," +
      "\"moduleConfig\":{\"text2vec-contextionary\":{\"vectorizeClassName\":false}}," +
      "\"vectorIndexConfig\":{\"bq\":{\"enabled\":true,\"rescoreLimit\":100}},\"vectorIndexType\":\"flat\"," +
      "\"vectorizer\":\"text2vec-contextionary\"}");
  }

  private Object createDummyModuleConfig() {
    Map<String, Object> text2vecContextionary = new HashMap<>();
    text2vecContextionary.put("vectorizeClassName", false);

    Map<String, Object> moduleConfig = new HashMap<>();
    moduleConfig.put("text2vec-contextionary", text2vecContextionary);

    return moduleConfig;
  }
}
