package io.weaviate.client6.v1.api.rbac;

import java.util.Arrays;
import java.util.List;

import com.google.gson.annotations.SerializedName;

import io.weaviate.client6.v1.api.rbac.groups.GroupType;

public record GroupsPermission(
    @SerializedName("group") String groupId,
    @SerializedName("groupType") GroupType groupType,
    @SerializedName("actions") List<Action> actions) implements Permission {

  public GroupsPermission(String groupId, GroupType groupType, Action... actions) {
    this(groupId, groupType, Arrays.asList(actions));
  }

  @Override
  public Permission.Kind _kind() {
    return Permission.Kind.GROUPS;
  }

  @Override
  public Object self() {
    return this;
  }

  public enum Action implements RbacAction<Action> {
    @SerializedName("read_groups")
    READ("read_groups"),
    @SerializedName("assign_and_revoke_groups")
    ASSIGN_AND_REVOKE("assign_and_revoke_groups");

    private final String jsonValue;

    private Action(String jsonValue) {
      this.jsonValue = jsonValue;
    }

    @Override
    public String jsonValue() {
      return jsonValue;
    }
  }
}
