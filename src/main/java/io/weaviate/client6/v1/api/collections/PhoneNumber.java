package io.weaviate.client6.v1.api.collections;

import com.google.gson.annotations.SerializedName;

public record PhoneNumber(
    /** Raw input data provided at creation. */
    @SerializedName("input") String rawInput,
    /**
     * ISO 3166-1 alpha-2 country code. Required only if the raw input does not
     * include an explicit country code, e.g. {@code +31}. Only present if provided
     * by user.
     */
    @SerializedName("defaultCountry") String defaultCountry,
    /** Numerical country code. Returned by Weaviate on read. */
    @SerializedName("countryCode") Integer countryCode,
    /**
     * Phone number with numerical country code prepended.
     * Returned by Weaviate on read.
     */
    @SerializedName("internationalFormatted") String internationalFormatted,
    /**
     * Numerical representation of the national number.
     * Returned by Weaviate on read.
     */
    @SerializedName("national") Integer national,
    /**
     * Formatted national number.
     * Returned by Weaviate on read.
     */
    @SerializedName("nationalFormatted") String nationalFormatted,
    /**
     * Whether the server recognized this number as valid.
     * Returned by Weaviate on read.
     */
    @SerializedName("valid") Boolean valid) {

  /**
   * Create national phone number (without explicit country code),
   * e.g. {@code "020 1234567"}
   *
   * @param country     ISO 3166-1 alpha-2 country code.
   * @param phoneNumber Phone number.
   * @return PhoneNumber
   */
  public static PhoneNumber national(String country, String phoneNumber) {
    return new PhoneNumber(phoneNumber, country, null, null, null, null, null);
  }

  /**
   * Create a phone number with explicit country code,
   * e.g. {@code "+31 20 1234567"}
   *
   * @param phoneNumber Phone number.
   * @return PhoneNumber
   */
  public static PhoneNumber international(String phoneNumber) {
    return new PhoneNumber(phoneNumber, null, null, null, null, null, null);
  }
}
