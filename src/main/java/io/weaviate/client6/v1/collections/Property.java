package io.weaviate.client6.v1.collections;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import com.google.gson.annotations.SerializedName;

public class Property {
  @SerializedName("name")
  public final String name;

  @SerializedName("dataType")
  public final List<DataType> dataTypes;

  public static Property text(String name) {
    return new Property(name, DataType.TEXT);
  }

  public static Property integer(String name) {
    return new Property(name, DataType.INT);
  }

  public static final class Configuration {
    private List<DataType> dataTypes;

    public Configuration dataTypes(DataType... types) {
      this.dataTypes = Arrays.asList(types);
      return this;
    }
  }

  private Property(String name, DataType type) {
    this.name = name;
    this.dataTypes = List.of(type);
  }

  public Property(String name, Consumer<Configuration> options) {
    var config = new Configuration();
    options.accept(config);

    this.name = name;
    this.dataTypes = config.dataTypes;
  }
}
