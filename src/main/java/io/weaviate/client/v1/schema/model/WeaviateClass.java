package io.weaviate.client.v1.schema.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

import io.weaviate.client.v1.misc.model.MultiTenancyConfig;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import io.weaviate.client.v1.misc.model.InvertedIndexConfig;
import io.weaviate.client.v1.misc.model.ReplicationConfig;
import io.weaviate.client.v1.misc.model.ShardingConfig;
import io.weaviate.client.v1.misc.model.VectorIndexConfig;

@Getter
@Builder
@ToString
@EqualsAndHashCode
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class WeaviateClass {
  @SerializedName("class")
  String className;
  String description;
  InvertedIndexConfig invertedIndexConfig;
  Object moduleConfig;
  List<Property> properties;
  VectorIndexConfig vectorIndexConfig;
  ShardingConfig shardingConfig;
  String vectorIndexType;
  String vectorizer;
  ReplicationConfig replicationConfig;
  MultiTenancyConfig multiTenancyConfig;


  public static class WeaviateClassBuilder {

    private Object moduleConfig;

    @Deprecated
    public WeaviateClassBuilder ModuleConfig(Object moduleConfig) {
      this.moduleConfig = moduleConfig;
      return this;
    }
  }
}
