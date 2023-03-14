package io.weaviate.client.v1.misc.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@Getter
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Meta {
  String hostname;
  String version;
  Object modules;
}
