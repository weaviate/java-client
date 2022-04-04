package technology.semi.weaviate.client.v1.schema.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import technology.semi.weaviate.client.v1.misc.model.InvertedIndexConfig;
import technology.semi.weaviate.client.v1.misc.model.ShardingConfig;
import technology.semi.weaviate.client.v1.misc.model.VectorIndexConfig;

@Getter
@Builder
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class WeaviateClass {
  @SerializedName("class")
  String className;
  String description;
  InvertedIndexConfig invertedIndexConfig;
  Object ModuleConfig;
  List<Property> properties;
  VectorIndexConfig vectorIndexConfig;
  ShardingConfig shardingConfig;
  String vectorIndexType;
  String vectorizer;
}
