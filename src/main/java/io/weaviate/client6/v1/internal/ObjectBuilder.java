package io.weaviate.client6.v1.internal;

import java.util.function.Function;

public interface ObjectBuilder<T> {
  T build();

  static <B extends ObjectBuilder<T>, T> Function<B, ObjectBuilder<T>> identity() {
    return builder -> builder;
  }

  static <B extends ObjectBuilder<T>, T> Function<B, ObjectBuilder<T>> partial(Function<B, ObjectBuilder<T>> fn,
      Function<B, B> partialFn) {
    return partialFn.andThen(fn);
  }
}
