package io.weaviate.client.v1.rbac.model;

import com.google.gson.annotations.SerializedName;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
public class UserAssignment {
  @SerializedName("userId")
  String userId;

  @SerializedName("userType")
  String userType;
}
