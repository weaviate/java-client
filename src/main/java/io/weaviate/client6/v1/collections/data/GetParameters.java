package io.weaviate.client6.v1.collections.data;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class GetParameters implements QueryParameters {
  private enum Include {
    VECTOR, CLASSIFICATION, INTERPRETATION;

    String toLowerCase() {
      return this.name().toLowerCase();
    }
  }

  private Set<Include> include = new LinkedHashSet<>(); // Preserves insertion order, helps testing
  private ConsistencyLevel consistency;
  private String nodeName;
  private String tenant;

  GetParameters(Consumer<GetParameters> options) {
    options.accept(this);
  }

  public GetParameters withVector() {
    include.add(Include.VECTOR);
    return this;
  }

  public GetParameters withClassification() {
    include.add(Include.CLASSIFICATION);
    return this;
  }

  public GetParameters withInterpretation() {
    include.add(Include.INTERPRETATION);
    return this;
  }

  public GetParameters consistencyLevel(ConsistencyLevel consistency) {
    this.consistency = consistency;
    return this;
  }

  public GetParameters nodeName(String name) {
    this.nodeName = name;
    return this;
  }

  public GetParameters tenant(String name) {
    this.tenant = name;
    return this;
  }

  @Override
  public String encode() {
    var sb = new StringBuilder();

    if (!include.isEmpty()) {
      List<String> includeString = include.stream().map(Include::toLowerCase).toList();
      QueryParameters.addRaw(sb, "include", String.join(",", includeString));
    }

    if (consistency != null) {
      QueryParameters.add(sb, "consistency_level", consistency.name());
    }
    if (nodeName != null) {
      QueryParameters.add(sb, "node_name", nodeName);
    }
    if (tenant != null) {
      QueryParameters.add(sb, "tenant", tenant);
    }
    return sb.toString();
  }
}
