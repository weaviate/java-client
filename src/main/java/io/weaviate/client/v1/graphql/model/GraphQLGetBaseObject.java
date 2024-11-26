package io.weaviate.client.v1.graphql.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
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
    Group group;

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

    @Getter
    public static class Group {
      public String id;
      public GroupedBy groupedBy;
      public Integer count;
      public Float maxDistance;
      public Float minDistance;
      public List<GroupHit> hits;

      @Getter
      public static class GroupedBy {
        public String value;
        public String[] path;
      }

      @Getter
      @AllArgsConstructor
      public static class GroupHit {
        @SerializedName("properties")
        Map<String, Object> properties;
        @SerializedName(value = "_additional")
        AdditionalGroupHit additional;

        @Getter
        public static class AdditionalGroupHit {
          String id;
          Float distance;
        }
      }
    }
  }
}
