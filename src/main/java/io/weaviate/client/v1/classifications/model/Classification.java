package io.weaviate.client.v1.classifications.model;

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
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Classification {
  String[] basedOnProperties;
  @SerializedName("class")
  String className;
  String[] classifyProperties;
  String error;
  ClassificationFilters filters;
  String id;
  ClassificationMeta meta;
  Object settings;
  String status;
  String type;
}
