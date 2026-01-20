package io.weaviate.client6.v1.api.rbac;

import io.weaviate.client6.v1.internal.json.JsonEnum;

public interface RbacAction<E extends Enum<E>> extends JsonEnum<E> {

  /**
   * Returns true if the action is hard deprecated.
   *
   * <p>
   * Override default return for a deprecated enum value like so:
   *
   * <pre>{@code
   * OLD_ACTION("old_action") {
   *  {@literal @Override}
   *  public boolean isDeprecated() { return true; }
   * };
   * }</pre>
   */
  default boolean isDeprecated() {
    return false;
  }

  static <E extends RbacAction<? extends Enum<?>>> E fromString(Class<E> enumClass, String value)
      throws IllegalArgumentException {
    for (E action : enumClass.getEnumConstants()) {
      if (action.jsonValue().equals(value)) {
        return action;
      }
    }
    throw new IllegalArgumentException("No enum constant for value: " + value);
  }

  static <E extends RbacAction<? extends Enum<?>>> boolean isValid(Class<E> enumClass, String value) {
    for (var action : enumClass.getEnumConstants()) {
      if (action.jsonValue().equals(value)) {
        return true;
      }
    }
    return false;
  }
}
