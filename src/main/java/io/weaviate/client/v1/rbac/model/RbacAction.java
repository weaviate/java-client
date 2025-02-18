package io.weaviate.client.v1.rbac.model;

/**
 * RbacAction is a utility interface to allow retrieving concrete enum values
 * from their underlying string representations.
 *
 * <p>
 * Usage:
 *
 * <pre>
 * enum MyAction implements RbacAction {
 *   FOO("do_foo"),
 *   BAR("do_bar");
 *
 *   @Getter
 *   private String value;
 * }
 * </pre>
 *
 * <p>
 * Then {@code MyAction.FOO} can be retrieved from "do_foo" using
 * {@link #fromString}.
 */
interface RbacAction {
  String getValue();

  default boolean isDeprecated() {
    return false;
  }

  static <E extends Enum<E> & RbacAction> E fromString(Class<E> enumClass, String value)
      throws IllegalArgumentException {
    for (E action : enumClass.getEnumConstants()) {
      if (action.getValue().equals(value)) {
        return action;
      }
    }
    throw new IllegalArgumentException("No enum constant for value: " + value);
  }

  static <A extends RbacAction> boolean isValid(Class<A> enumClass, String value) {
    for (RbacAction action : enumClass.getEnumConstants()) {
      if (action.getValue().equals(value)) {
        return true;
      }
    }
    return false;
  }
}
