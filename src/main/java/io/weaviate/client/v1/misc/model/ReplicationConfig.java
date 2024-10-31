package io.weaviate.client.v1.misc.model;

import com.google.gson.annotations.SerializedName;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@ToString
@EqualsAndHashCode
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ReplicationConfig {
  Boolean asyncEnabled;
  Integer factor;
  DeletionStrategy deletionStrategy;

  public enum DeletionStrategy {
    @SerializedName("DeleteOnConflict")
    DELETE_ON_CONFLICT,
    @SerializedName("NoAutomatedResolution")
    NO_AUTOMATED_RESOLUTION;
  }
}
