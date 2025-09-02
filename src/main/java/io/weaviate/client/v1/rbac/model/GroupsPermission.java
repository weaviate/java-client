package io.weaviate.client.v1.rbac.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = true)
public class GroupsPermission extends Permission<GroupsPermission> {
  final String groupId;
  final String groupType;

  public GroupsPermission(String groupId, String groupType, Action... actions) {
    super(actions);
    this.groupId = groupId;
    this.groupType = groupType;
  }

  GroupsPermission(String groupId, String groupType, String action) {
    this(groupId, groupType, RbacAction.fromString(Action.class, action));
  }

  @AllArgsConstructor
  public enum Action implements RbacAction {
    READ("read_groups"),
    ASSIGN_AND_REVOKE("assign_and_revoke_groups");

    @Getter
    private final String value;
  }
}
