package io.weaviate.integration.async;

import io.weaviate.client.Config;
import io.weaviate.client.WeaviateAsyncClient;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.misc.model.Meta;
import io.weaviate.integration.client.WeaviateDockerCompose;
import static io.weaviate.integration.client.WeaviateVersion.EXPECTED_WEAVIATE_VERSION;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.apache.hc.core5.concurrent.FutureCallback;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

public class ClientMiscTest {

  private WeaviateClient client;

  @ClassRule
  public static WeaviateDockerCompose compose = new WeaviateDockerCompose();

  @Before
  public void before() {
    Config config = new Config("http", compose.getHttpHostAddress());
    client = new WeaviateClient(config);
  }

  @Test
  public void testMiscMetaEndpoint() throws ExecutionException, InterruptedException {
    try (WeaviateAsyncClient asyncClient = client.async()) {
      // perform operations
      Future<Result<Meta>> future =  asyncClient.misc().metaGetter().run();
      Result<Meta> meta = future.get();
      // assert results
      assertMeta(meta);
    }
  }

  @Test
  public void testMiscMetaEndpointWithCallback() throws ExecutionException, InterruptedException {
    try (WeaviateAsyncClient asyncClient = client.async()) {
      // perform operations
      FutureCallback<Result<Meta>> callback = new FutureCallback<Result<Meta>>() {
        @Override
        public void completed(Result<Meta> result) {
          assertMeta(result);
        }

        @Override
        public void failed(Exception ex) {
          assertNull(ex);
        }

        @Override
        public void cancelled() {}
      };
      Future<Result<Meta>> future =  asyncClient.misc().metaGetter().run(callback);
      future.get();
    }
  }

  private static void assertMeta(Result<Meta> meta) {
    assertNotNull(meta);
    assertNull(meta.getError());
    assertEquals("http://[::]:8080", meta.getResult().getHostname());
    assertEquals(EXPECTED_WEAVIATE_VERSION, meta.getResult().getVersion());
    assertEquals("{backup-filesystem={backupsPath=/tmp/backups}, " +
      "generative-openai={documentationHref=https://platform.openai.com/docs/api-reference/completions, name=Generative Search - OpenAI}, " +
      "text2vec-contextionary={version=en0.16.0-v1.2.1, wordCount=818072.0}}", meta.getResult().getModules().toString());
  }
}
