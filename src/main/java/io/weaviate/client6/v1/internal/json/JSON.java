package io.weaviate.client6.v1.internal.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public final class JSON {
  private static final Gson gson;

  static {
    var gsonBuilder = new GsonBuilder();
    gsonBuilder.registerTypeAdapterFactory(
        io.weaviate.client6.v1.api.collections.WeaviateCollection.CustomTypeAdapterFactory.INSTANCE);
    gsonBuilder.registerTypeAdapterFactory(
        io.weaviate.client6.v1.api.collections.Vectors.CustomTypeAdapterFactory.INSTANCE);
    gsonBuilder.registerTypeAdapterFactory(
        io.weaviate.client6.v1.api.collections.Vectorizer.CustomTypeAdapterFactory.INSTANCE);
    gsonBuilder.registerTypeAdapterFactory(
        io.weaviate.client6.v1.api.collections.VectorIndex.CustomTypeAdapterFactory.INSTANCE);

    gsonBuilder.registerTypeAdapter(
        io.weaviate.client6.v1.api.collections.vectorizers.NoneVectorizer.class,
        io.weaviate.client6.v1.api.collections.vectorizers.NoneVectorizer.TYPE_ADAPTER);
    gson = gsonBuilder.create();
  }

  public static final String serialize(Object value) {
    return gson.toJson(value);
  }

  public static final <T> T deserialize(String json, Class<T> cls) {
    return gson.fromJson(json, cls);
  }
}
