package io.weaviate.client6.v1.api.collections.query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.weaviate.client6.v1.api.collections.CollectionHandleDefaults;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoSearchGet;
import io.weaviate.client6.v1.internal.orm.CollectionDescriptor;

public record QueryResponseGrouped<PropertiesT>(
    /** All objects retrieved in the query. */
    List<QueryObjectGrouped<PropertiesT>> objects,
    /** Grouped response objects. */
    Map<String, QueryResponseGroup<PropertiesT>> groups) {

  static <PropertiesT> QueryResponseGrouped<PropertiesT> unmarshal(
      WeaviateProtoSearchGet.SearchReply reply,
      CollectionDescriptor<PropertiesT> collection,
      CollectionHandleDefaults defaults) {
    var allObjects = new ArrayList<QueryObjectGrouped<PropertiesT>>();
    var groups = reply.getGroupByResultsList()
        .stream().map(group -> {
          var name = group.getName();
          List<QueryObjectGrouped<PropertiesT>> objects = group.getObjectsList().stream()
              .map(obj -> QueryResponse.unmarshalResultObject(
                  obj.getProperties(),
                  obj.getMetadata(),
                  collection))
              .map(obj -> new QueryObjectGrouped<>(obj, name))
              .toList();

          allObjects.addAll(objects);
          return new QueryResponseGroup<>(
              name,
              group.getMinDistance(),
              group.getMaxDistance(),
              group.getNumberOfObjects(),
              objects);
        })
        // Collectors.toMap() throws an NPE if either key or value in the map are null.
        // In this specific case it is safe to use it, as the function in the map above
        // always returns a QueryResponseGroup.
        // The name of the group should not be null either, that's something we assume
        // about the server's response.
        .collect(Collectors.toMap(QueryResponseGroup::name, Function.identity()));

    return new QueryResponseGrouped<PropertiesT>(allObjects, groups);
  }
}
