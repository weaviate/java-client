package technology.semi.weaviate.client.v1.backup.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BackupCreateStatusResponse {

  String id;
  String path;
  String storageName;
  String status;
  String error;
}
