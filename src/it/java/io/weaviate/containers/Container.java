package io.weaviate.containers;

import java.util.Arrays;
import java.util.List;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.lifecycle.Startable;

import io.weaviate.client6.WeaviateClient;
import lombok.RequiredArgsConstructor;

public class Container {
  public static final Weaviate WEAVIATE = Weaviate.createDefault();
  public static final Contextionary CONTEXTIONARY = Contextionary.createDefault();

  static {
    WEAVIATE.start();
  }

  public static Group compose(Weaviate weaviate, GenericContainer<?>... containers) {
    return new Group(weaviate, containers);
  }

  public static TestRule asTestRule(Startable container) {
    System.out.print("HERE");
    return new PerTestSuite(container);
  };

  public static class Group implements Startable {
    private final Weaviate weaviate;
    private final List<GenericContainer<?>> containers;

    private Group(Weaviate weaviate, GenericContainer<?>... containers) {
      this.weaviate = weaviate;
      this.containers = Arrays.asList(containers);
      setSharedNetwork();
      System.out.println("Group initialized");
    }

    public WeaviateClient getClient() {
      System.out.println("get Weaviate client");
      return weaviate.getClient();
    }

    @Override
    public void start() {
      System.out.println("Starting containers...");
      containers.forEach(GenericContainer::start);
      System.out.println("Starting Weaviate...");
      weaviate.start();
      System.out.println("Started");
    }

    @Override
    public void stop() {
      System.out.println("Stopping...");
      weaviate.stop();
      containers.forEach(GenericContainer::stop);
    }

    private void setSharedNetwork() {
      System.out.println("Set shared network...");
      weaviate.setNetwork(Network.SHARED);
      containers.forEach(c -> c.setNetwork(Network.SHARED));
    }

    public TestRule asTestRule() {
      System.out.println("As TestRule!");
      return new PerTestSuite(this);
    };
  }

  @RequiredArgsConstructor
  public static class PerTestSuite implements TestRule {
    private final Startable container;

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
