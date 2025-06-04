package io.weaviate.client6.v1.internal.json;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.gson.JsonParser;
import com.jparams.junit4.JParamsTestRunner;
import com.jparams.junit4.data.DataMethod;

import io.weaviate.client6.v1.api.collections.VectorIndex;
import io.weaviate.client6.v1.api.collections.Vectorizer;
import io.weaviate.client6.v1.api.collections.vectorindex.Distance;
import io.weaviate.client6.v1.api.collections.vectorindex.Flat;
import io.weaviate.client6.v1.api.collections.vectorindex.Hnsw;
import io.weaviate.client6.v1.api.collections.vectorizers.Img2VecNeuralVectorizer;
import io.weaviate.client6.v1.api.collections.vectorizers.Multi2VecClipVectorizer;
import io.weaviate.client6.v1.api.collections.vectorizers.NoneVectorizer;
import io.weaviate.client6.v1.api.collections.vectorizers.Text2VecWeaviateVectorizer;

/** Unit tests for custom POJO-to-JSON serialization. */
@RunWith(JParamsTestRunner.class)
public class JSONTest {
  public static Object[][] testCases() {
    return new Object[][] {
        // Vectorizer.CustomTypeAdapterFactory
        {
            Vectorizer.class,
            new NoneVectorizer(),
            "{\"none\": {}}",
        },
        {
            Vectorizer.class,
            Img2VecNeuralVectorizer.of(i2v -> i2v.imageFields("jpeg", "png")),
            """
                {"img2vec-neural": {
                  "imageFields": ["jpeg", "png"]
                }}
                """,
        },
        {
            Vectorizer.class,
            Multi2VecClipVectorizer.of(m2v -> m2v
                .inferenceUrl("http://example.com")
                .imageField("img", 1f)
                .textField("txt", 2f)
                .vectorizeCollectionName(true)),
            """
                {"multi2vec-clip": {
                  "inferenceUrl": "http://example.com",
                  "vectorizeCollectionName": true,
                  "imageFields": ["img"],
                  "textFields": ["txt"],
                  "weights": {
                    "imageWeights": [1.0],
                    "textWeights": [2.0]
                  }
                }}
                """,
        },
        {
            Vectorizer.class,
            Text2VecWeaviateVectorizer.of(t2v -> t2v
                .inferenceUrl("http://example.com")
                .dimensions(4)
                .model("very-good-model")
                .vectorizeCollectionName(true)),
            """
                {"text2vec-weaviate": {
                  "baseUrl": "http://example.com",
                  "vectorizeCollectionName": true,
                  "dimensions": 4,
                  "model": "very-good-model"
                }}
                """,
        },

        // VectorIndex.CustomTypeFactory
        {
            VectorIndex.class,
            Flat.of(new NoneVectorizer(), flat -> flat
                .vectorCacheMaxObjects(100)),
            """
                {
                  "vectorIndexType": "flat",
                  "vectorizer": {"none": {}},
                  "vectorIndexConfig": {"vectorCacheMaxObjects": 100}
                }
                  """,
        },
        {
            VectorIndex.class,
            Hnsw.of(new NoneVectorizer(), hnsw -> hnsw
                .distance(Distance.DOT)
                .ef(1)
                .efConstruction(2)
                .maxConnections(3)
                .vectorCacheMaxObjects(4)
                .cleanupIntervalSeconds(5)
                .dynamicEfMin(6)
                .dynamicEfMax(7)
                .dynamicEfFactor(8)
                .flatSearchCutoff(9)
                .skipVectorization(true)
                .filterStrategy(Hnsw.FilterStrategy.ACORN)),
            """
                {
                  "vectorIndexType": "hnsw",
                  "vectorizer": {"none": {}},
                  "vectorIndexConfig": {
                    "distance": "dot",
                    "ef": 1,
                    "efConstruction": 2,
                    "maxConnections": 3,
                    "vectorCacheMaxObjects": 4,
                    "cleanupIntervalSeconds": 5,
                    "dynamicEfMin": 6,
                    "dynamicEfMax": 7,
                    "dynamicEfFactor": 8,
                    "flatSearchCutoff": 9,
                    "skip": true,
                    "filterStrategy":"acorn"
                  }
                }
                  """,
        },
    };
  }

  @Test
  @DataMethod(source = JSONTest.class, method = "testCases")
  public void test_serialize(Class<?> _cls, Object in, String want) {
    var got = JSON.serialize(in);
    assertEqualJson(want, got);
  }

  @Test
  @DataMethod(source = JSONTest.class, method = "testCases")
  public void test_deserialize(Class<?> targetClass, Object want, String in) {
    var got = JSON.deserialize(in, targetClass);
    Assertions.assertThat(got).isEqualTo(want);
  }

  private static void assertEqualJson(String want, String got) {
    var wantJson = JsonParser.parseString(want);
    var gotJson = JsonParser.parseString(got);
    Assertions.assertThat(gotJson).isEqualTo(wantJson);
  }
}
