package io.weaviate.client.v1.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.Map;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@ToString
@EqualsAndHashCode
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class SingleRef {
  @SerializedName("class")
  String clazz;
  String beacon;
  ReferenceMetaClassification classification;
  String href;
  Map<String, Object> schema;
}
