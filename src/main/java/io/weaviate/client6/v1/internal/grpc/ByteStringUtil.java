package io.weaviate.client6.v1.internal.grpc;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.UUID;

import com.google.protobuf.ByteString;

public class ByteStringUtil {
  private static final ByteOrder BYTE_ORDER = ByteOrder.LITTLE_ENDIAN;

  /** Decode ByteString to UUID. */
  public static UUID decodeUuid(ByteString bs) {
    var buf = ByteBuffer.wrap(bs.toByteArray());
    var most = buf.getLong();
    var least = buf.getLong();
    return new UUID(most, least);
  }

  /** Encode float[] to ByteString. */
  public static ByteString encodeVectorSingle(float[] vector) {
    if (vector == null || vector.length == 0) {
      return ByteString.EMPTY;
    }
    ByteBuffer buffer = ByteBuffer.allocate(vector.length * Float.BYTES).order(BYTE_ORDER);
    for (final var f : vector) {
      buffer.putFloat(f);
    }
    return ByteString.copyFrom(buffer.array());
  }

  /**
   * Encode float[][] to ByteString.
   * <p>
   * The first 2 bytes of the resulting ByteString encode the number of dimensions
   * (uint16 / short) followed by concatenated vectors (4 bytes per element).
   */
  public static ByteString encodeVectorMulti(float[][] vectors) {
    if (vectors == null || vectors.length == 0 || vectors[0].length == 0) {
      return ByteString.EMPTY;
    }

    int n = vectors.length;
    short dimensions = (short) vectors[0].length;
    int capacity = /* vector dimensions */ Short.BYTES +
    /* concatenated elements */ (n * dimensions * Float.BYTES);
    ByteBuffer buffer = ByteBuffer.allocate(capacity).order(BYTE_ORDER)
        .putShort(dimensions);
    Arrays.stream(vectors).forEach(vector -> {
      for (final var f : vector) {
        buffer.putFloat(f);
      }
    });
    return ByteString.copyFrom(buffer.array());
  }

  /**
   * Decode ByteString to float[].
   *
   * @throws IllegalArgumentException if ByteString size is not
   *                                  a multiple of {@link Float#BYTES}.
   */
  public static float[] decodeVectorSingle(ByteString bs) {
    if (bs.size() % Float.BYTES != 0) {
      throw new IllegalArgumentException(
          "ByteString is size " + bs.size() + ", not a multiple of " + String.valueOf(Float.BYTES) + " (Float.BYTES)");
    }
    float[] vector = new float[bs.size() / Float.BYTES];
    bs.asReadOnlyByteBuffer().order(BYTE_ORDER).asFloatBuffer().get(vector);
    return vector;
  }

  /**
   * Decode ByteString to float[][].
   *
   * @throws IllegalArgumentException if ByteString size is not
   *                                  a multiple of {@link Float#BYTES}.
   */
  public static float[][] decodeVectorMulti(ByteString bs) {
    if (bs == null || bs.size() == 0) {
      return new float[0][0];
    }

    ByteBuffer buf = bs.asReadOnlyByteBuffer().order(BYTE_ORDER);

    // Dimensions are encoded in the first 2 bytes.
    short dimensions = buf.getShort(); // advances current position

    // TODO: throw IllegalArgumentException if fbuf.remaining not a multile of
    // Float.BYTES
    FloatBuffer fbuf = buf.asFloatBuffer();
    int n = fbuf.remaining() / dimensions; // fbuf size is buf / Float.BYTES

    // Reading from buffer advances current position,
    // so we always read from offset=0.
    float[][] vectors = new float[n][dimensions];
    for (int i = 0; i < n; i++) {
      float[] v = new float[dimensions];
      // TODO: use pre-allocated array rather than creating a new one
      // fbuf.get(vectors[i], 0, dimensions);
      fbuf.get(v, 0, dimensions);
      vectors[i] = v;
    }
    return vectors;
  }
}
