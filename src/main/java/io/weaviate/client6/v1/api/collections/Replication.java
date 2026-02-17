package io.weaviate.client6.v1.api.collections;

import java.util.function.Function;

import com.google.gson.annotations.SerializedName;

import io.weaviate.client6.v1.internal.ObjectBuilder;

public record Replication(
    @SerializedName("factor") Integer replicationFactor,
    @SerializedName("asyncEnabled") Boolean asyncEnabled,
    @SerializedName("deletionStrategy") DeletionStrategy deletionStrategy,
    @SerializedName("asyncConfig") AsyncReplicationConfig asyncReplicationConfig) {

  public static enum DeletionStrategy {
    @SerializedName("NoAutomatedResolution")
    NO_AUTOMATED_RESOLUTION,
    @SerializedName("DeleteOnConflict")
    DELETE_ON_CONFLICT,
    @SerializedName("TimeBasedResolution")
    TIME_BASED_RESOLUTION;
  }

  public static Replication of(Function<Builder, ObjectBuilder<Replication>> fn) {
    return fn.apply(new Builder()).build();
  }

  public Replication(Builder builder) {
    this(
        builder.replicationFactor,
        builder.asyncEnabled,
        builder.deletionStrategy,
        builder.asyncReplicationConfig);
  }

  public static record AsyncReplicationConfig(
      @SerializedName("hashtreeHeight") Integer hashTreeHeight,
      @SerializedName("maxWorkers") Integer replicationConcurrency,
      @SerializedName("frequency") Integer replicationFrequencyMillis,
      @SerializedName("frequencyWhilePropagating") Integer replicationFrequencyMillisWhilePropagating,
      @SerializedName("aliveNodesCheckingFrequency") Integer nodePingFrequencyMillis,
      @SerializedName("loggingFrequency") Integer loggingFrequencyMillis,
      @SerializedName("diffBatchSize") Integer diffBatchSize,
      @SerializedName("diffPerNodeTimeout") Integer diffPerNodeTimeoutSeconds,
      @SerializedName("prePropagationTimeout") Integer prePropagationTimeoutSeconds,
      @SerializedName("propagationTimeout") Integer propagationTimeoutSeconds,
      @SerializedName("propagationDelay") Integer propagationDelayMillis,
      @SerializedName("propagationLimit") Integer propagationLimit,
      @SerializedName("propagationConcurrency") Integer propagationConcurrency,
      @SerializedName("propagationBatchSize") Integer propagationBatchSize) {

    public AsyncReplicationConfig(Builder builder) {
      this(
          builder.hashTreeHeight,
          builder.replicationConcurrency,
          builder.replicationFrequencyMillis,
          builder.replicationFrequencyMillisWhilePropagating,
          builder.nodePingFrequencyMillis,
          builder.loggingFrequencyMillis,
          builder.diffBatchSize,
          builder.diffPerNodeTimeoutSeconds,
          builder.prePropagationTimeoutSeconds,
          builder.propagationTimeoutSeconds,
          builder.propagationDelayMillis,
          builder.propagationLimit,
          builder.propagationConcurrency,
          builder.propagationBatchSize);
    }

    public static AsyncReplicationConfig of(Function<Builder, ObjectBuilder<AsyncReplicationConfig>> fn) {
      return fn.apply(new Builder()).build();
    }

    public static class Builder implements ObjectBuilder<AsyncReplicationConfig> {
      private Integer hashTreeHeight;
      private Integer replicationConcurrency;
      private Integer replicationFrequencyMillis;
      private Integer replicationFrequencyMillisWhilePropagating;
      private Integer nodePingFrequencyMillis;
      private Integer loggingFrequencyMillis;
      private Integer diffBatchSize;
      private Integer diffPerNodeTimeoutSeconds;
      private Integer prePropagationTimeoutSeconds;
      private Integer propagationTimeoutSeconds;
      private Integer propagationDelayMillis;
      private Integer propagationLimit;
      private Integer propagationConcurrency;
      private Integer propagationBatchSize;

      /** Height of the hashtree used for diffing. */
      public Builder hashTreeHeight(int hashTreeHeight) {
        this.hashTreeHeight = hashTreeHeight;
        return this;
      }

      /** Maximum number of async replication workers. */
      public Builder replicationConcurrency(int replicationConcurrency) {
        this.replicationConcurrency = replicationConcurrency;
        return this;
      }

      /**
       * Base frequency in milliseconds at which async replication
       * runs diff calculations.
       */
      public Builder replicationFrequencyMillis(int replicationFrequencyMillis) {
        this.replicationFrequencyMillis = replicationFrequencyMillis;
        return this;
      }

      /**
       * Frequency in milliseconds at which async replication runs
       * while propagation is active.
       */
      public Builder replicationFrequencyMillisWhilePropagating(int replicationFrequencyMillisWhilePropagating) {
        this.replicationFrequencyMillisWhilePropagating = replicationFrequencyMillisWhilePropagating;
        return this;
      }

      /** Interval in milliseconds at which liveness of target nodes is checked." */
      public Builder nodePingFrequencyMillis(int nodePingFrequencyMillis) {
        this.nodePingFrequencyMillis = nodePingFrequencyMillis;
        return this;
      }

      /** Interval in seconds at which async replication logs its status. */
      public Builder loggingFrequencyMillis(int loggingFrequencyMillis) {
        this.loggingFrequencyMillis = loggingFrequencyMillis;
        return this;
      }

      /** Maximum number of object keys included in a single diff batch. */
      public Builder diffBatchSize(int diffBatchSize) {
        this.diffBatchSize = diffBatchSize;
        return this;
      }

      /** Timeout in seconds for computing a diff against a single node. */
      public Builder diffPerNodeTimeoutSeconds(int diffPerNodeTimeoutSeconds) {
        this.diffPerNodeTimeoutSeconds = diffPerNodeTimeoutSeconds;
        return this;
      }

      /** Overall timeout in seconds for the pre-propagation phase. */
      public Builder prePropagationTimeoutSeconds(int prePropagationTimeoutSeconds) {
        this.prePropagationTimeoutSeconds = prePropagationTimeoutSeconds;
        return this;
      }

      /** Timeout in seconds for propagating batch of changes to a node. */
      public Builder propagationTimeoutSeconds(int propagationTimeoutSeconds) {
        this.propagationTimeoutSeconds = propagationTimeoutSeconds;
        return this;
      }

      /**
       * Delay in milliseconds before newly added or updated objects are propagated.
       */
      public Builder propagationDelayMillis(int propagationDelayMillis) {
        this.propagationDelayMillis = propagationDelayMillis;
        return this;
      }

      /** Maximum number of objects to propagate in a single async replication run. */
      public Builder propagationLimit(int propagationLimit) {
        this.propagationLimit = propagationLimit;
        return this;
      }

      /** Maximum number of concurrent propagation workers. */
      public Builder propagationConcurrency(int propagationConcurrency) {
        this.propagationConcurrency = propagationConcurrency;
        return this;
      }

      /** Maximum number of objects to propagate in a single async replication run. */
      public Builder propagationBatchSize(int propagationBatchSize) {
        this.propagationBatchSize = propagationBatchSize;
        return this;
      }

      @Override
      public AsyncReplicationConfig build() {
        return new AsyncReplicationConfig(this);
      }
    }
  }

  public static class Builder implements ObjectBuilder<Replication> {
    private Integer replicationFactor;
    private Boolean asyncEnabled;
    private DeletionStrategy deletionStrategy;
    private AsyncReplicationConfig asyncReplicationConfig;

    /** Set desired replication factor for this collection. */
    public Builder replicationFactor(int replicationFactor) {
      this.replicationFactor = replicationFactor;
      return this;
    }

    /** Enable / disable async replication. */
    public Builder asyncEnabled(boolean asyncEnabled) {
      this.asyncEnabled = asyncEnabled;
      return this;
    }

    /**
     * Select the deletion strategy for resolving conflicts
     * during async replication.
     */
    public Builder deletionStrategy(DeletionStrategy deletionStrategy) {
      this.deletionStrategy = deletionStrategy;
      return this;
    }

    /** Configuration parameters for asynchronous replication. */
    public Builder asyncReplication(AsyncReplicationConfig asyncReplicationConfig) {
      this.asyncReplicationConfig = asyncReplicationConfig;
      return this;
    }

    @Override
    public Replication build() {
      return new Replication(this);
    }
  }
}
