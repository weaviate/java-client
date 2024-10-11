package io.weaviate.client.v1.graphql.query.argument;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashMap;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.jparams.junit4.JParamsTestRunner;
import com.jparams.junit4.data.DataMethod;

@RunWith(JParamsTestRunner.class)
public class HybridArgumentTest {
        public static Object[][] provideTestCases() {
                return new Object[][] {
                                {
                                                "simple query",
                                                HybridArgument.builder()
                                                                .query("I'm a simple string")
                                                                .build(),
                                                "hybrid:{query:\"I'm a simple string\"}"
                                },
                                {
                                                "vector and alpha",
                                                HybridArgument.builder()
                                                                .query("I'm a simple string")
                                                                .vector(new Float[] { .1f, .2f, .3f })
                                                                .alpha(.567f)
                                                                .build(),
                                                "hybrid:{query:\"I'm a simple string\" vector:[0.1,0.2,0.3] alpha:0.567}"
                                },
                                {
                                                "vector and target vectors",
                                                HybridArgument.builder()
                                                                .query("I'm a simple string")
                                                                .vector(new Float[] { .1f, .2f, .3f })
                                                                .targetVectors(new String[] { "vector1" })
                                                                .build(),
                                                "hybrid:{query:\"I'm a simple string\" vector:[0.1,0.2,0.3] targetVectors:[\"vector1\"]}"
                                },
                                {
                                                "with escaped characters",
                                                HybridArgument.builder()
                                                                .query("\"I'm a complex string\" says the {'`:string:`'}")
                                                                .build(),
                                                "hybrid:{query:\"\\\"I'm a complex string\\\" says the {'`:string:`'}\"}"
                                },
                                {
                                                "fusion type ranked",
                                                HybridArgument.builder()
                                                                .query("I'm a simple string")
                                                                .fusionType(FusionType.RANKED)
                                                                .build(),
                                                "hybrid:{query:\"I'm a simple string\" fusionType:rankedFusion}"
                                },
                                {
                                                "fusion type relative score",
                                                HybridArgument.builder()
                                                                .query("I'm a simple string")
                                                                .fusionType(FusionType.RELATIVE_SCORE)
                                                                .build(),
                                                "hybrid:{query:\"I'm a simple string\" fusionType:relativeScoreFusion}"
                                },
                                {
                                                "specify properties to search on",
                                                HybridArgument.builder()
                                                                .query("I'm a simple string")
                                                                .properties(new String[] { "prop1", "prop2" })
                                                                .build(),
                                                "hybrid:{query:\"I'm a simple string\" properties:[\"prop1\",\"prop2\"]}"
                                },
                                {
                                                "nearVector search",
                                                HybridArgument.builder().query("I'm a simple string")
                                                // @formatter:off
                                                        .searches(HybridArgument.Searches.builder()
                                                                .nearVector(NearVectorArgument.builder()
                                                                        .vector(new Float[] {.1f, .2f, .3f })
                                                                        .certainty(0.9f)
                                                                        .build())
                                                                .build())
                                                // @formatter:on
                                                                .build(),
                                                "hybrid:{query:\"I'm a simple string\" searches:{nearVector:{vector:[0.1,0.2,0.3] certainty:0.9}}}"
                                },
                                {
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
                                                "hybrid:{query:\"I'm a simple string\" searches:{nearText:{concepts:[\"concept\"] certainty:0.9}}}"
                                },
                                {
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
                                                "hybrid:{query:\"I'm a simple string\" targets:{combinationMethod:minimum targetVectors:[\"t1\",\"t2\"] weights:{t1:0.8 t2:0.2}}}"
                                },
                                {
                                                "max vector distance",
                                                HybridArgument.builder().query("I'm a simple string")
                                                // @formatter:off
                                                .searches(HybridArgument.Searches.builder().nearText(
                                                        NearTextArgument.builder().concepts(new String[]{"concept"}).build()).build()
                                                )
                                                .maxVectorDistance(.5f)
                                                // @formatter:on
                                                                .build(),
                                                "hybrid:{query:\"I'm a simple string\" maxVectorDistance:0.5 searches:{nearText:{concepts:[\"concept\"]}}}"
                                }
                };
        }

        @DataMethod(source = HybridArgumentTest.class, method = "provideData")
        @Test
        public void test(String name, HybridArgument hybrid, String expected) throws Exception {
                String actual = hybrid.build();
                assertThat(actual).as(name).isEqualTo(expected);
        }
}
