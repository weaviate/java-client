package io.weaviate.client.v1.contextionary.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class C11yExtension {
  String concept;
  String definition;
  Float weight;
}
