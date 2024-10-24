package io.weaviate.client.v1.graphql.query.util;

import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

public class Serializer {

  private Serializer() {
  }

  /**
   * Creates graphql safe string
   * Nested quotes are escaped
   *
   * @param input string
   * @return escaped string
   */
  public static String escape(String input) {
    if (input == null) {
      return "";
    }
    return StringEscapeUtils.escapeJava(input);
  }

  /**
   * Creates graphql safe string
   * Surrounds input string with double quotes, nested quotes are escaped
   *
   * @param input string
   * @return quoted string
   */
  public static String quote(String input) {
    if (input == null) {
      return "";
    }
    if (input.equals("")) {
      return "\"\"";
    }
    return StringUtils.wrap(escape(input), "\"");
  }

  /**
   * Creates json safe array string
   * Surrounds each input string with double quotes, nested quotes are escaped
   *
   * @param input array of strings
   * @return json safe array string
   */
  public static String arrayWithQuotes(String[] input) {
    return array(input, Serializer::quote);
  }

  /**
   * Creates array string
   * It is up to user to make elements json safe
   *
   * @param input array of arbitrary elements
   * @return array string
   */
  public static <T> String array(T[] input) {
    return array(input, i -> i);
  }

  /**
   * Creates array string
   * It is up to user to make elements json safe
   *
   * @param input  array of arbitrary elements
   * @param mapper maps single element before building array
   * @return array string
   */
  public static <T, R> String array(T[] input, Function<T, R> mapper) {
    String inner = "";
    if (input != null) {
      inner = Arrays.stream(input)
        .map(mapper)
        .map(obj -> {
          if (obj.getClass().isArray()) {
            return array((Object[]) obj);
          }
          return obj.toString();
        })
        .collect(Collectors.joining(","));
    }

    return "[" + inner + "]";
  }
}
