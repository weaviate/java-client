package io.weaviate.client6.v1.internal.json;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.function.BiConsumer;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.jparams.junit4.JParamsTestRunner;
import com.jparams.junit4.data.DataMethod;

import io.weaviate.client6.v1.api.collections.ObjectMetadata;
import io.weaviate.client6.v1.api.collections.WeaviateObject;

/** Unit tests for custom POJO-to-JSON serialization. */
@RunWith(JParamsTestRunner.class)
public class JSONTest {
  public static Object[][] testCases() {
    return new Object[][] {
        // // Vectorizer.CustomTypeAdapterFactory
        // {
        // Vectorizer.class,
        // new NoneVectorizer(),
        // "{\"none\": {}}",
        // },
        // {
        // Vectorizer.class,
        // Img2VecNeuralVectorizer.of(i2v -> i2v.imageFields("jpeg", "png")),
        // """
        // {"img2vec-neural": {
        // "imageFields": ["jpeg", "png"]
        // }}
        // """,
        // },
        // {
        // Vectorizer.class,
        // Multi2VecClipVectorizer.of(m2v -> m2v
        // .inferenceUrl("http://example.com")
        // .imageField("img", 1f)
        // .textField("txt", 2f)
        // .vectorizeCollectionName(true)),
        // """
        // {"multi2vec-clip": {
        // "inferenceUrl": "http://example.com",
        // "vectorizeCollectionName": true,
        // "imageFields": ["img"],
        // "textFields": ["txt"],
        // "weights": {
        // "imageWeights": [1.0],
        // "textWeights": [2.0]
        // }
        // }}
        // """,
        // },
        // {
        // Vectorizer.class,
        // Text2VecWeaviateVectorizer.of(t2v -> t2v
        // .inferenceUrl("http://example.com")
        // .dimensions(4)
        // .model("very-good-model")
        // .vectorizeCollectionName(true)),
        // """
        // {"text2vec-weaviate": {
        // "baseUrl": "http://example.com",
        // "vectorizeCollectionName": true,
        // "dimensions": 4,
        // "model": "very-good-model"
        // }}
        // """,
        // },
        //
        // // VectorIndex.CustomTypeAdapterFactory
        // {
        // VectorIndex.class,
        // Flat.of(new NoneVectorizer(), flat -> flat
        // .vectorCacheMaxObjects(100)),
        // """
        // {
        // "vectorIndexType": "flat",
        // "vectorizer": {"none": {}},
        // "vectorIndexConfig": {"vectorCacheMaxObjects": 100}
        // }
        // """,
        // },
        // {
        // VectorIndex.class,
        // Hnsw.of(new NoneVectorizer(), hnsw -> hnsw
        // .distance(Distance.DOT)
        // .ef(1)
        // .efConstruction(2)
        // .maxConnections(3)
        // .vectorCacheMaxObjects(4)
        // .cleanupIntervalSeconds(5)
        // .dynamicEfMin(6)
        // .dynamicEfMax(7)
        // .dynamicEfFactor(8)
        // .flatSearchCutoff(9)
        // .skipVectorization(true)
        // .filterStrategy(Hnsw.FilterStrategy.ACORN)),
        // """
        // {
        // "vectorIndexType": "hnsw",
        // "vectorizer": {"none": {}},
        // "vectorIndexConfig": {
        // "distance": "dot",
        // "ef": 1,
        // "efConstruction": 2,
        // "maxConnections": 3,
        // "vectorCacheMaxObjects": 4,
        // "cleanupIntervalSeconds": 5,
        // "dynamicEfMin": 6,
        // "dynamicEfMax": 7,
        // "dynamicEfFactor": 8,
        // "flatSearchCutoff": 9,
        // "skip": true,
        // "filterStrategy":"acorn"
        // }
        // }
        // """,
        // },
        //
        // // Vectors.CustomTypeAdapterFactory
        // {
        // Vectors.class,
        // Vectors.of(new Float[] { 1f, 2f }),
        // "{\"default\": [1.0, 2.0]}",
        // (CustomAssert) JSONTest::compareVectors,
        // },
        // {
        // Vectors.class,
        // Vectors.of(new Float[][] { { 1f, 2f }, { 3f, 4f } }),
        // "{\"default\": [[1.0, 2.0], [3.0, 4.0]]}",
        // (CustomAssert) JSONTest::compareVectors,
        // },
        // {
        // Vectors.class,
        // Vectors.of("custom", new Float[] { 1f, 2f }),
        // "{\"custom\": [1.0, 2.0]}",
        // (CustomAssert) JSONTest::compareVectors,
        // },
        // {
        // Vectors.class,
        // Vectors.of("custom", new Float[][] { { 1f, 2f }, { 3f, 4f } }),
        // "{\"custom\": [[1.0, 2.0], [3.0, 4.0]]}",
        // (CustomAssert) JSONTest::compareVectors,
        // },
        // {
        // Vectors.class,
        // Vectors.of(named -> named
        // .vector("1d", new Float[] { 1f, 2f })
        // .vector("2d", new Float[][] { { 1f, 2f }, { 3f, 4f } })),
        // "{\"1d\": [1.0, 2.0], \"2d\": [[1.0, 2.0], [3.0, 4.0]]}",
        // (CustomAssert) JSONTest::compareVectors,
        // },
        //
        // // WeaviateCollection.CustomTypeAdapterFactory
        // {
        // WeaviateCollection.class,
        // WeaviateCollection.of("Things", things -> things
        // .description("A collection of things")
        // .properties(
        // Property.text("shape"),
        // Property.integer("size"))
        // .references(
        // Property.reference("owner", "Person", "Company"))
        // .vectors(named -> named
        // .vector("v-shape", Hnsw.of(new NoneVectorizer())))),
        // """
        // {
        // "class": "Things",
        // "description": "A collection of things",
        // "properties": [
        // {"name": "shape", "dataType": ["text"]},
        // {"name": "size", "dataType": ["int"]},
        // {"name": "owner", "dataType": ["Person", "Company"]}
        // ],
        // "vectorConfig": {
        // "v-shape": {
        // "vectorIndexType": "hnsw",
        // "vectorIndexConfig": {},
        // "vectorizer": {"none": {}}
        // }
        // }
        // }
        // """,
        // },
        //
        // // Reference.TYPE_ADAPTER
        // {
        // Reference.class,
        // Reference.uuids("id-1"),
        // "{\"beacon\": \"weaviate://localhost/id-1\"}",
        // },
        // {
        // Reference.class,
        // Reference.collection("Doodlebops", "id-1"),
        // "{\"beacon\": \"weaviate://localhost/Doodlebops/id-1\"}",
        // },

        // WeaviateObject.CustomTypeAdapterFactory.INSTANCE
        {
            new TypeToken<WeaviateObject<Map<String, Object>, ObjectMetadata>>() {
            },
            new WeaviateObject<Map<String, Object>, ObjectMetadata>(
                "Things",
                Map.of("title", "ThingOne"),
                Collections.emptyMap(),
                ObjectMetadata.of(meta -> meta.id("thing-1"))),
            """
                {
                  "class": "Things",
                  "properties": {
                    "title": "ThingOne"
                  },
                  "references": {},
                  "id": "thing-1"
                }
                  """,
        },
    };
  }

