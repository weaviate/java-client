package technology.semi.weaviate.client.v1.backup.model;

import com.google.gson.annotations.SerializedName;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BackupCreateMeta {

  String id;
  String path;
  String storageName;
  @SerializedName("classes")
  String[] classNames;
  String status;
  String error;
}
