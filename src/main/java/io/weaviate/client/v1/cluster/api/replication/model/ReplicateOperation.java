package io.weaviate.client.v1.cluster.api.replication.model;

import java.util.List;

import com.google.gson.annotations.SerializedName;

import io.weaviate.client.v1.cluster.model.ReplicationType;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class ReplicateOperation {
  @SerializedName("id")
  String uuid;
  @SerializedName("collection")
  String className;
  @SerializedName("shard")
  String shard;
  @SerializedName("sourceNode")
  String sourceNode;
  @SerializedName("targetNode")
  String targetNode;
  @SerializedName("status")
  ReplicateOperationStatus status;
  @SerializedName("statusHistory")
  List<ReplicateOperationStatus> statusHistory;
  @SerializedName("type")
  ReplicationType transferType;
}
