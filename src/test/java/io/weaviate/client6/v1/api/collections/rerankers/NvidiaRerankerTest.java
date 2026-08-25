package io.weaviate.client6.v1.api.collections.rerankers;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import io.weaviate.client6.v1.api.collections.Reranker;
import io.weaviate.client6.v1.internal.json.JSON;

public class NvidiaRerankerTest {

  /**
   * Collections created by earlier versions of this client have the base URL
   * stored under the "baseUrl" key the module never read. Those configs still
   * have to deserialize, otherwise upgrading turns a wrong value into a missing
   * one.
   */
  @Test
  public void test_readsLegacyBaseUrlKey() {
    var legacy = """
        {"reranker-nvidia": {"baseUrl": "https://legacy.example.com"}}
        """;

    var reranker = JSON.deserialize(legacy, Reranker.class);

    Assertions.assertThat(reranker)
        .asInstanceOf(org.assertj.core.api.InstanceOfAssertFactories.type(NvidiaReranker.class))
        .returns("https://legacy.example.com", NvidiaReranker::baseUrl);
  }

  /** ...but writing always uses the key the module actually reads. */
  @Test
  public void test_writesCanonicalBaseUrlKey() {
    var json = JSON.serialize(Reranker.nvidia(r -> r.baseUrl("https://example.com")));

    Assertions.assertThat(json).contains("\"baseURL\"").doesNotContain("\"baseUrl\"");
  }
}
