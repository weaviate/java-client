package io.weaviate.client6.v1.collections;

import java.util.function.Consumer;

// This class is WIP, I haven't decided how to structure it yet.
public abstract class Vectorizer {
  public static NoneVectorizer none() {
    return new NoneVectorizer();
  }

  public static ContextionaryVectorizer text2vecContextionary() {
    return ContextionaryVectorizer.of();
  }

  public static ContextionaryVectorizer text2vecContextionary(Consumer<ContextionaryVectorizer.Builder> fn) {
    return ContextionaryVectorizer.of(fn);
  }

  // TODO: add test cases
  public static Text2VecWeaviateVectorizer text2vecWeaviate() {
    return Text2VecWeaviateVectorizer.of();
  }

  public static Text2VecWeaviateVectorizer text2vecWeaviate(Consumer<Text2VecWeaviateVectorizer.Builder> fn) {
    return Text2VecWeaviateVectorizer.of(fn);
  }
}
