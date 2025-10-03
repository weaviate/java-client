package io.weaviate.client6.v1.api.rbac.users;

import com.google.gson.annotations.SerializedName;

public enum UserType {
  @SerializedName(value = "db", alternate = "db_user")
  DB_USER("db"),
  @SerializedName(value = "db", alternate = "db_env_user")
  DB_ENV_USER("db"),
  @SerializedName("oidc")
  OIDC("oidc");

  private final String jsonValue;

  private UserType(String jsonValue) {
    this.jsonValue = jsonValue;
  }

  public String jsonValue() {
    return jsonValue;
  }
}
