package io.weaviate.client6.v1.api.collections.query;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.gson.JsonParser;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.util.JsonFormat;
import com.jparams.junit4.JParamsTestRunner;
import com.jparams.junit4.data.DataMethod;

import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBaseSearch;

@RunWith(JParamsTestRunner.class)
public class TargetTest {

  public static Object[][] appendTargetsTestCases() {
    return new Object[][] {
        {
            Target.vector(new float[] { 1, 2, 3 }),
            null,
        },
        {
            Target.average(
                Target.vector("title_vec", new float[] { 1, 2, 3 }),
                Target.vector("body_vec", new float[] { 4, 5, 6 })),
            """
                {
                  "combination": "COMBINATION_METHOD_TYPE_AVERAGE",
                  "targetVectors": ["title_vec", "body_vec"],
                  "weightsForTargets": [
                    {"target": "title_vec"},
                    {"target": "body_vec"}
                  ]
                }
                    """,

        },
        {
            Target.manualWeights(
                Target.vector("title_vec", .2f, new float[] { 1, 2, 3 }),
                Target.vector("title_vec", .3f, new float[] { 1, 2, 3 }),
                Target.vector("body_vec", .5f, new float[] { 4, 5, 6 })),
            """
                {
                  "combination": "COMBINATION_METHOD_TYPE_MANUAL",
                  "targetVectors": ["title_vec", "title_vec", "body_vec"],
                  "weightsForTargets": [
                    {"target": "title_vec", "weight": 0.2},
                    {"target": "title_vec", "weight": 0.3},
                    {"target": "body_vec", "weight": 0.5}
                  ]
                }
                    """,

        },
        {
            Target.min(
                List.of("day", "night"),
                "title_vec", "body_vec"),
            """
                {
                  "combination": "COMBINATION_METHOD_TYPE_MIN",
                  "targetVectors": ["title_vec", "body_vec"],
                  "weightsForTargets": [
                    {"target": "title_vec"},
                    {"target": "body_vec"}
                  ]
                }
                    """,

        },
        {
            Target.relativeScore(
                List.of("one", "two", "three"),
                Target.weight("title_vec", 1),
                Target.weight("title_vec", 2),
                Target.weight("body_vec", 3)),
            """
                {
                  "combination": "COMBINATION_METHOD_TYPE_RELATIVE_SCORE",
                  "targetVectors": ["title_vec", "title_vec", "body_vec"],
                  "weightsForTargets": [
                    {"target": "title_vec", "weight": 1.0},
                    {"target": "title_vec", "weight": 2.0},
                    {"target": "body_vec", "weight": 3.0}
                  ]
                }
                    """,

        },
    };
  }

  @Test
  @DataMethod(source = TargetTest.class, method = "appendTargetsTestCases")
  public void test_appendTargets(Target target, String want) {
    var req = WeaviateProtoBaseSearch.Targets.newBuilder();
    var appended = target.appendTargets(req);
    if (want == null) {
      Assertions.assertThat(appended).as("should not append targets").isFalse();
      return;
    }

    var got = proto2json(req);
    assertEqualJson(want, got);
  }

  private static final String proto2json(MessageOrBuilder proto) {
    String out;
    try {
      out = JsonFormat.printer().print(proto);
    } catch (InvalidProtocolBufferException e) {
      out = e.getMessage();
    }

    return out;
  }

  private static void assertEqualJson(String want, String got) {
    var wantJson = JsonParser.parseString(want);
    var gotJson = JsonParser.parseString(got);
    Assertions.assertThat(gotJson).isEqualTo(wantJson);
  }
}
