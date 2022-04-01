package technology.semi.weaviate.client.v1.misc.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class StopwordConfig {
    String preset;
    String[] additions;
    String[] removals;
}
