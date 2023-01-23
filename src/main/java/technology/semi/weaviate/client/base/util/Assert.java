package technology.semi.weaviate.client.base.util;

import java.util.Objects;

public class Assert {

  private Assert(){}

  public static void requireGreater(int value, int minValue, String paramName) {
    if (value <= minValue) {
      throw new IllegalArgumentException(String.format("%s should be greater than %d", paramName, minValue));
    }
  }

  public static void requireGreaterEqual(int value, int minValue, String paramName) {
    if (value < minValue) {
      throw new IllegalArgumentException(String.format("%s should be greater than or equal %d", paramName, minValue));
    }
  }

  public static void requiredNotNull(Object value, String paramName) {
    if (Objects.isNull(value)) {
      throw new IllegalArgumentException(String.format("%s should not be null", paramName));
    }
  }
}