  @Test
  @DataMethod(source = JSONTest.class, method = "testCases")
  public void test_serialize(Object cls, Object in, String want) {
    String got;
    if (cls instanceof TypeToken typeToken) {
      got = JSON.serialize(in, typeToken);
    } else {
      got = JSON.serialize(in);
    }
    assertEqualJson(want, got);

  }

  private interface CustomAssert extends BiConsumer<Object, Object> {
  }

  @Test
  @SuppressWarnings("unchecked")
  @DataMethod(source = JSONTest.class, method = "testCases")
  public void test_deserialize(Object target, Object want, String in, CustomAssert assertion) {

    Object got;
    if (target instanceof Class targetClass) {
      got = JSON.deserialize(in, targetClass);
    } else if (target instanceof TypeToken targetToken) {
      got = JSON.deserialize(in, targetToken);
    } else {
      throw new IllegalArgumentException("target must be either Class<?> or TypeToken<?>");
    }

    if (assertion != null) {
      assertion.accept(got, want);
    } else {
      Assertions.assertThat(got).isEqualTo(want);
    }
  }

  private static void assertEqualJson(String want, String got) {
    var wantJson = JsonParser.parseString(want);
    var gotJson = JsonParser.parseString(got);
    Assertions.assertThat(gotJson).isEqualTo(wantJson);
  }

  /**
   * Custom assert function that uses deep array equality
   * to correctly compare Float[] and Float[][] nested in the object.
   */
  private static void compareVectors(Object got, Object want) {
    Assertions.assertThat(got)
        .usingRecursiveComparison()
        .withEqualsForType(Arrays::equals, Float[].class)
        .withEqualsForType(Arrays::deepEquals, Float[][].class)
        .isEqualTo(want);
  }
}
