package technology.semi.weaviate.client.v1.backup.model;

import com.google.gson.annotations.SerializedName;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BackupRestoreResponse {

  String id;
  String path;
  String backend;
  @SerializedName("classes")
  String[] classNames;
  String status;
  String error;
}
