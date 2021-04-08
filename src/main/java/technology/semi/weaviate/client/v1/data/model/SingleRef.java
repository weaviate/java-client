package technology.semi.weaviate.client.v1.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.Map;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SingleRef {
  String beacon;
  @SerializedName("class")
  String clazz;
  ReferenceMetaClassification classification;
  String href;
  Map<String, java.lang.Object> schema;
}
