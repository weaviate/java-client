package io.weaviate.integration.client.async.misc;

import io.weaviate.client.Config;
import io.weaviate.client.v1.async.WeaviateAsyncClient;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.misc.model.Meta;
import io.weaviate.integration.client.WeaviateDockerCompose;
import io.weaviate.integration.tests.misc.MiscTestSuite;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.apache.hc.core5.concurrent.FutureCallback;
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
  public void testMiscLivenessEndpoint() throws ExecutionException, InterruptedException {
    try (WeaviateAsyncClient asyncClient = client.async()) {
      // perform operations
      Future<Result<Boolean>> future = asyncClient.misc().liveChecker().run();
      Result<Boolean> livenessCheck = future.get();
      // assert results
      MiscTestSuite.assertLivenessOrReadiness(livenessCheck);
    }
  }

  @Test
  public void testMiscLivenessEndpointWithCallback() throws ExecutionException, InterruptedException {
    try (WeaviateAsyncClient asyncClient = client.async()) {
      // perform operations
      FutureCallback<Result<Boolean>> callback = new FutureCallback<Result<Boolean>>() {

        @Override
        public void completed(Result<Boolean> booleanResult) {
          MiscTestSuite.assertLivenessOrReadiness(booleanResult);
        }

        @Override
        public void failed(Exception e) {
          assertNull(e);
        }

        @Override
        public void cancelled() {
        }
      };
      Future<Result<Boolean>> future = asyncClient.misc().liveChecker().run(callback);
      future.get();
    }
  }

  @Test
  public void testMiscReadinessEndpoint() throws ExecutionException, InterruptedException {
    try (WeaviateAsyncClient asyncClient = client.async()) {
      // perform operations
      Future<Result<Boolean>> future = asyncClient.misc().readyChecker().run();
      Result<Boolean> readinessCheck = future.get();
      // assert results
      MiscTestSuite.assertLivenessOrReadiness(readinessCheck);
    }
  }

  @Test
  public void testMiscReadinessEndpointWithCallback() throws ExecutionException, InterruptedException {
    try (WeaviateAsyncClient asyncClient = client.async()) {
      // perform operations
      FutureCallback<Result<Boolean>> callback = new FutureCallback<Result<Boolean>>() {

        @Override
        public void completed(Result<Boolean> booleanResult) {
          MiscTestSuite.assertLivenessOrReadiness(booleanResult);
        }

        @Override
        public void failed(Exception e) {
          assertNull(e);
        }

        @Override
        public void cancelled() {
        }
      };
      Future<Result<Boolean>> future = asyncClient.misc().readyChecker().run(callback);
      future.get();
    }
  }

  @Test
  public void testMiscMetaEndpointWithCallback() throws ExecutionException, InterruptedException {
    try (WeaviateAsyncClient asyncClient = client.async()) {
      // perform operations
      FutureCallback<Result<Meta>> callback = new FutureCallback<Result<Meta>>() {
        @Override
        public void completed(Result<Meta> result) {
          MiscTestSuite.assertMeta(result);
        }

        @Override
        public void failed(Exception ex) {
          assertNull(ex);
        }

        @Override
        public void cancelled() {
        }
      };
      Future<Result<Meta>> future = asyncClient.misc().metaGetter().run(callback);
      future.get();
    }
  }
}
