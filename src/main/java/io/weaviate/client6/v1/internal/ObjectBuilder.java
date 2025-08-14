package io.weaviate.client6.v1.internal;

import java.util.function.Function;

public interface ObjectBuilder<T> {
  T build();

  static <B extends ObjectBuilder<T>, T> Function<B, ObjectBuilder<T>> identity() {
    return builder -> builder;
  }

  /**
   * Chain two builder-functions such that {@code partialFn} is applied before
   * {@code fn}.
   *
   * <p>
   * Usage:
   *
   * <pre>{@code
   *  static final Function<TBuilder, ObjectBuilder<T>> defaultConfig = b -> {...};
   *  void doWithConfig(Function<TBuilder, ObjectBuilder<T>> fn) {
   *    var withDefault = ObjectBuilder.partial(fn, defaultConfig);
   *    var config = fn.apply(new Config()).build();
   *  }
   * }</pre>
   *
   * @param fn        Function that will be applied last.
   * @param partialFn Function that will be applied first.
   * @return ObjectBuilder with "pre-applied" function.
   */
  static <B extends ObjectBuilder<T>, T> Function<B, ObjectBuilder<T>> partial(Function<B, ObjectBuilder<T>> fn,
      Function<B, B> partialFn) {
    return partialFn.andThen(fn);
  }
}
