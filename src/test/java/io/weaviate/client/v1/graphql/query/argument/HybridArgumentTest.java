package io.weaviate.client.v1.graphql.query.argument;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashMap;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class HybridArgumentTest {
        static Stream<Arguments> testCases() {
                return Stream.of(
                                Arguments.of(
                                                "simple query",
                                                HybridArgument.builder()
                                                                .query("I'm a simple string")
                                                                .build(),
                                                "hybrid:{query:\"I'm a simple string\"}"),
                                Arguments.of(
                                                "vector and alpha",
                                                HybridArgument.builder()
                                                                .query("I'm a simple string")
                                                                .vector(new Float[] { .1f, .2f, .3f })
                                                                .alpha(.567f)
                                                                .build(),
                                                "hybrid:{query:\"I'm a simple string\" vector:[0.1,0.2,0.3] alpha:0.567}"),
                                Arguments.of(
                                                "vector and target vectors",
                                                HybridArgument.builder()
                                                                .query("I'm a simple string")
                                                                .vector(new Float[] { .1f, .2f, .3f })
                                                                .targetVectors(new String[] { "vector1" })
                                                                .build(),
                                                "hybrid:{query:\"I'm a simple string\" vector:[0.1,0.2,0.3] targetVectors:[\"vector1\"]}"),
                                Arguments.of(
                                                "with escaped characters",
                                                HybridArgument.builder()
                                                                .query("\"I'm a complex string\" says the {'`:string:`'}")
                                                                .build(),
                                                "hybrid:{query:\"\\\"I'm a complex string\\\" says the {'`:string:`'}\"}"),
                                Arguments.of(
                                                "fusion type ranked",
                                                HybridArgument.builder()
                                                                .query("I'm a simple string")
                                                                .fusionType(FusionType.RANKED)
                                                                .build(),
                                                "hybrid:{query:\"I'm a simple string\" fusionType:rankedFusion}"),
                                Arguments.of(
                                                "fusion type relative score",
                                                HybridArgument.builder()
                                                                .query("I'm a simple string")
                                                                .fusionType(FusionType.RELATIVE_SCORE)
                                                                .build(),
                                                "hybrid:{query:\"I'm a simple string\" fusionType:relativeScoreFusion}"),
                                Arguments.of(
                                                "specify properties to search on",
                                                HybridArgument.builder()
                                                                .query("I'm a simple string")
                                                                .properties(new String[] { "prop1", "prop2" })
                                                                .build(),
                                                "hybrid:{query:\"I'm a simple string\" properties:[\"prop1\",\"prop2\"]}"),
                                Arguments.of(
                                                "nearVector search",
                                                HybridArgument.builder().query("I'm a simple string")
                                                                .searches(HybridArgument.Searches.builder()
                                                                                .nearVector(NearVectorArgument
                                                                                                .builder()
                                                                                                // @formatter:off
                                                                                                .vector(new Float[] {.1f, .2f, .3f })
                                                                                                // @formatter:on
                                                                                                .certainty(0.9f)
                                                                                                .build())
                                                                                .build())
                                                                .build(),
                                                "hybrid:{query:\"I'm a simple string\" searches:{nearVector:{vector:[0.1,0.2,0.3] certainty:0.9}}}"),
                                Arguments.of(
                                                "nearText search",
                                                HybridArgument.builder().query("I'm a simple string")
                                                // @formatter:off
                                                        .searches(
                                                                HybridArgument.Searches.builder().nearText(
                                                                        NearTextArgument.builder()
                                                                                .concepts(new String[] { "concept" })
                                                                                .certainty(0.9f)
                                                                                .build())
                                                                        .build())
                                                // @formatter:on
                                                                .build(),
                                                "hybrid:{query:\"I'm a simple string\" searches:{nearText:{concepts:[\"concept\"] certainty:0.9}}}"),
                                Arguments.of(
                                                "target vectors",
                                                HybridArgument.builder().query("I'm a simple string")
                                                                .targets(Targets.builder()
                                                                // @formatter:off
                                                                        .targetVectors(new String[] { "t1", "t2" })
                                                                        .combinationMethod(Targets.CombinationMethod.minimum)
                                                                        .weights(new LinkedHashMap<String, Float>() {{
                                                                                put("t1", 0.8f);
                                                                                put("t2", 0.2f);
                                                                        }})
                                                                        .build())
                                                                // @formatter:on
                                                                .build(),
                                                "hybrid:{query:\"I'm a simple string\" targets:{combinationMethod:minimum targetVectors:[\"t1\",\"t2\"] weights:{t1:0.8 t2:0.2}}}"));
        }

        @ParameterizedTest
        @MethodSource("testCases")
        void test(String name, HybridArgument hybrid, String expected) {
                String actual = hybrid.build();
                assertThat(actual).as(name).isEqualTo(expected);
        }
}
