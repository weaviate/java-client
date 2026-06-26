package io.weaviate.client6.v1.api.collections.query;

import static java.util.Objects.requireNonNull;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBase;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoSearchGet;

public class Boost {
  private final List<Condition> conditions;
  private final Float weight;
  private final Integer depth;

  // Package-private for testing.
  List<Condition> conditions() {
    return conditions;
  }

  // Package-private for testing.
  Float weight() {
    return weight;
  }

  // Package-private for testing.
  Integer depth() {
    return depth;
  }

  private Boost(Condition condition, Float weight, Integer depth) {
    this(List.of(requireNonNull(condition, "condition")), weight, depth);
  }

  private Boost(List<Condition> conditions, Float weight, Integer depth) {
    this.conditions = List.copyOf(requireNonNull(conditions, "conditions"));
    this.weight = weight;
    this.depth = depth;
  }

  public static Boost timeDecay(String property, String scale) {
    return timeDecay(property, scale, ObjectBuilder.identity());
  }

  public static Boost timeDecay(String property, String scale,
      Function<TimeDecay.Builder, ObjectBuilder<Boost>> fn) {
    return fn.apply(new TimeDecay.Builder(property, scale)).build();
  }

  public static Boost numericDecay(String property, float origin, float scale) {
    return numericDecay(property, origin, scale, ObjectBuilder.identity());
  }

  public static Boost numericDecay(String property, float origin, float scale,
      Function<NumericDecay.Builder, ObjectBuilder<Boost>> fn) {
    return fn.apply(new NumericDecay.Builder(property, origin, scale)).build();
  }

  public static Boost numericProperty(String property) {
    return numericProperty(property, ObjectBuilder.identity());
  }

  public static Boost numericProperty(String property,
      Function<PropertyValue.Builder, ObjectBuilder<Boost>> fn) {
    return fn.apply(new PropertyValue.Builder(property)).build();
  }

  public static Boost filter(Filter filter) {
    return filter(filter, ObjectBuilder.identity());
  }

  public static Boost filter(Filter filter,
      Function<FilterBuilder, ObjectBuilder<Boost>> fn) {
    return fn.apply(new FilterBuilder(filter)).build();
  }

  public static Boost blend(Float weight, Integer depth, Boost... boosts) {
    var conditions = Arrays.stream(boosts)
        .<Condition>mapMulti((b, stream) -> {
          if (b.depth != null) {
            throw new IllegalArgumentException("A boost passed to Boost.blend() cannot set it's own depth.");
          }
          b.conditions.forEach(cond -> {
            if (cond.weight == null && b.weight != null) {
              cond = cond.withWeight(b.weight);
            }
            stream.accept(cond);
          });
        }).toList();
    return new Boost(conditions, weight, depth);
  }

  public static abstract class Builder<SelfT extends Builder<SelfT>> implements ObjectBuilder<Boost> {
    protected Float weight;
    protected Integer depth;

    @SuppressWarnings("unchecked")
    public SelfT weight(float weight) {
      this.weight = weight;
      return (SelfT) this;
    }

    @SuppressWarnings("unchecked")
    public SelfT depth(int depth) {
      this.depth = depth;
      return (SelfT) this;
    }
  }

  public static class Condition {
    private final Object func;
    private final Float weight;

    private Condition(Object func, Float weight) {
      this.func = requireNonNull(func, "func");
      this.weight = weight;
    }

    private Condition withWeight(float weight) {
      return new Condition(func, weight);
    }

    // Package-private for testing.
    Float weight() {
      return weight;
    }
  }

  public static class FilterBuilder extends Boost.Builder<FilterBuilder> {
    private final Filter filter;

    public FilterBuilder(Filter filter) {
      this.filter = filter;
    }

    @Override
    public Boost build() {
      return new Boost(new Condition(filter, weight), weight, depth);
    }
  }

  public static record TimeDecay(
      String property,
      String origin,
      String scale,
      String offset,
      Curve curve,
      Float decay) {

    public TimeDecay(Builder builder) {
      this(
          builder.property,
          builder.origin,
          builder.scale,
          builder.offset,
          builder.curve,
          builder.decay);
    }

    public static class Builder extends Boost.Builder<Builder> {
      private final String property;
      private final String scale;

      private String origin;
      private String offset;
      private Curve curve;
      private Float decay;

      public Builder(String property, String scale) {
        this.property = property;
        this.scale = scale;
      }

      public Builder origin(String origin) {
        this.origin = origin;
        return this;
      }

      public Builder offset(String offset) {
        this.offset = offset;
        return this;
      }

      public Builder curve(Curve curve) {
        this.curve = curve;
        return this;
      }

      public Builder decay(float decay) {
        this.decay = decay;
        return this;
      }

      @Override
      public Boost build() {
        return new Boost(new Condition(new TimeDecay(this), weight), weight, depth);
      }
    }
  }

