package io.weaviate.client6.v1.api.rbac.groups;

import com.google.gson.annotations.SerializedName;

public enum UserType {
  @SerializedName("db_user")
  DB_USER,
  @SerializedName("db_end_user")
  DB_ENV_USER,
  @SerializedName("oidc")
  OIDC
}
