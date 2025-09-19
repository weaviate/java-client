package io.weaviate.client6.v1.api.collections;

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
    @SerializedName("tokenization") Tokenization tokenization,
    @SerializedName("skipVectorization") Boolean skipVectorization,
    @SerializedName("vectorizePropertyName") Boolean vectorizePropertyName) {

  /**
   * Create a {@code text} property.
   *
   * @param name Property name.
   */
  public static Property text(String name) {
    return text(name, ObjectBuilder.identity());
  }

  /**
   * Create a {@code text} property with additional configuration.
   *
   * @param name Property name.
   * @param fn   Lambda expression for optional parameters.
   */
  public static Property text(String name, Function<Builder, ObjectBuilder<Property>> fn) {
    return newProperty(name, DataType.TEXT, fn);
  }

  /**
   * Create a {@code text} property.
   *
   * @param name Property name.
   */
  public static Property textArray(String name) {
    return textArray(name, ObjectBuilder.identity());
  }

  /**
   * Create a {@code text[]} property with additional configuration.
   *
   * @param name Property name.
   * @param fn   Lambda expression for optional parameters.
   */
  public static Property textArray(String name, Function<Builder, ObjectBuilder<Property>> fn) {
    return newProperty(name, DataType.TEXT_ARRAY, fn);
  }

  /**
   * Create a {@code int} property.
   *
   * @param name Property name.
   */
  public static Property integer(String name) {
    return integer(name, ObjectBuilder.identity());
  }

  /**
   * Create a {@code int} property with additional configuration.
   *
   * @param name Property name.
   * @param fn   Lambda expression for optional parameters.
   */
  public static Property integer(String name, Function<Builder, ObjectBuilder<Property>> fn) {
    return newProperty(name, DataType.INT, fn);
  }

  /**
   * Create a {@code int[]} property.
   *
   * @param name Property name.
   */
  public static Property integerArray(String name) {
    return integerArray(name, ObjectBuilder.identity());
  }

  /**
   * Create a {@code int[]} property with additional configuration.
   *
   * @param name Property name.
   * @param fn   Lambda expression for optional parameters.
   */
  public static Property integerArray(String name, Function<Builder, ObjectBuilder<Property>> fn) {
    return newProperty(name, DataType.INT_ARRAY, fn);
  }

  /**
   * Create a {@code bool} property.
   *
   * @param name Property name.
   */
  public static Property blob(String name) {
    return blob(name, ObjectBuilder.identity());
  }

  /**
   * Create a {@code blob} property with additional configuration.
   *
   * @param name Property name.
   * @param fn   Lambda expression for optional parameters.
   */
  public static Property blob(String name, Function<Builder, ObjectBuilder<Property>> fn) {
    return newProperty(name, DataType.BLOB, fn);
  }

  /**
   * Create a {@code bool} property.
   *
   * @param name Property name.
   */
  public static Property bool(String name) {
    return bool(name, ObjectBuilder.identity());
  }

  /**
   * Create a {@code bool} property with additional configuration.
   *
   * @param name Property name.
   * @param fn   Lambda expression for optional parameters.
   */

  public static Property bool(String name, Function<Builder, ObjectBuilder<Property>> fn) {
    return newProperty(name, DataType.BOOL, fn);
  }

  /**
   * Create a {@code bool[]} property.
   *
   * @param name Property name.
   */
  public static Property boolArray(String name) {
    return boolArray(name, ObjectBuilder.identity());
  }

  /**
   * Create a {@code bool[]} property with additional configuration.
   *
   * @param name Property name.
   * @param fn   Lambda expression for optional parameters.
   */
  public static Property boolArray(String name, Function<Builder, ObjectBuilder<Property>> fn) {
    return newProperty(name, DataType.BOOL_ARRAY, fn);
  }

  /**
   * Create a {@code date} property.
   *
   * @param name Property name.
   */
  public static Property date(String name) {
    return date(name, ObjectBuilder.identity());
  }

  /**
   * Create a {@code date} property with additional configuration.
   *
   * @param name Property name.
   * @param fn   Lambda expression for optional parameters.
   */
  public static Property date(String name, Function<Builder, ObjectBuilder<Property>> fn) {
    return newProperty(name, DataType.DATE, fn);
  }

  /**
   * Create a {@code date[]} property.
   *
   * @param name Property name.
   */
  public static Property dateArray(String name) {
    return dateArray(name, ObjectBuilder.identity());
  }

  /**
   * Create a {@code date[]} property with additional configuration.
   *
   * @param name Property name.
   * @param fn   Lambda expression for optional parameters.
   */
  public static Property dateArray(String name, Function<Builder, ObjectBuilder<Property>> fn) {
    return newProperty(name, DataType.DATE_ARRAY, fn);
  }

  /**
   * Create a {@code uuid} property.
   *
   * @param name Property name.
   */
  public static Property uuid(String name) {
    return uuid(name, ObjectBuilder.identity());
  }

  /**
   * Create a {@code uuid} property with additional configuration.
   *
   * @param name Property name.
   * @param fn   Lambda expression for optional parameters.
   */
  public static Property uuid(String name, Function<Builder, ObjectBuilder<Property>> fn) {
    return newProperty(name, DataType.UUID, fn);
  }

  /**
   * Create a {@code uuid[]} property.
   *
   * @param name Property name.
   */
  public static Property uuidArray(String name) {
    return uuidArray(name, ObjectBuilder.identity());
  }

  /**
   * Create a {@code uuid[]} property with additional configuration.
   *
   * @param name Property name.
   * @param fn   Lambda expression for optional parameters.
   */
  public static Property uuidArray(String name, Function<Builder, ObjectBuilder<Property>> fn) {
    return newProperty(name, DataType.UUID_ARRAY, fn);
  }

  /**
   * Create a {@code number} property.
   *
   * @param name Property name.
   */
  public static Property number(String name) {
    return number(name, ObjectBuilder.identity());
  }

  /**
   * Create a {@code number} property with additional configuration.
   *
   * @param name Property name.
   * @param fn   Lambda expression for optional parameters.
   */
  public static Property number(String name, Function<Builder, ObjectBuilder<Property>> fn) {
    return newProperty(name, DataType.NUMBER, fn);
  }

  /**
   * Create a {@code number[]} property.
   *
   * @param name Property name.
   */
  public static Property numberArray(String name) {
    return numberArray(name, ObjectBuilder.identity());
  }

  /**
   * Create a {@code number[]} property with additional configuration.
   *
   * @param name Property name.
   * @param fn   Lambda expression for optional parameters.
   */
  public static Property numberArray(String name, Function<Builder, ObjectBuilder<Property>> fn) {
    return newProperty(name, DataType.NUMBER_ARRAY, fn);
  }

  private static Property newProperty(String name, String dataType, Function<Builder, ObjectBuilder<Property>> fn) {
    return fn.apply(new Builder(name, dataType)).build();
  }

  /**
   * Create a new "edit" builder from the property configuration. Consult the <a
   * href=
   * "https://docs.weaviate.io/weaviate/config-refs/collections#properties">documentation</a>
   * to see which configuration is mutable before updating it.
   *
   * Example: Update property description.
   *
   * <pre>{@code
   * Property updated = propertyHeight.edit()
   *     .description("How tall this building is.")
   *     .build();
   * }</pre>
   *
   * @see #edit(Function)
   */
  public Builder edit() {
    return new Builder(propertyName, dataTypes)
        .description(description)
        .indexInverted(indexInverted)
        .indexFilterable(indexFilterable)
        .indexRangeFilters(indexRangeFilters)
        .indexSearchable(indexSearchable)
        .tokenization(tokenization)
        .skipVectorization(skipVectorization)
        .vectorizePropertyName(vectorizePropertyName);
  }

  /**
   * Pass a lambda expression to update property configuration. Consult the <a
   * href=
   * "https://docs.weaviate.io/weaviate/config-refs/collections#properties">documentation</a>
   * to see which configuration is mutable before updating it.
   *
   * Example: Update property description.
   *
   * <pre>{@code
   * Property updated = propertyHeight.edit(
   *     p -> p.description("How tall this building is."));
   * }</pre>
   *
   * @see #edit()
   */
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
        builder.tokenization,
        builder.skipVectorization,
        builder.vectorizePropertyName);
  }

  // All methods accepting a `boolean` should have a boxed overload
  // to be used by Property::edit.
  //
  // There we can't just do:
  // .indexInverted(indexInverted == null ? false : indexInverted)
  // because that may change the value from `null` to Boolean.FALSE,
  // effectively updating this setting. In the context of PUT /schema/{collection}
  // call this becomes a problem because we're not allowed to update anything
  // except for the description.
  //
  // The alternative (wrapping each call in an if-block) seems too verbose.
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
    private Tokenization tokenization;
    private Boolean skipVectorization;
    private Boolean vectorizePropertyName;

    /**
     * Create a scalar / array type property.
     *
     * @param dataType Property data type, see {@link DataType}.
     */
    public Builder(String propertyName, String dataType) {
      this.propertyName = propertyName;
      this.dataTypes = List.of(dataType);
    }

    /**
     * Create a cross-reference property.
     *
     * @param dataTypes List of collection names this property can reference.
     */
    public Builder(String propertyName, List<String> dataTypes) {
      this.propertyName = propertyName;
      this.dataTypes = List.copyOf(dataTypes);
    }

    /** Add property description. */
    public Builder description(String description) {
      this.description = description;
      return this;
    }

    public Builder indexInverted(boolean indexInverted) {
      this.indexInverted = indexInverted;
      return this;
    }

    /** Convenience method to be used by {@link Property#edit}. */
    Builder indexInverted(Boolean indexInverted) {
      this.indexInverted = indexInverted;
      return this;
    }

    /**
     * Set to true to create a filtering index for this property.
     *
     * <p>
     * Filterable indices are not applicable to {@code blob}, {@code object},
     * {@code geoCoordinates}, and {@code phoneNumber} properties or arrays thereof.
     *
     * @see <a href=
     *      "https://docs.weaviate.io/weaviate/concepts/indexing/inverted-index#configure-inverted-indexes">Inverted
     *      Indexes</a>
     */
    public Builder indexFilterable(boolean indexFilterable) {
      this.indexFilterable = indexFilterable;
      return this;
    }

    /** Convenience method to be used by {@link Property#edit}. */
    Builder indexFilterable(Boolean indexFilterable) {
      this.indexFilterable = indexFilterable;
      return this;
    }

    /**
     * Set to true to create a range-based filter for filtering
     * by numerical ranges for this property.
     *
     * <p>
     * Applicable to {code int}, {@code number}, and {@code date} properties.
     *
     * @see <a href=
     *      "https://docs.weaviate.io/weaviate/concepts/indexing/inverted-index#configure-inverted-indexes">Inverted
     *      Indexes</a>
     */
    public Builder indexRangeFilters(boolean indexRangeFilters) {
      this.indexRangeFilters = indexRangeFilters;
      return this;
    }

    /** Convenience method to be used by {@link Property#edit}. */
    Builder indexRangeFilters(Boolean indexRangeFilters) {
      this.indexRangeFilters = indexRangeFilters;
      return this;
    }

    /**
     * Set to true to create a searchable index for this property.
     *
     * <p>
     * This index type enables BM25/hybrid search and is only applicable to
     * {@code text}/{@code text[]} fields. For those it is also created
     * by default; you should set {@code indexInverted(false)} if you
     * do not plan to run BM25/hybrid queries on this property.
     *
     * @see <a href=
     *      "https://docs.weaviate.io/weaviate/concepts/indexing/inverted-index#configure-inverted-indexes">Inverted
     *      Indexes</a>
     */
    public Builder indexSearchable(boolean indexSearchable) {
      this.indexSearchable = indexSearchable;
      return this;
    }

    /** Convenience method to be used by {@link Property#edit}. */
    Builder indexSearchable(Boolean indexSearchable) {
      this.indexSearchable = indexSearchable;
      return this;
    }

    /**
     * Change tokenization method for this property.
     *
     * @see <a href=
     *      "https://docs.weaviate.io/academy/py/tokenization/options">Tokenization</a>
     */
    public Builder tokenization(Tokenization tokenization) {
      this.tokenization = tokenization;
      return this;
    }

    public Builder skipVectorization(boolean skipVectorization) {
      this.skipVectorization = skipVectorization;
      return this;
    }

    /** Convenience method to be used by {@link Property#edit}. */
    Builder skipVectorization(Boolean skipVectorization) {
      this.skipVectorization = skipVectorization;
      return this;
    }

    /** Include property name into the input for the vectorizer module. */
    public Builder vectorizePropertyName(boolean vectorizePropertyName) {
      this.vectorizePropertyName = vectorizePropertyName;
      return this;
    }

    /** Convenience method to be used by {@link Property#edit}. */
    Builder vectorizePropertyName(Boolean vectorizePropertyName) {
      this.vectorizePropertyName = vectorizePropertyName;
      return this;
    }

    @Override
    public Property build() {
      return new Property(this);
    }
  }
}
