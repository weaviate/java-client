package io.weaviate.client6.v1.api.rbac;

import java.util.Arrays;
import java.util.List;

import com.google.gson.annotations.SerializedName;

public record McpPermission(
    @SerializedName("actions") List<Action> actions) implements Permission {

  public McpPermission(Action... actions) {
    this(Arrays.asList(actions));
  }

  @Override
  public Permission.Kind _kind() {
    return Permission.Kind.MCP;
  }

  @Override
  public Object self() {
    return this;
  }

  public enum Action implements RbacAction<Action> {
    @SerializedName("create_mcp")
    CREATE("create_mcp"),

    @SerializedName("read_mcp")
    READ("read_mcp"),

    @SerializedName("update_mcp")
    UPDATE("update_mcp");

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
