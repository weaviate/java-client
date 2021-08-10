package technology.semi.weaviate.client.v1.classifications.model;

public final class ClassificationType {
  // KNN (k nearest neighbours) a non parametric classification based on training data
  public static final String KNN = "knn";
  // Contextual classification labels a data object with
  // the closest label based on their vector position (which describes the context)
  public static final String Contextual = "text2vec-contextionary";
  // ZeroShot classification labels a data object with
  // the closest label based on their vector position (which describes the context)
  // It can be used with any vectorizer or custom vectors.
  public static final String ZeroShot = "zeroshot";
}
