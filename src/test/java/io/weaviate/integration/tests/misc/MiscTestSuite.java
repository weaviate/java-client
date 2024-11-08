package io.weaviate.integration.tests.misc;

import io.weaviate.client.base.Result;
import io.weaviate.client.v1.misc.model.Meta;
import static io.weaviate.integration.client.WeaviateVersion.EXPECTED_WEAVIATE_VERSION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class MiscTestSuite {
  public static void assertLivenessOrReadiness(Result<Boolean> result) {
    assertNotNull(result);
    assertTrue(result.getResult());
  }

  public static void assertMeta(Result<Meta> meta) {
    assertNotNull(meta);
    assertNull(meta.getError());
    assertEquals("http://[::]:8080", meta.getResult().getHostname());
    assertEquals(EXPECTED_WEAVIATE_VERSION, meta.getResult().getVersion());
    assertEquals("{backup-filesystem={backupsPath=/tmp/backups}, " +
      "generative-openai={documentationHref=https://platform.openai.com/docs/api-reference/completions, name=Generative Search - OpenAI}, " +
      "text2vec-contextionary={version=en0.16.0-v1.2.1, wordCount=818072.0}}", meta.getResult().getModules().toString());
  }
}
