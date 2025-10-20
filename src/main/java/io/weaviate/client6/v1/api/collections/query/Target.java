package io.weaviate.client6.v1.api.collections.query;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import com.google.protobuf.ByteString;

import io.weaviate.client6.v1.internal.grpc.ByteStringUtil;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBase;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBaseSearch;

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

      if (weight != null) {
        req.addWeightsForTargets(WeaviateProtoBaseSearch.WeightsForTarget.newBuilder()
            .setTarget(vectorName)
            .setWeight(weight));
      }
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

  static VectorTarget vector(float[][] vector) {
    return new VectorTarget(null, null, vector);
  }

  static VectorTarget vector(String vectorName, float[] vector) {
    return new VectorTarget(vectorName, null, vector);
  }

  static VectorTarget vector(String vectorName, float[][] vector) {
    return new VectorTarget(vectorName, null, vector);
  }

  static VectorTarget vector(String vectorName, float weight, float[] vector) {
    return new VectorTarget(vectorName, weight, vector);
  }

  static VectorTarget vector(String vectorName, float weight, float[][] vector) {
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

      // We use LinkedHashMap to preserve insertion order.
      // This has negligble performance penalty, if any,
      // but allows for a predictable output in tests.
      targets
          .stream()
          .collect(Collectors.groupingBy(
              VectorTarget::vectorName,
              LinkedHashMap::new,
              Collectors.toList()))
          .entrySet()
          .forEach(target -> {
            var vectorForTargets = WeaviateProtoBaseSearch.VectorForTarget.newBuilder()
                .setName(target.getKey());
            target.getValue().forEach(vt -> {
              vectorForTargets.addVectors(vt.encodeVectors());
            });
            req.addVectorForTargets(vectorForTargets);
          });
    }
  }

  record TextTarget(VectorWeight weight, List<String> query) implements Target {

    private TextTarget(String vectorName, Float weight, List<String> query) {
      this(new VectorWeight(vectorName, weight), query);
    }

    @Override
    public boolean appendTargets(WeaviateProtoBaseSearch.Targets.Builder req) {
      return weight.appendTargets(req);
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

  static TextTarget blob(String blob) {
    return new TextTarget(null, null, Collections.singletonList(blob));
  }

  static TextTarget blob(String vectorName, String blob) {
    return new TextTarget(vectorName, null, Collections.singletonList(blob));
  }

  static TextTarget blob(String vectorName, float weight, String blob) {
    return new TextTarget(vectorName, weight, Collections.singletonList(blob));
  }

  static TextTarget uuid(String uuid) {
    return new TextTarget(null, null, Collections.singletonList(uuid));
  }

  static TextTarget uuid(String vectorName, String uuid) {
    return new TextTarget(vectorName, null, Collections.singletonList(uuid));
  }

  static TextTarget uuid(String vectorName, float weight, String uuid) {
    return new TextTarget(vectorName, weight, Collections.singletonList(uuid));
  }

  /**
   * Weight to be applied to the vector distance. Used for text-based
   * queries where only a single input is allowed.
   */
  record VectorWeight(String vectorName, Float weight) {
    public boolean appendTargets(WeaviateProtoBaseSearch.Targets.Builder req) {
      if (vectorName == null) {
        return false;
      }
      req.addTargetVectors(vectorName);

      if (weight != null) {
        req.addWeightsForTargets(WeaviateProtoBaseSearch.WeightsForTarget.newBuilder()
            .setTarget(vectorName)
            .setWeight(weight));
      }
      return true;
    }
  }

  record CombinedTextTarget(List<String> query, CombinationMethod combinationMethod, List<VectorWeight> vectorWeights)
      implements Target {

    @Override
    public boolean appendTargets(WeaviateProtoBaseSearch.Targets.Builder req) {
      if (vectorWeights.isEmpty()) {
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
      vectorWeights.forEach(t -> t.appendTargets(req));
      return true;
    }
  }

  static VectorWeight weight(String vectorName, float weight) {
    return new VectorWeight(vectorName, weight);
  }

  static Target combine(List<String> query, CombinationMethod combinationMethod, VectorWeight... vectorWeights) {
    return new CombinedTextTarget(query, combinationMethod, Arrays.asList(vectorWeights));
  }

  static Target combine(List<String> query, CombinationMethod combinationMethod, String... targetVectors) {
    var vectorWeights = Arrays.stream(targetVectors)
        .map(vw -> new VectorWeight(vw, null))
        .toArray(VectorWeight[]::new);
    return combine(query, combinationMethod, vectorWeights);
  }

  static Target sum(String query, String... targetVectors) {
    return sum(List.of(query), targetVectors);
  }

  static Target sum(List<String> query, String... targetVectors) {
    return combine(query, CombinationMethod.SUM, targetVectors);
  }

  static Target min(String query, String... targetVectors) {
    return min(List.of(query), targetVectors);
  }

  static Target min(List<String> query, String... targetVectors) {
    return combine(query, CombinationMethod.MIN, targetVectors);
  }

  static Target average(String query, String... targetVectors) {
    return average(List.of(query), targetVectors);
  }

  static Target average(List<String> query, String... targetVectors) {
    return combine(query, CombinationMethod.AVERAGE, targetVectors);
  }

  static Target relativeScore(String query, VectorWeight... weights) {
    return relativeScore(List.of(query), weights);
  }

  static Target relativeScore(List<String> query, VectorWeight... weights) {
    return combine(query, CombinationMethod.RELATIVE_SCORE, weights);
  }

  static Target manualWeights(String query, VectorWeight... weights) {
    return manualWeights(List.of(query), weights);
  }

  static Target manualWeights(List<String> query, VectorWeight... weights) {
    return combine(query, CombinationMethod.MANUAL_WEIGHTS, weights);
  }
}
