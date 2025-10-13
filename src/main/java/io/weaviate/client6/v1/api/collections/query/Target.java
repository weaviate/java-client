package io.weaviate.client6.v1.api.collections.query;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.google.protobuf.ByteString;

import io.weaviate.client6.v1.internal.grpc.ByteStringUtil;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBase;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBaseSearch;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBaseSearch.Targets.Builder;

public interface Target {

  boolean appendTargets(WeaviateProtoBaseSearch.Targets.Builder req);

  record VectorTarget(
      /** Name of the vector index to search compare the input vector to. */
      String vectorName,
      /**
       * Weight assigned to this vector distance. Only required with
       * RELATIVE_SCORE and MANUAL combination methods.
       */
      Float weight,
      /** Query vector. Must be either {@code float[]} or {@code float[][]}. */
      Object vector) implements NearVectorTarget {

    /**
     * Append target vector name and, if provided, the weight it should be assigned.
     */
    @Override
    public boolean appendTargets(WeaviateProtoBaseSearch.Targets.Builder req) {
      if (vectorName == null) {
        return false;
      }
      req.addTargetVectors(vectorName);

      var weightsForTarget = WeaviateProtoBaseSearch.WeightsForTarget.newBuilder()
          .setTarget(vectorName);
      if (weight != null) {
        weightsForTarget.setWeight(weight);
      }
      req.addWeightsForTargets(weightsForTarget);
      return true;
    }

    /**
     * Append vectors if this is a single target vector.
     * Must not be called from {@link CombinedVectorTarget}.
     */
    @Override
    public void appendVectors(WeaviateProtoBaseSearch.NearVector.Builder req) {
      if (vectorName == null) {
        req.addVectors(encodeVectors());
      } else {
        req.addVectorForTargets(WeaviateProtoBaseSearch.VectorForTarget.newBuilder()
            .setName(vectorName)
            .addVectors(encodeVectors()));
      }
    }

    /**
     * Encode search vector as a {@link ByteString}
     * with the corresponding type (single/multi).
     */
    private WeaviateProtoBase.Vectors.Builder encodeVectors() {
      assert vector instanceof float[] || vector instanceof float[][];

      return vector instanceof float[] single
          ? WeaviateProtoBase.Vectors.newBuilder()
              .setType(WeaviateProtoBase.Vectors.VectorType.VECTOR_TYPE_SINGLE_FP32)
              .setVectorBytes(ByteStringUtil.encodeVectorSingle(single))
          : WeaviateProtoBase.Vectors.newBuilder()
              .setType(WeaviateProtoBase.Vectors.VectorType.VECTOR_TYPE_MULTI_FP32)
              .setVectorBytes(ByteStringUtil.encodeVectorMulti((float[][]) vector));
    }
  }

  static VectorTarget vector(float[] vector) {
    return new VectorTarget(null, null, vector);
  }

  static VectorTarget vector(String vectorName, float[] vector) {
    return new VectorTarget(vectorName, null, vector);
  }

  static VectorTarget vector(String vectorName, float weight, float[] vector) {
    return new VectorTarget(vectorName, weight, vector);
  }

  static Target combine(CombinationMethod combinationMethod, VectorTarget... vectorTargets) {
    return new CombinedVectorTarget(combinationMethod, Arrays.asList(vectorTargets));
  }

  static Target sum(VectorTarget... vectorTargets) {
    return combine(CombinationMethod.SUM, vectorTargets);
  }

  static Target min(VectorTarget... vectorTargets) {
    return combine(CombinationMethod.MIN, vectorTargets);
  }

  static Target average(VectorTarget... vectorTargets) {
    return combine(CombinationMethod.AVERAGE, vectorTargets);
  }

  static Target relativeScore(VectorTarget... vectorTargets) {
    return combine(CombinationMethod.RELATIVE_SCORE, vectorTargets);
  }

  static Target manualWeights(VectorTarget... vectorTargets) {
    return combine(CombinationMethod.MANUAL_WEIGHTS, vectorTargets);
  }

  enum CombinationMethod {
    SUM,
    MIN,
    AVERAGE,
    RELATIVE_SCORE,
    MANUAL_WEIGHTS;
  }

