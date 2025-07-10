package io.weaviate.client.v1.rbac.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = true)
public class AliasesPermission extends Permission<AliasesPermission> {
  final String alias;

  public AliasesPermission(String alias, Action... actions) {
    super(actions);
    this.alias = alias;
  }

  AliasesPermission(String alias, String action) {
    this(alias, RbacAction.fromString(Action.class, action));
  }

  @AllArgsConstructor
  public enum Action implements RbacAction {
    CREATE("create_aliases"),
    READ("read_aliases"),
    UPDATE("update_aliases"),
    DELETE("delete_aliases");

    @Getter
    private final String value;
  }
}
