package io.weaviate.client.v1.graphql.model;

import com.google.gson.annotations.SerializedName;
import java.util.Map;
import lombok.Getter;

@Getter
public class GraphQLGetBaseObject {
  @SerializedName(value = "_additional")
  Additional additional;

  @Getter
  public static class Additional {
    String id;
    Float certainty;
    Float distance;
    String creationTimeUnix;
    String lastUpdateTimeUnix;
    String explainScore;
    String score;
    Float[] vector;
    Map<String, Float[]> vectors;
    Generate generate;

    @Getter
    public static class Generate {
      String singleResult;
      String groupedResult;
      String error;
      Debug debug;
      @Getter
      public static class Debug {
        String prompt;
      }
    }
  }
}
