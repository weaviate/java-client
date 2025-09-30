package io.weaviate.client6.v1.api.rbac.users;

import com.google.gson.annotations.SerializedName;

public enum UserType {
  @SerializedName("db_user")
  DB_USER,
  @SerializedName("db_env_user")
  DB_ENV_USER,
  @SerializedName("oidc")
  OIDC;
}
