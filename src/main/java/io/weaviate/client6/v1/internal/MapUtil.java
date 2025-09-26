package io.weaviate.client6.v1.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class MapUtil {
  /** Prevent public initialization. */
  private MapUtil() {
  }

  /**
   * Collect stream entries into a map. Use this method whenever
   * potential null keys or null values prohibit {@link Collectors#toMap}.
   *
   * <p>
   * Example:
   *
   * <pre>{@code
   * Map<Integer, Integer> = MapUtil.collect(
   *  Stream.of(1, 2, 3),
   *  Function.identity(), // use value as key
   *  el -> el.equals(3) ? null : el;
   * );
   *
   * // Result: {1: 1, 2: 2, 3: null};
   * }</pre>
   *
   * @param stream Stream of elements {@link T}.
   * @param keyFn  Transforms element {@link T} to key {@link K}.
   * @param keyFn  Transforms element {@link T} to value {@link V}.
   * @return Map
   */
  public static <K, V, T> Map<K, V> collect(Stream<T> stream, Function<T, K> keyFn, Function<T, V> valueFn) {
    return stream.collect(
        HashMap::new,
        (m, el) -> m.put(keyFn.apply(el), valueFn.apply(el)),
        HashMap::putAll);
  }
}
