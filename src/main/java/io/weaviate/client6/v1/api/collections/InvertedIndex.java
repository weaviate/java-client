package io.weaviate.client6.v1.api.collections;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import com.google.gson.annotations.SerializedName;

import io.weaviate.client6.v1.internal.ObjectBuilder;

public record InvertedIndex(
    @SerializedName("cleanupIntervalSeconds") Integer cleanupIntervalSeconds,
    @SerializedName("bm25") Bm25 bm25,
    @SerializedName("stopwords") Stopwords stopwords,
    @SerializedName("indexTimestamps") Boolean indexTimestamps,
    @SerializedName("indexNullState") Boolean indexNulls,
    @SerializedName("indexPropertyLength") Boolean indexPropertyLength,
    @SerializedName("usingBlockMaxWAND") Boolean useBlockMaxWAND) {

  public static InvertedIndex of(Function<Builder, ObjectBuilder<InvertedIndex>> fn) {
    return fn.apply(new Builder()).build();
  }

  public record Bm25(
      @SerializedName("b") Integer b,
      @SerializedName("k1") Integer k1) {

    public static Bm25 of(Function<Builder, ObjectBuilder<Bm25>> fn) {
      return fn.apply(new Builder()).build();
    }

    public Bm25(Builder builder) {
      this(builder.b, builder.k1);
    }

    public static class Builder implements ObjectBuilder<Bm25> {
      private Integer b;
      private Integer k1;

      public Builder b(int b) {
        this.b = b;
        return this;
      }

      public Builder k1(int k1) {
        this.k1 = k1;
        return this;
      }

      @Override
      public Bm25 build() {
        return new Bm25(this);
      }
    }
  }

  public record Stopwords(
      @SerializedName("preset") String preset,
      @SerializedName("additions") List<String> additions,
      @SerializedName("removals") List<String> removals) {

    public static Stopwords of(Function<Builder, ObjectBuilder<Stopwords>> fn) {
      return fn.apply(new Builder()).build();
    }

    public Stopwords(Builder builder) {
      this(builder.preset, builder.additions, builder.removals);
    }

    public static class Builder implements ObjectBuilder<Stopwords> {
      private String preset;
      private List<String> additions;
      private List<String> removals;

      public Builder preset(String preset) {
        this.preset = preset;
        return this;
      }

      public Builder add(String... additions) {
        return add(Arrays.asList(additions));
      }

      public Builder add(List<String> additions) {
        this.additions.addAll(additions);
        return this;
      }

      public Builder remove(String... removals) {
        return remove(Arrays.asList(removals));
      }

      public Builder remove(List<String> removals) {
        this.removals.addAll(removals);
        return this;
      }

      @Override
      public Stopwords build() {
        return new Stopwords(this);
      }
    }
  }

  public InvertedIndex(Builder builder) {
    this(
        builder.cleanupIntervalSeconds,
        builder.bm25,
        builder.stopwords,
        builder.indexTimestamps,
        builder.indexNulls,
        builder.indexPropertyLength,
        builder.useBlockMaxWAND);
  }

  public static class Builder implements ObjectBuilder<InvertedIndex> {
    private Integer cleanupIntervalSeconds;
    private Bm25 bm25;
    private Stopwords stopwords;
    private Boolean indexTimestamps;
    private Boolean indexNulls;
    private Boolean indexPropertyLength;
    private Boolean useBlockMaxWAND;

    public Builder cleanupIntervalSeconds(int cleanupIntervalSeconds) {
      this.cleanupIntervalSeconds = cleanupIntervalSeconds;
      return this;
    }

    public Builder bm25(Function<Bm25.Builder, ObjectBuilder<Bm25>> fn) {
      this.bm25 = Bm25.of(fn);
      return this;
    }

    public Builder stopwords(Function<Stopwords.Builder, ObjectBuilder<Stopwords>> fn) {
      this.stopwords = Stopwords.of(fn);
      return this;
    }

    public Builder indexTimestamps(Boolean indexTimestamps) {
      this.indexTimestamps = indexTimestamps;
      return this;
    }

    public Builder indexNulls(Boolean indexNulls) {
      this.indexNulls = indexNulls;
      return this;
    }

    public Builder indexPropertyLength(Boolean indexPropertyLength) {
      this.indexPropertyLength = indexPropertyLength;
      return this;
    }

    public Builder useBlockMaxWAND(Boolean useBlockMaxWAND) {
      this.useBlockMaxWAND = useBlockMaxWAND;
      return this;
    }

    @Override
    public InvertedIndex build() {
      return new InvertedIndex(this);
    }
  }
}
