package technology.semi.weaviate.client.v1.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.Map;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@ToString
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class WeaviateObject {
  String id;
  @SerializedName("class")
  String className;
  Long creationTimeUnix;
  final Long lastUpdateTimeUnix;
  Map<String, Object> properties;
  Map<String, Object> additional;
  Float[] vector;
  Object vectorWeights;
}
