package io.weaviate.client6.v1.internal;

public interface TaggedUnion<KindT extends Enum<KindT>, SelfT> {
  KindT _kind();

  SelfT _self();

  /** Does the current instance have the kind? */
  default boolean _is(KindT kind) {
    return _kind() == kind;
  }

  /** Convert tagged union instance to one of its variants. */
  default <Value extends TaggedUnion<KindT, SelfT>> Value _as(KindT kind) {
    return TaggedUnion.as(this, kind);
  }

  /** Convert tagged union instance to one of its variants. */
  public static <Union extends TaggedUnion<Tag, ?>, Tag extends Enum<Tag>, Value> Value as(Union union, Tag kind) {
    if (union._is(kind)) {
      @SuppressWarnings("unchecked")
      Value value = (Value) union._self();
      return value;
    }
    throw new IllegalStateException("Cannot convert '%s' variant to '%s'".formatted(union._kind(), kind));
  }
}
