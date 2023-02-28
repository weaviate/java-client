package technology.semi.weaviate.client.v1.misc.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@ToString
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class PQConfig {

  Boolean enabled;
  Boolean bitCompression;
  Integer segments;
  Integer centroids;
  Encoder encoder;


  @Getter
  @Builder
  @ToString
  @FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
  public static class Encoder {
    String type;
    String distribution;
  }
}
