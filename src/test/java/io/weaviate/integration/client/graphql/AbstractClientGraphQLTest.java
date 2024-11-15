package io.weaviate.integration.client.graphql;

import io.weaviate.client.base.Result;
import io.weaviate.client.base.Serializer;
import io.weaviate.client.v1.graphql.model.GraphQLResponse;
import io.weaviate.integration.client.WeaviateTestGenerics;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.assertj.core.api.Assertions;

import java.util.*;
import java.util.stream.Collectors;

/** AbstractClientGraphQLTest has fixtures and assertion utils used for both sync and async tests. */
public abstract class AbstractClientGraphQLTest {
  protected static final WeaviateTestGenerics.DocumentPassageSchema testData = new WeaviateTestGenerics.DocumentPassageSchema();

  @Getter
  @AllArgsConstructor
  protected static class AdditionalOfDocument {
    String id;
  }

  @Getter
  protected static class Additional {
    Group group;
  }

  @Getter
  protected static class AdditionalGroupByAdditional {
    Additional _additional;
  }

  @Getter
  @AllArgsConstructor
  protected static class AdditionalGroupHit {
    String id;
    Float distance;
  }


  @Getter
  @AllArgsConstructor
  protected static class GroupHitOfDocument {
    AdditionalOfDocument _additional;
  }

  @Getter
  @AllArgsConstructor
  protected static class GroupHit {
    AdditionalGroupHit _additional;
    List<GroupHitOfDocument> ofDocument;
  }

  @Getter
  @AllArgsConstructor
  protected static class GroupedBy {
    public String value;
    public String[] path;
  }

  @Getter
  @AllArgsConstructor
  protected static class Group {
    public String id;
    public GroupedBy groupedBy;
    public Integer count;
    public Float maxDistance;
    public Float minDistance;
    public List<GroupHit> hits;
  }

  protected static final List<GroupHitOfDocument> ofDocumentA = Collections.singletonList(
    new GroupHitOfDocument(new AdditionalOfDocument(testData.DOCUMENT_IDS[0]))
  );
  protected static final List<GroupHitOfDocument> ofDocumentB = Collections.singletonList(
    new GroupHitOfDocument(new AdditionalOfDocument(testData.DOCUMENT_IDS[1]))
  );

  protected static final List<GroupHit> expectedHitsA = new ArrayList<GroupHit>() {
    {
      this.add(new GroupHit(new AdditionalGroupHit(testData.PASSAGE_IDS[0], 4.172325e-7f), ofDocumentA));
      this.add(new GroupHit(new AdditionalGroupHit(testData.PASSAGE_IDS[8], 0.0023148656f), ofDocumentA));
      this.add(new GroupHit(new AdditionalGroupHit(testData.PASSAGE_IDS[6], 0.0023562312f), ofDocumentA));
      this.add(new GroupHit(new AdditionalGroupHit(testData.PASSAGE_IDS[7], 0.0025092363f), ofDocumentA));
      this.add(new GroupHit(new AdditionalGroupHit(testData.PASSAGE_IDS[5], 0.002709806f), ofDocumentA));
      this.add(new GroupHit(new AdditionalGroupHit(testData.PASSAGE_IDS[9], 0.002762556f), ofDocumentA));
      this.add(new GroupHit(new AdditionalGroupHit(testData.PASSAGE_IDS[4], 0.0028533936f), ofDocumentA));
      this.add(new GroupHit(new AdditionalGroupHit(testData.PASSAGE_IDS[3], 0.0033442378f), ofDocumentA));
      this.add(new GroupHit(new AdditionalGroupHit(testData.PASSAGE_IDS[2], 0.004181564f), ofDocumentA));
      this.add(new GroupHit(new AdditionalGroupHit(testData.PASSAGE_IDS[1], 0.0057129264f), ofDocumentA));
    }
  };

  protected static final List<GroupHit> expectedHitsB = new ArrayList<GroupHit>() {
    {
      this.add(new GroupHit(new AdditionalGroupHit(testData.PASSAGE_IDS[10], 0.0025351048f), ofDocumentB));
      this.add(new GroupHit(new AdditionalGroupHit(testData.PASSAGE_IDS[12], 0.00288558f), ofDocumentB));
      this.add(new GroupHit(new AdditionalGroupHit(testData.PASSAGE_IDS[11], 0.0033002496f), ofDocumentB));
      this.add(new GroupHit(new AdditionalGroupHit(testData.PASSAGE_IDS[13], 0.004168868f), ofDocumentB));
    }
  };

  protected void assertIds(String className, Result<GraphQLResponse> gqlResult, String[] expectedIds) {
    Assertions.assertThat(gqlResult).isNotNull().returns(false, Result::hasErrors)
      .extracting(Result::getResult).isNotNull()
      .extracting(GraphQLResponse::getData).isInstanceOf(Map.class)
      .extracting(data -> ((Map<String, Object>) data).get("Get")).isInstanceOf(Map.class)
      .extracting(get -> ((Map<String, Object>) get).get(className)).isInstanceOf(List.class).asList().hasSize(expectedIds.length);

    List<Map<String, Object>> results = (List<Map<String, Object>>) ((Map<String, Object>) (((Map<String, Object>)
      (gqlResult.getResult().getData())).get("Get"))).get(className);
    String[] resultIds = results.stream().map(m -> m.get("_additional")).map(a -> ((Map<String, String>) a).get("id")).toArray(String[]::new);
    Assertions.assertThat(resultIds).containsExactlyInAnyOrder(expectedIds);
  }

  protected List<Group> getGroups(List<Map<String, Object>> result) {
    Serializer serializer = new Serializer();
    String jsonString = serializer.toJsonString(result);
    AdditionalGroupByAdditional[] response = serializer.toObject(jsonString, AdditionalGroupByAdditional[].class);
    Assertions.assertThat(response).isNotNull().hasSize(3);
    return Arrays.stream(response).map(AdditionalGroupByAdditional::get_additional).map(Additional::getGroup).collect(Collectors.toList());
  }

  protected void checkGroupElements(List<GroupHit> expected, List<GroupHit> actual) {
    Assertions.assertThat(expected).hasSameSizeAs(actual);
    for (int i = 0; i < actual.size(); i++) {
      Assertions.assertThat(actual.get(i).get_additional().getId()).isEqualTo(expected.get(i).get_additional().getId());
      Assertions.assertThat(actual.get(i).getOfDocument().get(0).get_additional().getId())
        .isEqualTo(expected.get(i).getOfDocument().get(0).get_additional().getId());
    }
  }
}
