package technology.semi.weaviate.client.v1.classifications.model;

public interface ClassificationType {
  // KNN (k nearest neighbours) a non parametric classification based on training data
  String KNN = "knn";
  // Contextual classification labels a data object with
  // the closest label based on their vector position (which describes the context)
  String Contextual = "text2vec-contextionary";
}
