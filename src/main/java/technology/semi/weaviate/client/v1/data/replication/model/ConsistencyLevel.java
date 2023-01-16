package technology.semi.weaviate.client.v1.data.replication.model;

public interface ConsistencyLevel {
  String ALL = "ALL";
  String ONE = "ONE";
  String QUORUM = "QUORUM";
}
