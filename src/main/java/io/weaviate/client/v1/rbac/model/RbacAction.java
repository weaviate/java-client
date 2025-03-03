package io.weaviate.client.v1.rbac.model;

/**
 * RbacAction is a utility interface to allow retrieving concrete enum values
 * from their underlying string representations.
 *
 * <p>
 * Usage:
 *
 * <pre>{@code
 * enum MyAction implements RbacAction {
 *   FOO("do_foo"),
 *   BAR("do_bar");
 *
 *   {@literal @Getter}
 *   private String value;
 * }
 * }</pre>
 *
 * <p>
 * Then {@code MyAction.FOO} can be retrieved from "do_foo" using
 * {@link #fromString}.
 */
interface RbacAction {
  String getValue();

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
