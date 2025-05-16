package io.weaviate.client6.v1.api.collections.aggregate;

import java.util.List;

public record AggregateResponseGrouped(List<AggregateResponseGroup<?>> groups) {

}
