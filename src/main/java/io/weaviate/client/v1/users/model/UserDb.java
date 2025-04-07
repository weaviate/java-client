package io.weaviate.client.v1.users.model;

import java.util.List;

import com.google.gson.annotations.SerializedName;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class UserDb {
  @SerializedName("roles")
  List<String> roleNames;

  @SerializedName("userId")
  String userId;

  @SerializedName("dbUserType")
  String userType;

  @SerializedName("active")
  boolean active;
}