  record CombinedVectorTarget(CombinationMethod combinationMethod, List<VectorTarget> targets)
      implements NearVectorTarget {

    @Override
    public boolean appendTargets(WeaviateProtoBaseSearch.Targets.Builder req) {
      if (targets.isEmpty()) {
        return false;
      }
      switch (combinationMethod) {
        case SUM:
          req.setCombination(WeaviateProtoBaseSearch.CombinationMethod.COMBINATION_METHOD_TYPE_SUM);
          break;
        case MIN:
          req.setCombination(WeaviateProtoBaseSearch.CombinationMethod.COMBINATION_METHOD_TYPE_MIN);
          break;
        case AVERAGE:
          req.setCombination(WeaviateProtoBaseSearch.CombinationMethod.COMBINATION_METHOD_TYPE_AVERAGE);
          break;
        case RELATIVE_SCORE:
          req.setCombination(WeaviateProtoBaseSearch.CombinationMethod.COMBINATION_METHOD_TYPE_RELATIVE_SCORE);
          break;
        case MANUAL_WEIGHTS:
          req.setCombination(WeaviateProtoBaseSearch.CombinationMethod.COMBINATION_METHOD_TYPE_MANUAL);
          break;
      }
      targets.forEach(t -> t.appendTargets(req));
      return true;
    }

    @Override
    /** Append combined vector targets. */
    public void appendVectors(WeaviateProtoBaseSearch.NearVector.Builder req) {
      if (targets.isEmpty()) {
        return;
      }

      targets
          .stream()
          .collect(Collectors.groupingBy(VectorTarget::vectorName, Collectors.toList()))
          .entrySet()
          .forEach(target -> {
            var vectorForTarget = WeaviateProtoBaseSearch.VectorForTarget.newBuilder()
                .setName(target.getKey());
            target.getValue().forEach(vt -> {
              vectorForTarget.addVectors(vt.encodeVectors());
            });
          });
    }
  }

  record TextTarget(String vectorName, Float weight, List<String> query) implements Target {

    @Override
    public boolean appendTargets(Builder req) {
      if (vectorName == null) {
        return false;
      }
      req.addTargetVectors(vectorName);

      var weightsForTarget = WeaviateProtoBaseSearch.WeightsForTarget.newBuilder()
          .setTarget(vectorName);
      if (weight != null) {
        weightsForTarget.setWeight(weight);
      }
      req.addWeightsForTargets(weightsForTarget);
      return true;

    }
  }

  static TextTarget text(List<String> text) {
    return new TextTarget(null, null, text);
  }

  static TextTarget text(String vectorName, String... text) {
    return new TextTarget(vectorName, null, Arrays.asList(text));
  }

  static TextTarget text(String vectorName, float weight, String... text) {
    return new TextTarget(vectorName, weight, Arrays.asList(text));
  }

  record CombinedTextTarget(List<String> query, CombinationMethod combinationMethod, List<TextTarget> targets)
      implements Target {

    @Override
    public boolean appendTargets(WeaviateProtoBaseSearch.Targets.Builder req) {
      if (targets.isEmpty()) {
        return false;
      }
      switch (combinationMethod) {
        case SUM:
          req.setCombination(WeaviateProtoBaseSearch.CombinationMethod.COMBINATION_METHOD_TYPE_SUM);
          break;
        case MIN:
          req.setCombination(WeaviateProtoBaseSearch.CombinationMethod.COMBINATION_METHOD_TYPE_MIN);
          break;
        case AVERAGE:
          req.setCombination(WeaviateProtoBaseSearch.CombinationMethod.COMBINATION_METHOD_TYPE_AVERAGE);
          break;
        case RELATIVE_SCORE:
          req.setCombination(WeaviateProtoBaseSearch.CombinationMethod.COMBINATION_METHOD_TYPE_RELATIVE_SCORE);
          break;
        case MANUAL_WEIGHTS:
          req.setCombination(WeaviateProtoBaseSearch.CombinationMethod.COMBINATION_METHOD_TYPE_MANUAL);
          break;
      }
      targets.forEach(t -> t.appendTargets(req));
      return true;
    }
  }
}
