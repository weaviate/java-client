package io.weaviate.client6.v1.api.collections;

import java.util.Map;

import io.weaviate.client6.v1.api.collections.data.ObjectReference;

public interface Reference {
  /** UUID of the reference object. */
  String uuid();

  /** Name of the collection the reference belongs to. */
  String collection();

  /**
   * Cast {@code this} into an instance of {@link WeaviateObject<Map<String,
   * Object>>}. Useful when working with references retrieved in a query.
   *
   * <pre>{@code
   *  var metalSongs = songs.query.fetchObjects(q -> q
   *    .filters(Filter.property("genres").containsAll("metal")
   *    .returnReferences(QueryReference.multi("performedBy"));
   *
   *  metalSongs.objects().forEach(song -> {
   *    var songName = song.properties().get("name");
   *    song.references().forEach(ref -> {
   *      var artistName = ref.asWeaviateObject().properties().get("artistName");
   *      System.out.printf("%s is performed by %s", songName, artistName);
   *    });
   *  });
   * }</pre>
   *
   * <p>
   * Only call this method on objects returned from methods under {@code .query}
   * namespace, as insert-references do not implement this interface.
   *
   * @throws IllegalStateException if reference object is an instance of
   *                               {@link ObjectReference}. See usage guidelines
   *                               above.
   */
  WeaviateObject<Map<String, Object>> asWeaviateObject();
}
