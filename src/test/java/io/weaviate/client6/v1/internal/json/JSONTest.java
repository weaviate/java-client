package io.weaviate.client6.v1.internal.json;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.jparams.junit4.JParamsTestRunner;
import com.jparams.junit4.data.DataMethod;

import io.weaviate.client6.v1.api.collections.CollectionConfig;
import io.weaviate.client6.v1.api.collections.Generative;
import io.weaviate.client6.v1.api.collections.ObjectMetadata;
import io.weaviate.client6.v1.api.collections.Property;
import io.weaviate.client6.v1.api.collections.Reranker;
import io.weaviate.client6.v1.api.collections.VectorIndex;
import io.weaviate.client6.v1.api.collections.Vectorizer;
import io.weaviate.client6.v1.api.collections.Vectors;
import io.weaviate.client6.v1.api.collections.WeaviateObject;
import io.weaviate.client6.v1.api.collections.data.BatchReference;
import io.weaviate.client6.v1.api.collections.data.Reference;
import io.weaviate.client6.v1.api.collections.data.ReferenceAddManyResponse;
import io.weaviate.client6.v1.api.collections.rerankers.CohereReranker;
import io.weaviate.client6.v1.api.collections.vectorindex.Distance;
import io.weaviate.client6.v1.api.collections.vectorindex.Flat;
import io.weaviate.client6.v1.api.collections.vectorindex.Hnsw;
import io.weaviate.client6.v1.api.collections.vectorizers.Img2VecNeuralVectorizer;
import io.weaviate.client6.v1.api.collections.vectorizers.Multi2VecClipVectorizer;
import io.weaviate.client6.v1.api.collections.vectorizers.NoneVectorizer;
import io.weaviate.client6.v1.api.collections.vectorizers.Text2VecContextionaryVectorizer;
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
                  "vectorizeClassName": true,
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
            Text2VecContextionaryVectorizer.of(t2v -> t2v
                .vectorizeCollectionName(true)),
            """
                {"text2vec-contextionary": {
                  "vectorizeClassName": true
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
                  "vectorizeClassName": true,
                  "dimensions": 4,
                  "model": "very-good-model"
                }}
                """,
        },

        // VectorIndex.CustomTypeAdapterFactory
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

        // Vectors.CustomTypeAdapterFactory
        {
            Vectors.class,
            Vectors.of(new Float[] { 1f, 2f }),
            "{\"default\": [1.0, 2.0]}",
            (CustomAssert) JSONTest::compareVectors,
        },
        {
            Vectors.class,
            Vectors.of(new Float[][] { { 1f, 2f }, { 3f, 4f } }),
            "{\"default\": [[1.0, 2.0], [3.0, 4.0]]}",
            (CustomAssert) JSONTest::compareVectors,
        },
        {
            Vectors.class,
            Vectors.of("custom", new Float[] { 1f, 2f }),
            "{\"custom\": [1.0, 2.0]}",
            (CustomAssert) JSONTest::compareVectors,
        },
        {
            Vectors.class,
            Vectors.of("custom", new Float[][] { { 1f, 2f }, { 3f, 4f } }),
            "{\"custom\": [[1.0, 2.0], [3.0, 4.0]]}",
            (CustomAssert) JSONTest::compareVectors,
        },
        {
            Vectors.class,
            Vectors.of(named -> named
                .vector("1d", new Float[] { 1f, 2f })
                .vector("2d", new Float[][] { { 1f, 2f }, { 3f, 4f } })),
            "{\"1d\": [1.0, 2.0], \"2d\": [[1.0, 2.0], [3.0, 4.0]]}",
            (CustomAssert) JSONTest::compareVectors,
        },

        // WeaviateCollection.CustomTypeAdapterFactory
        {
            CollectionConfig.class,
            CollectionConfig.of("Things", things -> things
                .description("A collection of things")
                .properties(
                    Property.text("shape"),
                    Property.integer("size"))
                .references(
                    Property.reference("owner", "Person", "Company"))
                .vectors(named -> named
                    .vector("v-shape", Hnsw.of(Img2VecNeuralVectorizer.of(
                        i2v -> i2v.imageFields("img")))))),
            """
                {
                  "class": "Things",
                  "description": "A collection of things",
                  "properties": [
                    {"name": "shape", "dataType": ["text"]},
                    {"name": "size", "dataType": ["int"]},
                    {"name": "owner", "dataType": ["Person", "Company"]}
                  ],
                  "vectorConfig": {
                    "v-shape": {
                      "vectorIndexType": "hnsw",
                      "vectorIndexConfig": {},
                      "vectorizer": {"img2vec-neural": {
                        "imageFields": ["img"]
                      }}
                    }
                  }
                }
                """,
        },

        // Reference.TYPE_ADAPTER
        {
            Reference.class,
            Reference.uuids("id-1"),
            "{\"beacon\": \"weaviate://localhost/id-1\"}",
        },
        {
            Reference.class,
            Reference.collection("Doodlebops", "id-1"),
            "{\"beacon\": \"weaviate://localhost/Doodlebops/id-1\"}",
        },

        // WeaviateObject.CustomTypeAdapterFactory.INSTANCE
        {
            new TypeToken<WeaviateObject<Map<String, Object>, Reference, ObjectMetadata>>() {
            },
            new WeaviateObject<>(
                "Things",
                Map.of("title", "ThingOne"),
                Map.of("hasRef", List.of(Reference.uuids("ref-1"))),
                ObjectMetadata.of(meta -> meta.uuid("thing-1"))),
            """
                {
                  "class": "Things",
                  "properties": {
                    "title": "ThingOne",
                    "hasRef": [{"beacon": "weaviate://localhost/ref-1"}]
                  },
                  "id": "thing-1"
                }
                  """,
        },

        // Reranker.CustomTypeAdapterFactory
        {
            Reranker.class,
            Reranker.cohere(rerank -> rerank
                .model(CohereReranker.RERANK_ENGLISH_V2)),
            """
                {
                  "reranker-cohere": {
                    "model": "rerank-english-v2.0"
                  }
                }
                  """,
        },

        {
            Generative.class,
            Generative.cohere(generate -> generate
                .kProperty("k-property")
                .maxTokensProperty(10)
                .model("example-model")
                .returnLikelihoodsProperty("likelihood")
                .stopSequencesProperty("stop", "halt")
                .temperatureProperty("celcius")),
            """
                {
                  "generative-cohere": {
                    "kProperty": "k-property",
                    "maxTokensProperty": 10,
                    "model": "example-model",
                    "returnLikelihoodsProperty": "likelihood",
                    "stopSequencesProperty": ["stop", "halt"],
                    "temperatureProperty": "celcius"
                  }
                }
                  """,
        },
        {
            BatchReference.class,
            new BatchReference("FromCollection", "fromProperty", "from-uuid",
                Reference.collection("ToCollection", "to-uuid")),
            """
                {
                  "from": "weaviate://localhost/FromCollection/from-uuid/fromProperty",
                  "to": "weaviate://localhost/ToCollection/to-uuid"
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

  @Test
  public void test_ReferenceAddManyResponse_CustomDeserializer() {
    var json = """
        [
          {
            "result": { "status": "SUCCESS", "errors": {} }
          },
          {
            "result": { "status": "FAILED", "errors": {
              "error": [ "oops" ]
            }}
          }
        ]
          """;

    var got = JSON.deserialize(json, ReferenceAddManyResponse.class);

    Assertions.assertThat(got.errors())
        .as("response contains 1 error")
        .hasSize(1);
  }
}
