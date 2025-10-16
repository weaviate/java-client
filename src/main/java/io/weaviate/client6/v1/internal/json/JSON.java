package io.weaviate.client6.v1.internal.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.ToNumberPolicy;
import com.google.gson.reflect.TypeToken;

import io.weaviate.client6.v1.internal.orm.PropertyFieldNamingStrategy;

public final class JSON {
  private static final Gson gson;

  static {
    var gsonBuilder = new GsonBuilder();

    // TypeAdapterFactories ---------------------------------------------------
    gsonBuilder.registerTypeAdapterFactory(
        io.weaviate.client6.v1.api.rbac.Permission.CustomTypeAdapterFactory.INSTANCE);
    gsonBuilder.registerTypeAdapterFactory(
        io.weaviate.client6.v1.api.rbac.Role.CustomTypeAdapterFactory.INSTANCE);
    gsonBuilder.registerTypeAdapterFactory(
        io.weaviate.client6.v1.api.collections.WeaviateObject.CustomTypeAdapterFactory.INSTANCE);
    gsonBuilder.registerTypeAdapterFactory(
        io.weaviate.client6.v1.api.collections.data.WriteWeaviateObject.CustomTypeAdapterFactory.INSTANCE);
    gsonBuilder.registerTypeAdapterFactory(
        io.weaviate.client6.v1.api.collections.CollectionConfig.CustomTypeAdapterFactory.INSTANCE);
    gsonBuilder.registerTypeAdapterFactory(
        io.weaviate.client6.v1.api.collections.Vectors.CustomTypeAdapterFactory.INSTANCE);
    gsonBuilder.registerTypeAdapterFactory(
        io.weaviate.client6.v1.api.collections.VectorConfig.CustomTypeAdapterFactory.INSTANCE);
    gsonBuilder.registerTypeAdapterFactory(
        io.weaviate.client6.v1.api.collections.VectorIndex.CustomTypeAdapterFactory.INSTANCE);
    gsonBuilder.registerTypeAdapterFactory(
        io.weaviate.client6.v1.api.collections.Reranker.CustomTypeAdapterFactory.INSTANCE);
    gsonBuilder.registerTypeAdapterFactory(
        io.weaviate.client6.v1.api.collections.Generative.CustomTypeAdapterFactory.INSTANCE);
    gsonBuilder.registerTypeAdapterFactory(
        io.weaviate.client6.v1.api.collections.Quantization.CustomTypeAdapterFactory.INSTANCE);
    gsonBuilder.registerTypeAdapterFactory(
        io.weaviate.client6.v1.api.collections.vectorindex.MultiVector.CustomTypeAdapterFactory.INSTANCE);
    gsonBuilder.registerTypeAdapterFactory(
        io.weaviate.client6.v1.api.collections.Encoding.CustomTypeAdapterFactory.INSTANCE);
    gsonBuilder.registerTypeAdapterFactory(
        io.weaviate.client6.v1.internal.DateUtil.CustomTypeAdapterFactory.INSTANCE);

    // TypeAdapters -----------------------------------------------------------
    gsonBuilder.registerTypeAdapter(
        io.weaviate.client6.v1.api.collections.data.Reference.class,
        io.weaviate.client6.v1.api.collections.data.Reference.TYPE_ADAPTER);
    gsonBuilder.registerTypeAdapter(
        io.weaviate.client6.v1.api.collections.data.BatchReference.class,
        io.weaviate.client6.v1.api.collections.data.BatchReference.TYPE_ADAPTER);

    // Deserilizers -----------------------------------------------------------
    gsonBuilder.registerTypeAdapter(
        io.weaviate.client6.v1.api.collections.data.ReferenceAddManyResponse.class,
        io.weaviate.client6.v1.api.collections.data.ReferenceAddManyResponse.CustomJsonDeserializer.INSTANCE);

    // ORM FieldNaminsStrategy ------------------------------------------------
    gsonBuilder.setFieldNamingStrategy(PropertyFieldNamingStrategy.INSTANCE);

    gsonBuilder.setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE);
    gson = gsonBuilder.create();
  }

  public static final Gson getGson() {
    return gson;
  }

  public static final String serialize(Object value) {
    if (value == null) {
      return null;
    }
    return serialize(value, TypeToken.get(value.getClass()));
  }

  public static final String serialize(Object value, TypeToken<?> typeToken) {
    return gson.toJson(value, typeToken.getType());
  }

  public static final <T> T deserialize(String json, Class<T> cls) {
    return gson.fromJson(json, cls);
  }

  public static final <T> T deserialize(String json, TypeToken<T> token) {
    return gson.fromJson(json, token);
  }
}
