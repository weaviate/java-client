package io.weaviate.client6.v1.api.collections;

import java.util.Map;
import java.util.function.Function;

import io.weaviate.client6.v1.api.collections.vectorizers.Img2VecNeuralVectorizer;
import io.weaviate.client6.v1.api.collections.vectorizers.Multi2VecClipVectorizer;
import io.weaviate.client6.v1.api.collections.vectorizers.SelfProvidedVectorizer;
import io.weaviate.client6.v1.api.collections.vectorizers.Text2VecContextionaryVectorizer;
import io.weaviate.client6.v1.api.collections.vectorizers.Text2VecWeaviateVectorizer;
import io.weaviate.client6.v1.internal.ObjectBuilder;

/** Static methods for creating instances of {@link Vectorizer}. */
public final class Vectorizers {

  public static Map.Entry<String, Vectorizer> selfProvided() {
    return selfProvided(VectorIndex.DEFAULT_VECTOR_NAME);
  }

  public static Map.Entry<String, Vectorizer> selfProvided(
      Function<SelfProvidedVectorizer.Builder, ObjectBuilder<SelfProvidedVectorizer>> fn) {
    return selfProvided(VectorIndex.DEFAULT_VECTOR_NAME, fn);
  }

  public static Map.Entry<String, Vectorizer> selfProvided(String vectorName) {
    return Map.entry(vectorName, SelfProvidedVectorizer.of());
  }

  public static Map.Entry<String, Vectorizer> selfProvided(String vectorName,
      Function<SelfProvidedVectorizer.Builder, ObjectBuilder<SelfProvidedVectorizer>> fn) {
    return Map.entry(vectorName, SelfProvidedVectorizer.of(fn));
  }

  public static Map.Entry<String, Vectorizer> img2vecNeural() {
    return img2vecNeural(VectorIndex.DEFAULT_VECTOR_NAME);
  }

  public static Map.Entry<String, Vectorizer> img2vecNeural(
      Function<Img2VecNeuralVectorizer.Builder, ObjectBuilder<Img2VecNeuralVectorizer>> fn) {
    return img2vecNeural(VectorIndex.DEFAULT_VECTOR_NAME, fn);
  }

  public static Map.Entry<String, Vectorizer> img2vecNeural(String vectorName) {
    return Map.entry(vectorName, Img2VecNeuralVectorizer.of());
  }

  public static Map.Entry<String, Vectorizer> img2vecNeural(String vectorName,
      Function<Img2VecNeuralVectorizer.Builder, ObjectBuilder<Img2VecNeuralVectorizer>> fn) {
    return Map.entry(vectorName, Img2VecNeuralVectorizer.of(fn));
  }

  public static Map.Entry<String, Vectorizer> multi2vecClip() {
    return multi2vecClip(VectorIndex.DEFAULT_VECTOR_NAME);
  }

  public static Map.Entry<String, Vectorizer> multi2vecClip(
      Function<Multi2VecClipVectorizer.Builder, ObjectBuilder<Multi2VecClipVectorizer>> fn) {
    return multi2vecClip(VectorIndex.DEFAULT_VECTOR_NAME, fn);
  }

  public static Map.Entry<String, Vectorizer> multi2vecClip(String vectorName) {
    return Map.entry(vectorName, Multi2VecClipVectorizer.of());
  }

  public static Map.Entry<String, Vectorizer> multi2vecClip(String vectorName,
      Function<Multi2VecClipVectorizer.Builder, ObjectBuilder<Multi2VecClipVectorizer>> fn) {
    return Map.entry(vectorName, Multi2VecClipVectorizer.of(fn));
  }

  public static Map.Entry<String, Vectorizer> text2vecContextionary() {
    return text2vecContextionary(VectorIndex.DEFAULT_VECTOR_NAME);
  }

  public static Map.Entry<String, Vectorizer> text2vecContextionary(
      Function<Text2VecContextionaryVectorizer.Builder, ObjectBuilder<Text2VecContextionaryVectorizer>> fn) {
    return text2vecContextionary(VectorIndex.DEFAULT_VECTOR_NAME, fn);
  }

  public static Map.Entry<String, Vectorizer> text2vecContextionary(String vectorName) {
    return Map.entry(vectorName, Text2VecContextionaryVectorizer.of());
  }

  public static Map.Entry<String, Vectorizer> text2vecContextionary(String vectorName,
      Function<Text2VecContextionaryVectorizer.Builder, ObjectBuilder<Text2VecContextionaryVectorizer>> fn) {
    return Map.entry(vectorName, Text2VecContextionaryVectorizer.of(fn));
  }

  public static Map.Entry<String, Vectorizer> text2VecWeaviate() {
    return text2VecWeaviate(VectorIndex.DEFAULT_VECTOR_NAME);
  }

  public static Map.Entry<String, Vectorizer> text2VecWeaviate(
      Function<Text2VecWeaviateVectorizer.Builder, ObjectBuilder<Text2VecWeaviateVectorizer>> fn) {
    return text2VecWeaviate(VectorIndex.DEFAULT_VECTOR_NAME, fn);
  }

  public static Map.Entry<String, Vectorizer> text2VecWeaviate(String vectorName) {
    return Map.entry(vectorName, Text2VecWeaviateVectorizer.of());
  }

  public static Map.Entry<String, Vectorizer> text2VecWeaviate(String vectorName,
      Function<Text2VecWeaviateVectorizer.Builder, ObjectBuilder<Text2VecWeaviateVectorizer>> fn) {
    return Map.entry(vectorName, Text2VecWeaviateVectorizer.of(fn));
  }
}
