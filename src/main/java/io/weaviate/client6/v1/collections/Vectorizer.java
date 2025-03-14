package io.weaviate.client6.v1.collections;

// This class is WIP, I haven't decided how to structure it yet.
public abstract class Vectorizer {
  public static NoneVectorizer none() {
    return new NoneVectorizer();
  }
}
