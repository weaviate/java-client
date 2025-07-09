package io.weaviate.client.v1.aliases.model;

import com.google.gson.annotations.SerializedName;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@EqualsAndHashCode
public class Alias {
  @SerializedName("class")
  private final String className;
  @SerializedName("alias")
  private final String alias;
}
