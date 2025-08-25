package io.weaviate.client6.v1.internal;

import java.time.OffsetDateTime;

public final class DateUtil {

  /** Convert ISO8601-formatted time string to {@Olink OffsetDateTime}. */
  public static OffsetDateTime fromISO8601(String iso8601) {
    return OffsetDateTime.parse(iso8601);
  }
}
