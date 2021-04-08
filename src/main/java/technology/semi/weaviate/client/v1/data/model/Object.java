package technology.semi.weaviate.client.v1.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.Map;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Object {
  String id;
  @SerializedName("class")
  String className;
  Long creationTimeUnix;
  Long lastUpdateTimeUnix;
  Map<String, java.lang.Object> properties;
  Map<String, java.lang.Object> additional;
  Float[] vector;
  Object vectorWeights;
}
