package io.weaviate.client6.v1.collections;

import java.util.function.Consumer;

// This class is WIP, I haven't decided how to structure it yet.
public abstract class Vectorizer {
  public static NoneVectorizer none() {
    return new NoneVectorizer();
  }

  public static ContextionaryVectorizer contextionary() {
    return ContextionaryVectorizer.of();
  }

  public static ContextionaryVectorizer contextionary(Consumer<ContextionaryVectorizer.Builder> fn) {
    return ContextionaryVectorizer.of(fn);
  }
}
