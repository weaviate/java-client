package io.weaviate.client6.v1.api.alias;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public record ListAliasResponse(@SerializedName("aliases") List<Alias> aliases) {
}
