package io.weaviate.client6.v1.api.collections;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import com.google.gson.annotations.SerializedName;

import io.weaviate.client6.v1.internal.ObjectBuilder;

public record InvertedIndex(
    /** The frequency of cleanup operations in the HNSW vector index. */
    @SerializedName("cleanupIntervalSeconds") Integer cleanupIntervalSeconds,
    /** Parameters for BM25 ranking algorithm. */
    @SerializedName("bm25") Bm25 bm25,
    /** Common words which should be ignored in queries. */
    @SerializedName("stopwords") Stopwords stopwords,
    /**
     * If true, indexes object creation and update timestamps,
     * enabling filtering by creationTimeUnix and lastUpdateTimeUnix.
     */
    @SerializedName("indexTimestamps") Boolean indexTimestamps,
    /**
     * If true, indexes the null/non-null state of each property,
     * enabling filtering for null values.
     */
    @SerializedName("indexNullState") Boolean indexNulls,
    /**
     * If true, indexes the length of each property,
     * enabling filtering by property length.
     */
    @SerializedName("indexPropertyLength") Boolean indexPropertyLength,
    /** If true, BlockMaxWAND optimization is used. */
    @SerializedName("usingBlockMaxWAND") Boolean useBlockMaxWAND) {

  public static InvertedIndex of(Function<Builder, ObjectBuilder<InvertedIndex>> fn) {
    return fn.apply(new Builder()).build();
  }

  public record Bm25(
      /** Free parameter for the BM25 ranking function. */
      @SerializedName("b") Float b,
      /** Free parameter for the BM25 ranking function. */
      @SerializedName("k1") Float k1) {

    public static Bm25 of(Function<Builder, ObjectBuilder<Bm25>> fn) {
      return fn.apply(new Builder()).build();
    }

    public Bm25(Builder builder) {
      this(builder.b, builder.k1);
    }

    public static class Builder implements ObjectBuilder<Bm25> {
      private Float b;
      private Float k1;

      /** Set free parameter {@code b} for the BM25 ranking function. */
      public Builder b(float b) {
        this.b = b;
        return this;
      }

      /** Set free parameter {@code k1} for the BM25 ranking function. */
      public Builder k1(float k1) {
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
      /** Selected preset. */
      @SerializedName("preset") String preset,
      /** Custom words added to the selected preset. */
      @SerializedName("additions") List<String> additions,
      /** Words removed from the selected preset. */
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

      /** Select a preset to use for a particular language. */
      public Builder preset(String preset) {
        this.preset = preset;
        return this;
      }

      /** Add words to the selected preset. */
      public Builder add(String... additions) {
        return add(Arrays.asList(additions));
      }

      /** Add words to the selected preset. */
      public Builder add(List<String> additions) {
        this.additions.addAll(additions);
        return this;
      }

      /** Remove words from the selected preset. */
      public Builder remove(String... removals) {
        return remove(Arrays.asList(removals));
      }

      /** Remove words from the selected preset. */
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

    /** Set the frequency of cleanup operations in the HNSW vector index. */
    public Builder cleanupIntervalSeconds(int cleanupIntervalSeconds) {
      this.cleanupIntervalSeconds = cleanupIntervalSeconds;
      return this;
    }

    /** Set {@code b} and {@code k1} parameters for BM25 ranking algorithm. */
    public Builder bm25(Function<Bm25.Builder, ObjectBuilder<Bm25>> fn) {
      this.bm25 = Bm25.of(fn);
      return this;
    }

    /** Select and configure a stopwords preset. */
    public Builder stopwords(Function<Stopwords.Builder, ObjectBuilder<Stopwords>> fn) {
      this.stopwords = Stopwords.of(fn);
      return this;
    }

    /**
     * Enable / disable creating an index for creation / update timestamps.
     *
     * @see InvertedIndex#indexTimestamps
     */
    public Builder indexTimestamps(boolean indexTimestamps) {
      this.indexTimestamps = indexTimestamps;
      return this;
    }

    /**
     * Enable / disable creating an index for null property values.
     *
     * @see InvertedIndex#indexNulls
     */
    public Builder indexNulls(boolean indexNulls) {
      this.indexNulls = indexNulls;
      return this;
    }

    /**
     * Enable / disable creating an index for property lengths.
     *
     * @see InvertedIndex#indexPropertyLength
     */
    public Builder indexPropertyLength(boolean indexPropertyLength) {
      this.indexPropertyLength = indexPropertyLength;
      return this;
    }

    /**
     * If true, indexes object creation and update timestamps,
     * enabling filtering by creationTimeUnix and lastUpdateTimeUnix.
     */
    public Builder useBlockMaxWAND(boolean useBlockMaxWAND) {
      this.useBlockMaxWAND = useBlockMaxWAND;
      return this;
    }

    @Override
    public InvertedIndex build() {
      return new InvertedIndex(this);
    }
  }
}
