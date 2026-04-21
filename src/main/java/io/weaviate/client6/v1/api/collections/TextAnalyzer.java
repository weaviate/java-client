package io.weaviate.client6.v1.api.collections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import com.google.gson.annotations.SerializedName;

import io.weaviate.client6.v1.internal.ObjectBuilder;

public record TextAnalyzer(
    @SerializedName("ascii_fold") Boolean foldAscii,
    @SerializedName("ascii_fold_ignore") List<String> keepAscii,
    @SerializedName("stopword_preset") String stopwordPreset) {

  public static TextAnalyzer of() {
    return null;
  }

  public static TextAnalyzer of(Function<Builder, ObjectBuilder<TextAnalyzer>> fn) {
    return fn.apply(new Builder()).build();
  }

  public TextAnalyzer(Builder builder) {
    this(
        builder.foldAscii,
        builder.keepAscii,
        builder.stopwordPreset);
  }

  public static class Builder implements ObjectBuilder<TextAnalyzer> {
    Boolean foldAscii = true;
    List<String> keepAscii = new ArrayList<>();
    String stopwordPreset;

    public Builder foldAscii(boolean enable) {
      this.foldAscii = enable;
      return this;
    }

    public Builder keepAscii(String... keepAscii) {
      return keepAscii(Arrays.asList(keepAscii));
    }

    public Builder keepAscii(List<String> keepAscii) {
      this.keepAscii = keepAscii;
      return this;
    }

    public Builder stopwordPreset(String stopwordPreset) {
      this.stopwordPreset = stopwordPreset;
      return this;
    }

    @Override
    public TextAnalyzer build() {
      return new TextAnalyzer(this);
    }
  }
}
