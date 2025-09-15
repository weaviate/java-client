package io.weaviate.client6.v1.api.collections;

import java.util.Arrays;
import java.util.List;

import com.google.gson.annotations.SerializedName;

public record ReferenceProperty(
    @SerializedName("name") String propertyName,
    @SerializedName("dataType") List<String> dataTypes) {

  /**
   * Create a cross-reference to another collection.
   *
   * <pre>{@code
   * // Single-target reference
   * ReferenceProperty.to("livesIn", "Cities");
   *
   * // Multi-reference
   * ReferenceProperty.to("hasSeen", "Movies", "Plays", "SoapOperas");
   * }</pre>
   *
   * @param name        Name of the property.
   * @param collections One or more collections which can be referenced.
   * @return ReferenceProperty
   */
  public static ReferenceProperty to(String name, String... collections) {
    return new ReferenceProperty(name, Arrays.asList(collections));
  }

  /**
   * Create a multi-target reference property.
   *
   * <pre>{@code
   * List<String> thingsToSee = List.of("Movies", "Plays", "SoapOperas");
   * ReferenceProperty.to("hasSeen", thingsToSee);
   * }</pre>
   *
   * @param name        Name of the property.
   * @param collections One or more collections which can be referenced.
   * @return ReferenceProperty
   */
  public static ReferenceProperty to(String name, List<String> collections) {
    return new ReferenceProperty(name, collections);
  }

  public Property toProperty() {
    return new Property.Builder(propertyName, dataTypes).build();
  }
}
