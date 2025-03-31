package io.weaviate.client6.v1.collections.aggregate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public record TextMetric(String property, List<_Function> functions, boolean occurrenceCount,
    Integer atLeast)
    implements Metric {

  public record Values(Long count, List<TopOccurrence> topOccurrences) implements Metric.Values {
  }

  static TextMetric with(String property, Consumer<Builder> options) {
    var opt = new Builder(options);
    return new TextMetric(property,
        new ArrayList<>(opt.functions),
        opt.occurrenceCount, opt.atLeast);
  }

  public enum _Function {
    COUNT, TYPE, TOP_OCCURRENCES;
  }

  public static class Builder {
    private final Set<_Function> functions = new HashSet<>();
    private boolean occurrenceCount = false;
    private Integer atLeast;

    public Builder count() {
      functions.add(_Function.COUNT);
      return this;
    }

    public Builder type() {
      functions.add(_Function.TYPE);
      return this;
    }

    public Builder topOccurences() {
      functions.add(_Function.TOP_OCCURRENCES);
      return this;
    }

    public Builder topOccurences(int atLeast) {
      topOccurences();
      this.atLeast = atLeast;
      return this;
    }

    public Builder includeTopOccurencesCount() {
      topOccurences();
      this.occurrenceCount = true;
      return this;
    }

    Builder(Consumer<Builder> options) {
      options.accept(this);
    }
  }
}
