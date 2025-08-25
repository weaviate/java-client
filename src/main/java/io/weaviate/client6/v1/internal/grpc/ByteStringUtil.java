package io.weaviate.client6.v1.internal.grpc;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.UUID;

import com.google.protobuf.ByteString;

public class ByteStringUtil {
  /** Prevent public initialization. */
  private ByteStringUtil() {
  }

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
   * Decode ByteString to {@code float[]}.
   *
   * @throws IllegalArgumentException if ByteString size is not
   *                                  a multiple of {@link Float#BYTES}.
   */
  public static float[] decodeVectorSingle(ByteString bs) {
    if (bs.size() % Float.BYTES != 0) {
      throw new IllegalArgumentException(
          "ByteString size " + bs.size() + " is not a multiple of " + String.valueOf(Float.BYTES) + " (Float.BYTES)");
    }
    float[] vector = new float[bs.size() / Float.BYTES];
    bs.asReadOnlyByteBuffer().order(BYTE_ORDER).asFloatBuffer().get(vector);
    return vector;
  }

  /**
   * Decode ByteString to {@code float[][]}.
   *
   * <p>
   * The expected structure of the byte string of total size N is:
   * <ul>
   * <li>[2 bytes]: dimensionality of the inner vector ({@code dim})
   * <li>[N-2 bytes]: concatenated inner vectors. N-2 must be a multiple of
   * {@code Float.BYTES * dim}
   * </ul>
   *
   * @throws IllegalArgumentException if ByteString is not of a valid size.
   */
  public static float[][] decodeVectorMulti(ByteString bs) {
    if (bs == null || bs.size() == 0) {
      return new float[0][0];
    }

    ByteBuffer buf = bs.asReadOnlyByteBuffer().order(BYTE_ORDER);
    short dim = buf.getShort(); // advances current position
    if (dim == 0) {
      return new float[0][0];
    }

    FloatBuffer fbuf = buf.asFloatBuffer(); // fbuf size is buf / Float.BYTES
    if (fbuf.remaining() % dim != 0) {
      throw new IllegalArgumentException(
          "Remaing ByteString size " + fbuf.remaining() + " is not a multiple of " + dim
              + " (dim)");
    }
    int n = fbuf.remaining() / dim;

    // Reading from buffer advances current position,
    // so we always read from offset=0.
    float[][] vectors = new float[n][dim];
    for (int i = 0; i < n; i++) {
      fbuf.get(vectors[i], 0, dim);
    }
    return vectors;
  }

  /**
   * Decode ByteString to {@code long[]}.
   *
   * @throws IllegalArgumentException if ByteString size is not
   *                                  a multiple of {@link Long#BYTES}.
   */
  public static long[] decodeIntValues(ByteString bs) {
    if (bs.size() % Long.BYTES != 0) {
      throw new IllegalArgumentException(
          "ByteString size " + bs.size() + " is not a multiple of " + String.valueOf(Long.BYTES) + " (Long.BYTES)");
    }
    long[] vector = new long[bs.size() / Long.BYTES];
    bs.asReadOnlyByteBuffer().order(BYTE_ORDER).asLongBuffer().get(vector);
    return vector;
  }
}
