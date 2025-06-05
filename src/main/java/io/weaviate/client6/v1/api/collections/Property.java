package io.weaviate.client6.v1.api.collections;

import java.util.Arrays;
import java.util.List;

import com.google.gson.annotations.SerializedName;

public record Property(
    @SerializedName("name") String name,
    @SerializedName("dataType") List<String> dataTypes) {

  public Property(String name, String dataType) {
    this(name, List.of(dataType));
  }

  /** Add text property with default configuration. */
  public static Property text(String name) {
    return new Property(name, DataType.TEXT);
  }

  /** Add integer property with default configuration. */
  public static Property integer(String name) {
    return new Property(name, DataType.INT);
  }

  /** Add blob property with default configuration. */
  public static Property blob(String name) {
    return new Property(name, DataType.BLOB);
  }

  public static ReferenceProperty reference(String name, String... collections) {
    return new ReferenceProperty(name, Arrays.asList(collections));
  }

  public static ReferenceProperty reference(String name, List<String> collections) {
    return new ReferenceProperty(name, collections);
  }

  boolean isReference() {
    return dataTypes.size() > 1 || !DataType.KNOWN_TYPES.contains(dataTypes.get(0));
  }
}
