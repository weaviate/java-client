package io.weaviate.containers;

import java.util.Arrays;
import java.util.List;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.lifecycle.Startable;

import io.weaviate.client6.v1.api.WeaviateClient;

public class Container {
  public static final Weaviate WEAVIATE = Weaviate.createDefault();
  public static final Contextionary CONTEXTIONARY = Contextionary.createDefault();
  public static final Img2VecNeural IMG2VEC_NEURAL = Img2VecNeural.createDefault();

  static {
    startAll();
  }

  /** Start all shared Testcontainers. */
  // TODO: start lazily!
  static void startAll() {
    // WEAVIATE.start();
  }

  /**
   * Stop all shared Testcontainers created in {@link #startAll}.
   * <p>
   * Testcontainer's Ryuk will reap any dangling containers after the tests
   * finish. However, since {@link Weaviate} instances also hold a
   * {@link WeaviateClient}, we want to stop them proactively to
   * close client connections.
   */
  static void stopAll() {
    WEAVIATE.stop();
  }

  public static ContainerGroup compose(Weaviate weaviate, GenericContainer<?>... containers) {
    return new ContainerGroup(weaviate, containers);
  }

  public static TestRule asTestRule(Startable container) {
    return new PerTestSuite(container);
  };

  public static class ContainerGroup implements Startable {
    private final Weaviate weaviate;
    private final List<GenericContainer<?>> containers;

    private ContainerGroup(Weaviate weaviate, GenericContainer<?>... containers) {
      this.weaviate = weaviate;
      this.containers = Arrays.asList(containers);

      weaviate.dependsOn(containers);
      setSharedNetwork();
    }

    public WeaviateClient getClient() {
      return weaviate.getClient();
    }

    @Override
    public void start() {
      weaviate.start(); // testcontainers will resolve dependencies
    }

    @Override
    public void stop() {
      weaviate.stop();
      containers.forEach(GenericContainer::stop);
    }

    private void setSharedNetwork() {
      weaviate.setNetwork(Network.SHARED);
      containers.forEach(c -> c.setNetwork(Network.SHARED));
    }

    public TestRule asTestRule() {
      return new PerTestSuite(this);
    };
  }

  public static class PerTestSuite implements TestRule {
    private final Startable container;

    public PerTestSuite(Startable container) {
      this.container = container;
    }

    @Override
    public Statement apply(Statement base, Description description) {
      return new Statement() {
        @Override
        public void evaluate() throws Throwable {
          try {
            container.start();
            base.evaluate();
          } finally {
            container.stop();
          }
        }
      };
    }
  }
}
