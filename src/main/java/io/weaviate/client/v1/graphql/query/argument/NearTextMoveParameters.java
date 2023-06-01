package io.weaviate.client.v1.graphql.query.argument;

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
public class NearTextMoveParameters {
  String[] concepts;
  Float force;
  ObjectMove[] objects;

  @Getter
  @Builder
  @ToString
  @EqualsAndHashCode
  @FieldDefaults(level = AccessLevel.PRIVATE)
  public static class ObjectMove {
    String id;
    String beacon;
  }
}
