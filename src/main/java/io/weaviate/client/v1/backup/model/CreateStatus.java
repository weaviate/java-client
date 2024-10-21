package io.weaviate.client.v1.backup.model;

public interface CreateStatus {

  String STARTED = "STARTED";
  String TRANSFERRING = "TRANSFERRING";
  String TRANSFERRED = "TRANSFERRED";
  String SUCCESS = "SUCCESS";
  String FAILED = "FAILED";
  String CANCELED = "CANCELED";
}
