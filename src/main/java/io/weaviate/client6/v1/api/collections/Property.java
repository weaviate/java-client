package io.weaviate.client6.v1.api.collections;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import com.google.gson.annotations.SerializedName;

import io.weaviate.client6.v1.internal.ObjectBuilder;

public record Property(
    @SerializedName("name") String propertyName,
    @SerializedName("dataType") List<String> dataTypes,
    @SerializedName("description") String description,
    @SerializedName("indexInverted") Boolean indexInverted,
    @SerializedName("indexFilterable") Boolean indexFilterable,
    @SerializedName("indexRangeFilters") Boolean indexRangeFilters,
    @SerializedName("indexSearchable") Boolean indexSearchable,
    @SerializedName("skipVectorization") Boolean skipVectorization,
    @SerializedName("vectorizePropertyName") Boolean vectorizePropertyName) {

  public static Property text(String name) {
    return text(name, ObjectBuilder.identity());
  }

  public static Property text(String name, Function<Builder, ObjectBuilder<Property>> fn) {
    return newProperty(name, DataType.TEXT, fn);
  }

  public static Property textArray(String name) {
    return textArray(name, ObjectBuilder.identity());
  }

  public static Property textArray(String name, Function<Builder, ObjectBuilder<Property>> fn) {
    return newProperty(name, DataType.TEXT_ARRAY, fn);
  }

  public static Property integer(String name) {
    return integer(name, ObjectBuilder.identity());
  }

  public static Property integer(String name, Function<Builder, ObjectBuilder<Property>> fn) {
    return newProperty(name, DataType.INT, fn);
  }

  public static Property integerArray(String name) {
    return integerArray(name, ObjectBuilder.identity());
  }

  public static Property integerArray(String name, Function<Builder, ObjectBuilder<Property>> fn) {
    return newProperty(name, DataType.INT_ARRAY, fn);
  }

  public static Property blob(String name) {
    return blob(name, ObjectBuilder.identity());
  }

  public static Property blob(String name, Function<Builder, ObjectBuilder<Property>> fn) {
    return newProperty(name, DataType.BLOB, fn);
  }

  public static Property bool(String name) {
    return bool(name, ObjectBuilder.identity());
  }

  public static Property bool(String name, Function<Builder, ObjectBuilder<Property>> fn) {
    return newProperty(name, DataType.BOOL, fn);
  }

  public static Property boolArray(String name) {
    return boolArray(name, ObjectBuilder.identity());
  }

  public static Property boolArray(String name, Function<Builder, ObjectBuilder<Property>> fn) {
    return newProperty(name, DataType.BOOL_ARRAY, fn);
  }

  public static Property date(String name) {
    return date(name, ObjectBuilder.identity());
  }

  public static Property date(String name, Function<Builder, ObjectBuilder<Property>> fn) {
    return newProperty(name, DataType.DATE, fn);
  }

  public static Property dateArray(String name) {
    return dateArray(name, ObjectBuilder.identity());
  }

  public static Property dateArray(String name, Function<Builder, ObjectBuilder<Property>> fn) {
    return newProperty(name, DataType.DATE_ARRAY, fn);
  }

  public static Property uuid(String name) {
    return uuid(name, ObjectBuilder.identity());
  }

  public static Property uuid(String name, Function<Builder, ObjectBuilder<Property>> fn) {
    return newProperty(name, DataType.UUID, fn);
  }

  public static Property uuidArray(String name) {
    return uuidArray(name, ObjectBuilder.identity());
  }

  public static Property uuidArray(String name, Function<Builder, ObjectBuilder<Property>> fn) {
    return newProperty(name, DataType.UUID_ARRAY, fn);
  }

  public static Property number(String name) {
    return number(name, ObjectBuilder.identity());
  }

  public static Property number(String name, Function<Builder, ObjectBuilder<Property>> fn) {
    return newProperty(name, DataType.NUMBER, fn);
  }

  public static Property numberArray(String name) {
    return numberArray(name, ObjectBuilder.identity());
  }

  public static Property numberArray(String name, Function<Builder, ObjectBuilder<Property>> fn) {
    return newProperty(name, DataType.NUMBER_ARRAY, fn);
  }

  private static Property newProperty(String name, String dataType, Function<Builder, ObjectBuilder<Property>> fn) {
    return fn.apply(new Builder(name, dataType)).build();
  }

  public static ReferenceProperty reference(String name, String... collections) {
    return new ReferenceProperty(name, Arrays.asList(collections));
  }

  public static ReferenceProperty reference(String name, List<String> collections) {
    return new ReferenceProperty(name, collections);
  }

  public Builder edit() {
    return new Builder(propertyName, dataTypes)
        .description(description)
        .indexInverted(indexInverted)
        .indexFilterable(indexFilterable)
        .indexRangeFilters(indexRangeFilters)
        .indexSearchable(indexSearchable)
        .skipVectorization(skipVectorization)
        .vectorizePropertyName(vectorizePropertyName);
  }

  public Property edit(Function<Builder, ObjectBuilder<Property>> fn) {
    return fn.apply(edit()).build();
  }

  public Property(Builder builder) {
    this(
        builder.propertyName,
        builder.dataTypes,
        builder.description,
        builder.indexInverted,
        builder.indexFilterable,
        builder.indexRangeFilters,
        builder.indexSearchable,
        builder.skipVectorization,
        builder.vectorizePropertyName);
  }

  public static class Builder implements ObjectBuilder<Property> {
    // Required parameters.
    private final String propertyName;

    // Optional parameters.
    private List<String> dataTypes;
    private String description;
    private Boolean indexInverted;
    private Boolean indexFilterable;
    private Boolean indexRangeFilters;
    private Boolean indexSearchable;
    private Boolean skipVectorization;
    private Boolean vectorizePropertyName;

    public Builder(String propertyName, String dataType) {
      this.propertyName = propertyName;
      this.dataTypes = List.of(dataType);
    }

    public Builder(String propertyName, String... dataTypes) {
      this(propertyName, Arrays.asList(dataTypes));
    }

    public Builder(String propertyName, List<String> dataTypes) {
      this.propertyName = propertyName;
      this.dataTypes = List.copyOf(dataTypes);
    }

    public Builder dataTypes(List<String> dataTypes) {
      this.dataTypes = dataTypes;
      return this;
    }

    public Builder description(String description) {
      this.description = description;
      return this;
    }

    public Builder indexInverted(Boolean indexInverted) {
      this.indexInverted = indexInverted;
      return this;
    }

    public Builder indexFilterable(Boolean indexFilterable) {
      this.indexFilterable = indexFilterable;
      return this;
    }

    public Builder indexRangeFilters(Boolean indexRangeFilters) {
      this.indexRangeFilters = indexRangeFilters;
      return this;
    }

    public Builder indexSearchable(Boolean indexSearchable) {
      this.indexSearchable = indexSearchable;
      return this;
    }

    public Builder skipVectorization(Boolean skipVectorization) {
      this.skipVectorization = skipVectorization;
      return this;
    }

    public Builder vectorizePropertyName(Boolean vectorizePropertyName) {
      this.vectorizePropertyName = vectorizePropertyName;
      return this;
    }

    @Override
    public Property build() {
      return new Property(this);
    }
  }
}
