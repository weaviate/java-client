package io.weaviate.client6.v1.collections;

import java.util.Arrays;
import java.util.List;

import com.google.gson.annotations.SerializedName;

public class Property {
  @SerializedName("name")
  public final String name;

  @SerializedName("dataType")
  public final List<String> dataTypes;

  /** Add text property with default configuration. */
  public static Property text(String name) {
    return new Property(name, DataType.TEXT);
  }

  /** Add integer property with default configuration. */
  public static Property integer(String name) {
    return new Property(name, DataType.INT);
  }

  public static Property reference(String name, String... collections) {
    return new Property(name, collections);
  }

  private Property(String name, DataType type) {
    this.name = name;
    this.dataTypes = List.of(type.name().toLowerCase());
  }

  private Property(String name, String... collections) {
    this.name = name;
    this.dataTypes = Arrays.asList(collections);
  }
}
