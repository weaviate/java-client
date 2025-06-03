package io.weaviate.client.v1.data.model;

import java.util.Collections;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.jparams.junit4.JParamsTestRunner;
import com.jparams.junit4.data.DataMethod;

@RunWith(JParamsTestRunner.class)
/**
 * Test that WeaviateObject vectors are de-/serialized correctly. Specifically,
 * single- and multi-vectors should be correctly combined under the "vectors"
 * key in case any named vectors are present.
 */
public class WeaviateObjectAdapterTest {
  private static final Gson gson = new GsonBuilder()
      .registerTypeAdapter(WeaviateObject.class, WeaviateObject.Adapter.INSTANCE)
      .create();

  public static Object[][] testCasesJson() {
    return new Object[][] {
        {
            WeaviateObject.builder().vector(new Float[] { 1f, 2f, 3f }).build(),
            "{\"vector\":[1.0,2.0,3.0]}"
        },
        {
            WeaviateObject.builder().vectors(Collections.singletonMap("single", new Float[] { 1f, 2f, 3f })).build(),
            "{\"vectors\":{\"single\":[1.0,2.0,3.0]}}"
        },
        {
            WeaviateObject.builder()
                .multiVectors(Collections.singletonMap("multi", new Float[][] {
                    { 1f, 2f, 3f },
                    { 4f, 5f, 6f },
                }))
                .build(),
            "{\"vectors\":{\"multi\":[[1.0,2.0,3.0],[4.0, 5.0, 6.0]]}}"
        },
        {
            WeaviateObject.builder()
                .vectors(Collections.singletonMap("single", new Float[] { 1f, 2f, 3f }))
                .multiVectors(Collections.singletonMap("multi", new Float[][] {
                    { 1f, 2f, 3f },
                    { 4f, 5f, 6f },
                }))
                .build(),
            "{\"vectors\":{\"single\":[1.0,2.0,3.0],\"multi\":[[1.0,2.0,3.0],[4.0, 5.0, 6.0]]}}"
        },
    };
  }

  @Test
  @DataMethod(source = WeaviateObjectAdapterTest.class, method = "testCasesJson")
  public void test_toJson(WeaviateObject in, String want) {
    String got = gson.toJson(in);
    assertSameJson(got, want);
  }

  @Test
  @DataMethod(source = WeaviateObjectAdapterTest.class, method = "testCasesJson")
  public void test_fromJson(WeaviateObject want, String in) {
    WeaviateObject got = gson.fromJson(in, WeaviateObject.class);
    Assertions.assertThat(got).usingRecursiveComparison().isEqualTo(want);
  }

  private void assertSameJson(String got, String want) {
    JsonElement gotEl = JsonParser.parseString(got);
    JsonElement wantEl = JsonParser.parseString(want);
    Assertions.assertThat(gotEl).isEqualTo(wantEl);
  }
}
