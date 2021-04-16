package technology.semi.weaviate.client.v1.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.Map;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class SingleRef {
  String beacon;
  @SerializedName("class")
  String clazz;
  ReferenceMetaClassification classification;
  String href;
  Map<String, Object> schema;
}
