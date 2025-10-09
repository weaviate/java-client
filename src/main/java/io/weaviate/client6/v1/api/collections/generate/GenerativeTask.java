package io.weaviate.client6.v1.api.collections.generate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBase;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoGenerative;

public record GenerativeTask(Single single, Grouped grouped) {
  public static GenerativeTask of(Function<Builder, ObjectBuilder<GenerativeTask>> fn) {
    return fn.apply(new Builder()).build();
  }

  public GenerativeTask(Builder builder) {
    this(builder.single, builder.grouped);
  }

  public static class Builder implements ObjectBuilder<GenerativeTask> {
    private Single single;
    private Grouped grouped;

    public Builder singlePrompt(String prompt) {
      this.single = Single.of(prompt);
      return this;
    }

    public Builder singlePrompt(String prompt, Function<Single.Builder, ObjectBuilder<Single>> fn) {
      this.single = Single.of(prompt, fn);
      return this;

    }

    public Builder groupedTask(String prompt) {
      this.grouped = Grouped.of(prompt);
      return this;
    }

    public Builder groupedTask(String prompt, Function<Grouped.Builder, ObjectBuilder<Grouped>> fn) {
      this.grouped = Grouped.of(prompt, fn);
      return this;
    }

    @Override
    public GenerativeTask build() {
      return new GenerativeTask(this);
    }
  }

  void appendTo(WeaviateProtoGenerative.GenerativeSearch.Builder req) {
    if (single != null) {
      single.appendTo(req);
    }
    if (grouped != null) {
      grouped.appendTo(req);
    }
  }

  public record Single(String prompt, boolean debug) {
    public static Single of(String prompt) {
      return of(prompt, ObjectBuilder.identity());
    }

    public static Single of(String prompt, Function<Builder, ObjectBuilder<Single>> fn) {
      return fn.apply(new Builder(prompt)).build();
    }

    public Single(Builder builder) {
      this(builder.prompt, builder.debug);
    }

    public static class Builder implements ObjectBuilder<Single> {
      private final String prompt;
      private boolean debug = false;

      public Builder(String prompt) {
        this.prompt = prompt;
      }

      public Builder debug(boolean enable) {
        this.debug = enable;
        return this;
      }

      @Override
      public Single build() {
        return new Single(this);
      }
    }

    public void appendTo(WeaviateProtoGenerative.GenerativeSearch.Builder req) {
      req.setSingle(
          WeaviateProtoGenerative.GenerativeSearch.Single.newBuilder()
              .setPrompt(prompt)
              .setDebug(debug));
    }
  }

  public record Grouped(String prompt, boolean debug, List<String> properties) {
    public static Grouped of(String prompt) {
      return of(prompt, ObjectBuilder.identity());
    }

    public static Grouped of(String prompt, Function<Builder, ObjectBuilder<Grouped>> fn) {
      return fn.apply(new Builder(prompt)).build();
    }

    public Grouped(Builder builder) {
      this(builder.prompt, builder.debug, builder.properties);
    }

    public static class Builder implements ObjectBuilder<Grouped> {
      private final String prompt;
      private final List<String> properties = new ArrayList<>();
      private boolean debug = false;

      public Builder(String prompt) {
        this.prompt = prompt;
      }

      public Builder properties(String... properties) {
        return properties(Arrays.asList(properties));
      }

      public Builder properties(List<String> properties) {
        this.properties.addAll(properties);
        return this;
      }

      public Builder debug(boolean enable) {
        this.debug = enable;
        return this;
      }

      @Override
      public Grouped build() {
        return new Grouped(this);
      }
    }

    public void appendTo(WeaviateProtoGenerative.GenerativeSearch.Builder req) {
      var grouped = WeaviateProtoGenerative.GenerativeSearch.Grouped.newBuilder()
          .setTask(prompt)
          .setDebug(debug);

      if (properties != null && !properties.isEmpty()) {
        grouped.setProperties(
            WeaviateProtoBase.TextArray.newBuilder()
                .addAllValues(properties));

      }
      req.setGrouped(grouped);
    }
  }
}
