package technology.semi.weaviate.client.v1.backup.model;

public interface RestoreStatus {

  String STARTED = "STARTED";
  String TRANSFERRING = "TRANSFERRING";
  String TRANSFERRED = "TRANSFERRED";
  String SUCCESS = "SUCCESS";
  String FAILED = "FAILED";
}
