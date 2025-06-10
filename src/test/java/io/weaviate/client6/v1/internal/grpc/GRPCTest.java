package io.weaviate.client6.v1.internal.grpc;

import static org.junit.Assert.assertArrayEquals;

import org.junit.Test;

import com.google.protobuf.ByteString;

/**
 * Note: Java's {@code byte} is signed (int8) and is different from {@code byte}
 * in Go, which is an alias for uint8.
 *
 * For this tests purposes the distinction is immaterial, as "want" arrays
 * are "golden values" meant to be a readable respresentation for the test.
 */
public class GRPCTest {
  @Test
  public void test_toBytesString_1d() {
    Float[] vector = { 1f, 2f, 3f };
    byte[] want = { 0, 0, -128, 63, 0, 0, 0, 64, 0, 0, 64, 64 };
    byte[] got = GRPC.toByteString(vector).toByteArray();
    assertArrayEquals(want, got);
  }

  @Test
  public void test_fromBytesString_1d() {
    byte[] bytes = { 0, 0, -128, 63, 0, 0, 0, 64, 0, 0, 64, 64 };
    Float[] want = { 1f, 2f, 3f };
    Float[] got = GRPC.fromByteString(ByteString.copyFrom(bytes));
    assertArrayEquals(want, got);
  }

  @Test
  public void test_toBytesString_2d() {
    Float[][] vector = { { 1f, 2f, 3f }, { 4f, 5f, 6f } };
    byte[] want = { 3, 0, 0, 0, -128, 63, 0, 0, 0, 64, 0, 0, 64, 64, 0, 0, -128, 64, 0, 0, -96, 64, 0, 0, -64, 64 };
    byte[] got = GRPC.toByteString(vector).toByteArray();
    assertArrayEquals(want, got);
  }

  @Test
  public void test_fromBytesString_2d() {
    byte[] bytes = { 3, 0, 0, 0, -128, 63, 0, 0, 0, 64, 0, 0, 64, 64, 0, 0, -128, 64, 0, 0, -96, 64, 0, 0, -64, 64 };
    Float[][] want = { { 1f, 2f, 3f }, { 4f, 5f, 6f } };
    Float[][] got = GRPC.fromByteStringMulti(ByteString.copyFrom(bytes));
    assertArrayEquals(want, got);
  }
}
