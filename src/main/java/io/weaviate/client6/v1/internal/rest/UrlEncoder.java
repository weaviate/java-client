package io.weaviate.client6.v1.internal.rest;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

public final class UrlEncoder {

  public static String encodeValue(Object value) {
    try {
      return URLEncoder.encode(value.toString(), StandardCharsets.UTF_8.toString());
    } catch (UnsupportedEncodingException e) {
      throw new AssertionError(e); // should never happen with a standard encoding
    }
  }

  /**
   * Encodes each key-value pair into a URL-compatible query string, omitting any
   * {@code null} or blank (in case of String parameter) values.
   * Each value is represented by the return of its [@conde toString()} method.
   *
   * @return URL query string or empty string if the map was empty
   *         or contained no valid parameters.
   */
  public static String encodeQuery(Map<String, Object> queryParams) {
    if (queryParams == null || queryParams.isEmpty()) {
      return "";
    }
    var query = queryParams.entrySet().stream()
        .filter(qp -> {
          if (qp == null) {
            return false;
          }
          if (qp.getValue() instanceof String str) {
            return !str.isBlank();
          }
          return true;
        })
        .filter(qp -> qp.getKey() != null && qp.getValue() != null)
        .map(qp -> qp.getKey() + "=" + encodeValue(qp.getValue()))
        .collect(Collectors.joining("&", "?", ""));

    return query.equals("?") ? "" : query;
  }
}
