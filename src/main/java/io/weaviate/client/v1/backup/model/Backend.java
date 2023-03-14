package io.weaviate.client.v1.backup.model;

public interface Backend {

  String FILESYSTEM = "filesystem";
  String S3 = "s3";
  String GCS = "gcs";
  String AZURE = "azure";
}
