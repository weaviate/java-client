package io.weaviate.client6.v1.api.collections.generate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.weaviate.client6.v1.api.collections.CollectionHandleDefaults;
import io.weaviate.client6.v1.api.collections.query.QueryObjectGrouped;
import io.weaviate.client6.v1.api.collections.query.QueryResponse;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoSearchGet;
import io.weaviate.client6.v1.internal.orm.CollectionDescriptor;

public record GenerativeResponseGrouped<PropertiesT>(
    /** Execution time of the request as measure by the server. */
    float took,
    /** Objects returned by the associated query. */
    List<QueryObjectGrouped<PropertiesT>> objects,
    /** Grouped results with per-group generated output. */
    Map<String, GenerativeResponseGroup<PropertiesT>> groups,
    /** Output of the summary group task. */
    TaskOutput generative) {

  static <PropertiesT> GenerativeResponseGrouped<PropertiesT> unmarshal(
      WeaviateProtoSearchGet.SearchReply reply,
      CollectionDescriptor<PropertiesT> collection,
      CollectionHandleDefaults defaults) {
    var allObjects = new ArrayList<QueryObjectGrouped<PropertiesT>>();
    var groups = reply.getGroupByResultsList().stream()
        .map(group -> {
          var groupName = group.getName();
          List<QueryObjectGrouped<PropertiesT>> objects = group.getObjectsList().stream()
              .map(object -> QueryResponse.unmarshalResultObject(
                  object.getProperties(),
                  object.getMetadata(),
                  collection))
              .map(object -> new QueryObjectGrouped<>(
                  object.uuid(),
                  object.vectors(),
                  object.properties(),
                  object.queryMetadata(),
                  groupName))
              .toList();

          allObjects.addAll(objects);

          TaskOutput generative = null;
          if (group.hasGenerativeResult()) {
            generative = GenerativeResponse.unmarshalTaskOutput(group.getGenerativeResult());
          } else if (group.hasGenerative()) {
            // As of today the server continues to use the deprecated field in response.
            generative = GenerativeResponse.unmarshalTaskOutput(List.of(group.getGenerative()));
          }

          return new GenerativeResponseGroup<>(
              groupName,
              group.getMinDistance(),
              group.getMaxDistance(),
              group.getNumberOfObjects(),
              objects,
              generative);
        })
        // Collectors.toMap() throws an NPE if either key or value in the map are null.
        // In this specific case it is safe to use it, as the function in the map above
        // always returns a QueryResponseGroup.
        // The name of the group should not be null either, that's something we assume
        // about the server's response.
        .collect(Collectors.toMap(GenerativeResponseGroup::name, Function.identity()));

    TaskOutput summary = null;
    if (reply.hasGenerativeGroupedResults()) {
      summary = GenerativeResponse.unmarshalTaskOutput(reply.getGenerativeGroupedResults());
    } else if (reply.hasGenerativeGroupedResult()) {
      summary = new TaskOutput(reply.getGenerativeGroupedResult(), null, null);
    }
    return new GenerativeResponseGrouped<>(reply.getTook(), allObjects, groups, summary);
  }
}
