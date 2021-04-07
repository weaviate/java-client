package technology.semi.weaviate.client.v1.schema.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import technology.semi.weaviate.client.v1.misc.model.InvertedIndexConfig;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Class {
  @SerializedName("class")
  String className;
  String description;
  InvertedIndexConfig invertedIndexConfig;
  Object ModuleConfig;
  List<Property> properties;
  Object vectorIndexConfig;
  String vectorIndexType;
  String vectorizer;
}
