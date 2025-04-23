package io.weaviate.client6.v1.collections.data;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

interface QueryParameters {
  /* Implementations must return an empty string if there're no parameters. */
  String encode();

  static String encodeGet(Consumer<GetParameters> options) {
    return with(new GetParameters(options));
  }

  private static <P extends QueryParameters> String with(P parameters) {
    var encoded = parameters.encode();
    return encoded.isEmpty() ? "" : "?" + encoded;
  }

  static void add(StringBuilder sb, String key, String value) {
    addRaw(sb, encode(key), encode(value));
  }

  static void addRaw(StringBuilder sb, String key, String value) {
    if (!sb.isEmpty()) {
      sb.append("&");
    }
    sb.append(key).append("=").append(value);
  }

  static String encode(String value) {
    try {
      return URLEncoder.encode(value, StandardCharsets.UTF_8.name());
    } catch (UnsupportedEncodingException e) {
      // Will never happen, as we are using standard encoding.
      return value;
    }
  }
}
