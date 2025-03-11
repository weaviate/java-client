package io.weaviate.client6.v1.collections;

import org.junit.Test;

import com.google.gson.Gson;
import com.jparams.junit4.description.Name;

public class VectorsTesa {
  private static final Gson gson = new Gson();

  public static Object[][] testCases() {
    return new Object[][] {
        {
            "hnsw index with 'none' vectorizer",
            """
                  {
                    "default-index": {
                      "vectorizer": {
                        "none": {}
                      },
                      "vectorIndexType": "hnsw",
                      "vectorIndexConfig": {},
                    }
                  }
                """,
        },
    };
  }

  @Test
  @Name("{0}")
  public void testVectorIndex_toJson() {
  }
}
