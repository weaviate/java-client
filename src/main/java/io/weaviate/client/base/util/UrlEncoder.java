package io.weaviate.client.base.util;

import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class UrlEncoder {

  private UrlEncoder() {}

  public static String encodeQueryParam(String key, String value) {
    return String.format("%s=%s", encode(StringUtils.trim(key)), encode(StringUtils.trim(value)));
  }

  public static String encodePathParam(String value) {
    return encode(StringUtils.trim(value));
  }

  public static String encode(String value) {
    try {
      return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
    } catch (UnsupportedEncodingException e) {
      return value;
    }
  }
}
