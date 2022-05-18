package technology.semi.weaviate.client.v1.graphql.query.argument;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class NearTextMoveParameters {
  String[] concepts;
  Float force;
  ObjectMove[] objects;

  @Getter
  @Builder
  @FieldDefaults(level = AccessLevel.PRIVATE)
  @EqualsAndHashCode
  public static class ObjectMove {
    String id;
    String beacon;
  }
}
