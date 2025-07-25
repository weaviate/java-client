package io.weaviate;

import java.util.Random;
import java.util.UUID;
import java.util.stream.IntStream;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Rule;
import org.junit.rules.TestName;

import com.google.common.primitives.Floats;

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
    return Floats.toArray(IntStream.range(0, length)
        .<Float>mapToObj(f -> rand.nextFloat(origin, bound))
        .toList());
  }
}
