package technology.semi.weaviate.client.v1.data.model;

import com.google.gson.annotations.SerializedName;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.util.Map;

@Getter
@Builder
@ToString
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class WeaviateObject {
  String id;
  @SerializedName("class")
  String className;
  Long creationTimeUnix;
  Long lastUpdateTimeUnix;
  Map<String, Object> properties;
  Map<String, Object> additional;
  Float[] vector;
  Object vectorWeights;
}