  public static record NumericDecay(
      String property,
      Float origin,
      Float scale,
      Float offset,
      Curve curve,
      Float decay) {

    public NumericDecay(Builder builder) {
      this(
          builder.property,
          builder.origin,
          builder.scale,
          builder.offset,
          builder.curve,
          builder.decay);
    }

    public static class Builder extends Boost.Builder<Builder> {
      private final String property;
      private final float origin;
      private final float scale;

      private Float offset;
      private Curve curve;
      private Float decay;

      public Builder(String property, float origin, float scale) {
        this.property = property;
        this.origin = origin;
        this.scale = scale;
      }

      public Builder offset(float offset) {
        this.offset = offset;
        return this;
      }

      public Builder curve(Curve curve) {
        this.curve = curve;
        return this;
      }

      public Builder decay(float decay) {
        this.decay = decay;
        return this;
      }

      @Override
      public Boost build() {
        return new Boost(new Condition(new NumericDecay(this), weight), weight, depth);
      }
    }
  }

  public enum Curve {
    EXPONENTIAL(WeaviateProtoSearchGet.Boost.DecayCurve.DECAY_CURVE_EXPONENTIAL),
    GAUSSIAN(WeaviateProtoSearchGet.Boost.DecayCurve.DECAY_CURVE_GAUSS),
    LINEAR(WeaviateProtoSearchGet.Boost.DecayCurve.DECAY_CURVE_LINEAR);

    private final WeaviateProtoSearchGet.Boost.DecayCurve protoValue;

    private Curve(WeaviateProtoSearchGet.Boost.DecayCurve protoValue) {
      this.protoValue = protoValue;
    }
  }

  public static record PropertyValue(
      String property,
      Modifier modifier) {

    public PropertyValue(Builder builder) {
      this(
          builder.property,
          builder.modifier);
    }

    public static class Builder extends Boost.Builder<Builder> {
      private final String property;

      private Modifier modifier;

      public Builder(String property) {
        this.property = property;
      }

      public Builder modifier(Modifier modifier) {
        this.modifier = modifier;
        return this;
      }

      @Override
      public Boost build() {
        return new Boost(new Condition(new PropertyValue(this), weight), weight, depth);
      }
    }
  }

  public enum Modifier {
    LOG1P(WeaviateProtoSearchGet.Boost.PropertyValueModifier.PROPERTY_VALUE_MODIFIER_LOG1P),
    SQRT(WeaviateProtoSearchGet.Boost.PropertyValueModifier.PROPERTY_VALUE_MODIFIER_SQRT);

    private final WeaviateProtoSearchGet.Boost.PropertyValueModifier protoValue;

    private Modifier(WeaviateProtoSearchGet.Boost.PropertyValueModifier protoValue) {
      this.protoValue = protoValue;
    }
  }

  public WeaviateProtoSearchGet.Boost.Builder toProto() {
    var boost = WeaviateProtoSearchGet.Boost.newBuilder();
    if (weight != null) {
      boost.setWeight(weight);
    }
    if (depth != null) {
      boost.setDepth(depth);
    }

    for (var cond : conditions) {
      var condBuilder = WeaviateProtoSearchGet.Boost.Condition.newBuilder();
      if (cond.weight != null) {
        condBuilder.setWeight(cond.weight);
      }
      if (cond.func instanceof Filter f) {
        var filterBuilder = WeaviateProtoBase.Filters.newBuilder();
        f.appendTo(filterBuilder);
        condBuilder.setFilter(filterBuilder);
      } else if (cond.func instanceof TimeDecay time) {
        var timeBuilder = WeaviateProtoSearchGet.Boost.TimeDecayFunction.newBuilder();
        if (time.property != null) {
          timeBuilder.setProperty(time.property);
        }
        if (time.origin != null) {
          timeBuilder.setOrigin(time.origin);
        }
        if (time.scale != null) {
          timeBuilder.setScale(time.scale);
        }
        if (time.offset != null) {
          timeBuilder.setOffset(time.offset);
        }
        if (time.curve != null) {
          timeBuilder.setCurve(time.curve.protoValue);
        }
        if (time.decay != null) {
          timeBuilder.setDecayValue(time.decay);
        }
        condBuilder.setTimeDecay(timeBuilder);
      } else if (cond.func instanceof NumericDecay num) {
        var numBuilder = WeaviateProtoSearchGet.Boost.NumericDecayFunction.newBuilder();
        if (num.property != null) {
          numBuilder.setProperty(num.property);
        }
        if (num.origin != null) {
          numBuilder.setOrigin(num.origin);
        }
        if (num.scale != null) {
          numBuilder.setScale(num.scale);
        }
        if (num.offset != null) {
          numBuilder.setOffset(num.offset);
        }
        if (num.curve != null) {
          numBuilder.setCurve(num.curve.protoValue);
        }
        if (num.decay != null) {
          numBuilder.setDecayValue(num.decay);
        }
        condBuilder.setNumericDecay(numBuilder);
      } else if (cond.func instanceof PropertyValue prop) {
        var propBuilder = WeaviateProtoSearchGet.Boost.PropertyValueFunction.newBuilder();
        if (prop.property != null) {
          propBuilder.setProperty(prop.property);
        }
        if (prop.modifier != null) {
          propBuilder.setModifier(prop.modifier.protoValue);
        }
        condBuilder.setPropertyValue(propBuilder);
      }
      boost.addConditions(condBuilder);
    }
    return boost;
  }
}
