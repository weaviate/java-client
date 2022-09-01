package technology.semi.weaviate.client.v1.backup.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BackupCreatePayload {

  String id;
  Config config;
  String[] include;
  String[] exclude;


  @Getter
  @Builder
  @FieldDefaults(level = AccessLevel.PRIVATE)
  public static class Config {
    // TBD
  }
}
