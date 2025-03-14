package io.weaviate.client6.internal;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;

import com.google.protobuf.ByteString;

public class GRPC {
  private static final ByteOrder BYTE_ORDER = ByteOrder.LITTLE_ENDIAN;

  /** Encode Float[] to ByteString. */
  public static ByteString toByteString(Float[] vector) {
    if (vector == null || vector.length == 0) {
      return ByteString.EMPTY;
    }
    ByteBuffer buffer = ByteBuffer.allocate(vector.length * Float.BYTES).order(BYTE_ORDER);
    Arrays.stream(vector).forEach(buffer::putFloat);
    return ByteString.copyFrom(buffer.array());
  }

  /** Encode float[] to ByteString. */
  public static ByteString toByteString(float[] vector) {
    ByteBuffer buffer = ByteBuffer.allocate(vector.length * Float.BYTES).order(BYTE_ORDER);
    for (float f : vector) {
      buffer.putFloat(f);
    }
    return ByteString.copyFrom(buffer.array());
  }

  /**
   * Encode Float[][] to ByteString.
   * <p>
   * The first 2 bytes of the resulting ByteString encode the number of dimensions
   * (uint16 / short) followed by concatenated vectors (4 bytes per element).
   */
  public static ByteString toByteString(Float[][] vectors) {
    if (vectors == null || vectors.length == 0 || vectors[0].length == 0) {
      return ByteString.EMPTY;
    }

    int n = vectors.length;
    short dimensions = (short) vectors[0].length;
    int capacity = /* vector dimensions */ Short.BYTES +
    /* concatenated elements */ (n * dimensions * Float.BYTES);
    ByteBuffer buffer = ByteBuffer.allocate(capacity).order(BYTE_ORDER)
        .putShort(dimensions);
    Arrays.stream(vectors).forEach(v -> Arrays.stream(v).forEach(buffer::putFloat));
    return ByteString.copyFrom(buffer.array());
  }

  /**
   * Decode ByteString into a Float[]. ByteString size must be a multiple of
   * {@link Float#BYTES}, throws {@link IllegalArgumentException} otherwise.
   */
  public static Float[] fromByteString(ByteString bs) {
    if (bs.size() % Float.BYTES != 0) {
      throw new IllegalArgumentException(
          "byte string size not a multiple of " + String.valueOf(Float.BYTES) + " (Float.BYTES)");
    }
    float[] vector = new float[bs.size() / Float.BYTES];
    bs.asReadOnlyByteBuffer().order(BYTE_ORDER).asFloatBuffer().get(vector);
    return ArrayUtils.toObject(vector);
  }

  /** Decode ByteString into a Float[][]. */
  public static Float[][] fromByteStringMulti(ByteString bs) {
    if (bs == null || bs.size() == 0) {
      return new Float[0][0];
    }

    ByteBuffer buf = bs.asReadOnlyByteBuffer().order(BYTE_ORDER);

    // Dimensions are encoded in the first 2 bytes.
    short dimensions = buf.getShort(); // advances current position

    FloatBuffer fbuf = buf.asFloatBuffer();
    int n = fbuf.remaining() / dimensions; // fbuf size is buf / Float.BYTES

    // Reading from buffer advances current position,
    // so we always read from offset=0.
    Float[][] vectors = new Float[n][dimensions];
    for (int i = 0; i < n; i++) {
      float[] v = new float[dimensions];
      fbuf.get(v, 0, dimensions);
      vectors[i] = ArrayUtils.toObject(v);
    }
    return vectors;
  }
}
