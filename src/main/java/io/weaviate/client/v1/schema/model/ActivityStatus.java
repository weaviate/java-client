package io.weaviate.client.v1.schema.model;

public interface ActivityStatus {
  @Deprecated
  String WARM = "WARM";
  String HOT = "HOT";
  String COLD = "COLD";
  String FROZEN = "FROZEN";
  String FREEZING = "FREEZING";
  String UNFREEZING = "UNFREEZING";
  String ACTIVE = "ACTIVE";
  String INACTIVE = "INACTIVE";
  String OFFLOADED = "OFFLOADED";
  String OFFLOADING = "OFFLOADING";
  String ONLOADING = "ONLOADING";
}
