package io.weaviate.client6.v1.api.tokenize;

import java.util.List;

import com.google.gson.annotations.SerializedName;

import io.weaviate.client6.v1.api.collections.Tokenization;

public record TokenizeResponse(
    @SerializedName("tokenization") Tokenization tokenization,
    @SerializedName("indexed") List<String> indexed,
    @SerializedName("query") List<String> query) {
}
