package io.weaviate.client.v1.batch.model;

public interface BatchDeleteResultStatus {

    String SUCCESS = "SUCCESS";
    String FAILED = "FAILED";
    String DRYRUN = "DRYRUN";
}
