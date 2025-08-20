package io.weaviate.client6.v1.internal.rest;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

public final class UrlEncoder {

  private static String encodeValue(Object value) {
    try {
      return URLEncoder.encode(value.toString(), StandardCharsets.UTF_8.toString());
    } catch (UnsupportedEncodingException e) {
      throw new AssertionError(e); // should never happen with a standard encoding
    }
  }

  public static String encodeQuery(Map<String, Object> queryParams) {
    if (queryParams == null || queryParams.isEmpty()) {
      return "";
    }
    return queryParams.entrySet().stream()
        .map(qp -> qp.getKey() + "=" + encodeValue(qp.getValue()))
        .collect(Collectors.joining("&", "?", ""));
  }
}
