package io.weaviate.client.v1.misc.model;

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
public class MuveraConfig {
  @Builder.Default
  private boolean enabled = false;
  private Integer ksim;
  private Integer dprojections;
  private Integer repetitions;
}