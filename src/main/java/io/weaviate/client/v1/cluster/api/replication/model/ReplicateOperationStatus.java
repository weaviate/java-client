package io.weaviate.client.v1.cluster.api.replication.model;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class ReplicateOperationStatus {
  @SerializedName("state")
  ReplicateOperationState state;
  @SerializedName("errors")
  List<String> errors;
}
