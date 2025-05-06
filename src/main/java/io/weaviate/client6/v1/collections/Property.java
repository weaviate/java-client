package io.weaviate.client6.v1.collections;

import java.util.Arrays;
import java.util.List;

import com.google.gson.annotations.SerializedName;

public record Property(
    @SerializedName("name") String name,
    @SerializedName("dataType") List<String> dataTypes) {

  /** Add text property with default configuration. */
  public static Property text(String name) {
    return new Property(name, AtomicDataType.TEXT);
  }

  /** Add integer property with default configuration. */
  public static Property integer(String name) {
    return new Property(name, AtomicDataType.INT);
  }

  public static ReferenceProperty reference(String name, String... collections) {
    return new ReferenceProperty(name, Arrays.asList(collections));
  }

  public static ReferenceProperty reference(String name, List<String> collections) {
    return new ReferenceProperty(name, collections);
  }

  public boolean isReference() {
    return dataTypes.stream().noneMatch(t -> AtomicDataType.isAtomic(t));
  }

  private Property(String name, AtomicDataType type) {
    this(name, List.of(type.name().toLowerCase()));
  }

}
