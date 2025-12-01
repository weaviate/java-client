package io.weaviate.integration;

import java.io.IOException;
import java.util.Map;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TestRule;

import io.weaviate.ConcurrentTest;
import io.weaviate.client6.v1.api.WeaviateClient;
import io.weaviate.client6.v1.api.collections.Property;
import io.weaviate.client6.v1.api.collections.VectorConfig;
import io.weaviate.client6.v1.api.collections.WeaviateObject;
import io.weaviate.client6.v1.api.collections.query.FetchObjectById;
import io.weaviate.containers.Container;
import io.weaviate.containers.Model2Vec;
import io.weaviate.containers.Weaviate;

import static org.assertj.core.api.Assertions.assertThat;

public class VectorizersITest extends ConcurrentTest {
  private static final Container.ContainerGroup compose = Container.compose(
    Weaviate.custom()
      .withModel2VecUrl(Model2Vec.URL)
      .build(),
    Container.MODEL2VEC);
  @ClassRule // Bind containers to the lifetime of the test
  public static final TestRule _rule = compose.asTestRule();
  private static final WeaviateClient client = compose.getClient();

  @Test
  public void testVectorizerModel2VecPropeties() throws IOException {
    var collectionName = ns("Model2Vec2NamedVectors");
    client.collections.create(collectionName,
      col -> col
        .properties(Property.text("name"), Property.text("author"))
        .vectorConfig(
          VectorConfig.text2vecModel2Vec("name", v -> v.sourceProperties("name")),
          VectorConfig.text2vecModel2Vec("author", v -> v.sourceProperties("author"))
        )
    );

    var model2vec = client.collections.use(collectionName);
    assertThat(model2vec).isNotNull();

    String uuid1 = "00000000-0000-0000-0000-000000000001";
    WeaviateObject<Map<String, Object>> obj1 = WeaviateObject.of(o ->
      o.properties(Map.of("name", "Dune", "author", "Frank Herbert")).uuid(uuid1)
    );
    String uuid2 = "00000000-0000-0000-0000-000000000002";
    WeaviateObject<Map<String, Object>> obj2 = WeaviateObject.of(o ->
      o.properties(Map.of("name", "same content", "author", "same content")).uuid(uuid2)
    );

    var resp = model2vec.data.insertMany(obj1, obj2);
    assertThat(resp).isNotNull().satisfies(s -> {
      assertThat(s.errors()).isEmpty();
    });

    var o1 = model2vec.query.fetchObjectById(uuid1, FetchObjectById.Builder::includeVector);
    // Assert that for object1 we have generated 2 different vectors
    assertThat(o1).get()
      .extracting(WeaviateObject::vectors)
      .satisfies(v -> {
        assertThat(v.getSingle("name")).isNotEmpty();
        assertThat(v.getSingle("author")).isNotEmpty();
        assertThat(v.getSingle("name")).isNotEqualTo(v.getSingle("author"));
      });

    var o2 = model2vec.query.fetchObjectById(uuid2, FetchObjectById.Builder::includeVector);
    // Assert that for object2 we have generated same vectors
    assertThat(o2).get()
      .extracting(WeaviateObject::vectors)
      .satisfies(v -> {
        assertThat(v.getSingle("name")).isNotEmpty();
        assertThat(v.getSingle("author")).isNotEmpty();
        assertThat(v.getSingle("name")).isEqualTo(v.getSingle("author"));
      });
  }
}
