package io.weaviate.client6.v1.api.backup;

import java.time.Duration;
import java.util.function.Function;

import io.weaviate.client6.v1.internal.ObjectBuilder;

public record WaitOptions(long interval, long timeout) {
  private static final long DEFAULT_INTERVAL_MILLIS = 1_000;
  private static final long DEFAULT_TIMEOUT_MILLIS = 3600_000;

  public static WaitOptions of(Function<Builder, ObjectBuilder<WaitOptions>> fn) {
    return fn.apply(new Builder()).build();
  }

  public WaitOptions(Builder builder) {
    this(builder.interval, builder.timeout);
  }

  public static class Builder implements ObjectBuilder<WaitOptions> {
    private long interval = DEFAULT_INTERVAL_MILLIS;
    private long timeout = DEFAULT_TIMEOUT_MILLIS;

    /** Set polling interval. Defaults to 1s. */
    public Builder interval(Duration duration) {
      return interval(duration.toMillis());
    }

    /**
     * Set polling interval. Defaults to 1s.
     *
     * @param intervalMillis Polling interval in milliseconds. Minimum 1ms.
     */
    public Builder interval(long intervalMillis) {
      this.interval = Math.max(intervalMillis, 1);
      return this;
    }

    /**
     * Set wait timeout. Defaults to 1s.
     *
     * @param duration Wait timeout duration.
     */
    public Builder timeout(Duration duration) {
      return timeout(duration.toMillis());
    }

    /**
     * Set wait timeout. Set this to a negative value
     * for the wait to expire immediately.
     *
     * @param timeoutMillis Wait timeout in milliseconds.
     */
    public Builder timeout(long timeoutMillis) {
      this.timeout = timeoutMillis;
      return this;
    }

    @Override
    public WaitOptions build() {
      return new WaitOptions(this);
    }
  }
}
