package io.weaviate.client6.v1.collections;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.jparams.junit4.JParamsTestRunner;
import com.jparams.junit4.data.DataMethod;

import io.weaviate.client6.internal.DtoTypeAdapterFactory;
import io.weaviate.client6.v1.collections.VectorIndex.IndexingStrategy;

@RunWith(JParamsTestRunner.class)
public class VectorsTest {
  // private static final Gson gson = new Gson();

  static {
    DtoTypeAdapterFactory.register(CollectionDefinition.class, CollectionDefinitionDTO.class,
        m -> new CollectionDefinitionDTO(m));
  }
  private static final Gson gson = new GsonBuilder()
      .registerTypeAdapterFactory(new DtoTypeAdapterFactory())
      // TODO: create TypeAdapters via TypeAdapterFactory
      .registerTypeAdapter(Vectors.class, new TypeAdapter<Vectors>() {
        Gson gson = new Gson();

        @Override
        public void write(JsonWriter out, Vectors value) throws IOException {
          gson.toJson(value.asMap(), Map.class, out);
        }

        @Override
        public Vectors read(JsonReader in) throws IOException {
          // TODO Auto-generated method stub
          throw new UnsupportedOperationException("Unimplemented method 'read'");
        }

      })
      .registerTypeHierarchyAdapter(Vectorizer.class, new TypeAdapter<Vectorizer>() {
        Gson gson = new Gson();

        @Override
        public void write(JsonWriter out, Vectorizer value) throws IOException {
          if (value != null) {
            gson.toJson(value, value.getClass(), out);
          } else {
            out.nullValue();
          }
        }

        @Override
        public Vectorizer read(JsonReader in) throws IOException {
          return Vectorizer.none();
        }
      })
      .create();

  public static Object[][] testCases() {
    return new Object[][] {
        {
            """
                {
                  "vectorConfig": {
                    "default": { "vectorizer": { "none":{}}}
                  }
                }
                  """,
            collectionWithVectors(Vectors.of(new VectorIndex<>(Vectorizer.none()))),
            new String[] { "vectorConfig" },
        },
        {
            """
                {
                  "vectorConfig": {
                    "vector-1": { "vectorizer": { "none":{}}},
                    "vector-2": {
                      "vectorizer": { "none":{}},
                      "vectorIndexType": "hnsw",
                      "vectorIndexConfig": {}
                    }
                  }
                }
                  """,
            collectionWithVectors(Vectors.with(named -> named
                .vector("vector-1", new VectorIndex<>(Vectorizer.none()))
                .vector("vector-2", new VectorIndex<>(IndexingStrategy.hnsw(), Vectorizer.none())))),
            new String[] { "vectorConfig" },
        },
        {
            """
                {
                  "vectorizer": { "none": {}},
                  "vectorIndexConfig": { "distance": "COSINE", "skip": true },
                  "vectorIndexType": "hnsw"
                }
                  """,
            collectionWithVectors(Vectors.unnamed(
                new VectorIndex<>(
                    IndexingStrategy.hnsw(opt -> opt
                        .distance(HNSW.Distance.COSINE)
                        .disableIndexation()),
                    Vectorizer.none()))),
            new String[] { "vectorIndexType", "vectorIndexConfig", "vectorizer" },
        },
    };
  }

  @Test
  @DataMethod(source = VectorsTest.class, method = "testCases")
  public void test_toJson(String want, CollectionDefinition collection, String... compareKeys) {
    var got = gson.toJson(collection);
    assertEqual(want, got, compareKeys);
  }

  private static CollectionDefinition collectionWithVectors(Vectors vectors) {
    return new CollectionDefinition("Things", List.of(), vectors);
  }

  private void assertEqual(String wantJson, String gotJson, String... compareKeys) {
    var want = JsonParser.parseString(wantJson).getAsJsonObject();
    var got = JsonParser.parseString(gotJson).getAsJsonObject();

    if (compareKeys == null || compareKeys.length == 0) {
      Assertions.assertThat(got).isEqualTo(want);
      return;
    }

    for (var key : compareKeys) {
      Assertions.assertThat(got.get(key))
          .isEqualTo(want.get(key))
          .as(key);
    }
  }
}
