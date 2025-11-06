package io.weaviate;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang3.RandomStringUtils;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.Assumptions;
import org.junit.Rule;
import org.junit.rules.TestName;

import io.weaviate.client6.v1.internal.VersionSupport.SemanticVersion;
import io.weaviate.containers.Weaviate;

/**
 * ConcurrentTest is the base class for integration tests, which provides
 * utility methods to uniqualize collections and objects created in the
 * database.
 *
 * Because we want to re-use the same database container across most of the
 * test suites and (eventually) run them in parallel,
 * test classes should extend this class and use its methods
 * to avoid name clashes in the shared Weaviate instance.
 */
public abstract class ConcurrentTest {
  @Rule
  public TestName currentTest = new TestName();

  protected static final Random rand = new Random();

  /**
   * Add unique namespace prefix to the string.
   *
   * @param value Collection name, object ID, etc., which has to be unique across
   *              all test suites.
   * @return Value prefixed with the name of the current test suite + test method.
   */
  protected String ns(String value) {
    String cls = getClass().getSimpleName();
    String method = currentTest.getMethodName();
    return cls + "_" + method + "_" + value;
  }

  /** Appends random characters to create unique value. */
  protected static String unique(String value) {
    var randString = RandomStringUtils.insecure().next(8, true, false);
    return value + "_" + randString;
  }

  /** Generate random UUID. */
  protected static String randomUUID() {
    return UUID.randomUUID().toString();
  }

  /**
   * Generate a random vector.
   *
   * @param length Vector length.
   * @param origin Value range lower bound.
   * @param bound  Value range upper bound.
   * @return
   */
  protected static float[] randomVector(int length, float origin, float bound) {
    var vector = new float[length];
    for (var i = 0; i < length; i++) {
      vector[i] = rand.nextFloat(origin, bound);
    }
    return vector;
  }

  /**
   * Check that a condition is eventually met.
   *
   * @param cond           Arbitrary code that evaluates the test condition..
   * @param intervalMillis Polling interval.
   * @param timeoutSeconds Maximum waiting time.
   * @param message        Optional failure message.
   *
   * @throws AssertionError   if the condition does not evaluate to true
   *                          within {@code timeoutSeconds} or a thread
   *                          was interrupted in the meantime.
   * @throws RuntimeException if an exception occurred when envalating condition.
   */
  public static void eventually(Callable<Boolean> cond, int intervalMillis, int timeoutSeconds, String... message) {
    var check = CompletableFuture.runAsync(() -> {
      try {
        while (!Thread.currentThread().isInterrupted() && !cond.call()) {
          try {
            Thread.sleep(intervalMillis);
          } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
          }
        }
      } catch (Exception e) {
        // Propagate to callee
        throw new RuntimeException(e);
      }
    });

    try {
      check.get(timeoutSeconds, TimeUnit.SECONDS);
    } catch (TimeoutException ex) {
      check.cancel(true);
      Assertions.fail(message.length >= 0 ? message[0] : null, ex);
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
      Assertions.fail(ex);
    } catch (ExecutionException ex) {
      throw new RuntimeException(ex);
    }
  }

  /**
   * Skip the test if the version that the {@link Weaviate}
   * container is running is older than the required one.
   */
  public static void requireAtLeast(int major, int minor) {
    var required = new SemanticVersion(major, minor);
    var actual = SemanticVersion.of(Weaviate.VERSION);
    Assumptions.assumeThat(actual)
        .as("requires at least %s, but running %s", required, actual)
        .isGreaterThanOrEqualTo(required);
  }

  public static void requireAtLeast(int major, int minor, Runnable r) {
    var required = new SemanticVersion(major, minor);
    var actual = SemanticVersion.of(Weaviate.VERSION);
    if (actual.compareTo(required) >= 0) {
      r.run();
    }
  }
}
