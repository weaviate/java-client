package io.weaviate.client6.v1.internal.oidc.nimbus;

import com.google.gson.annotations.SerializedName;
import com.nimbusds.oauth2.sdk.ErrorResponse;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.TokenResponse;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponse;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponseParser;
import com.nimbusds.openid.connect.sdk.token.OIDCTokens;

import io.weaviate.client6.v1.api.WeaviateOAuthException;
import io.weaviate.client6.v1.internal.json.JSON;

final class OIDCTokensParser {
  /**
   * Prevents public instantiation.
   */
  private OIDCTokensParser() {
  }

  /**
   * Parse HTTP response containing a new set of OIDC tokens (access_token and
   * refresh_token).
   *
   * <p>
   * Nimbus expects the following format of an error response, as per RFC 6749:
   *
   * <pre>
   *  {
   *    "error_code": "invalid_client",
   *    "error_description": "Invalid value for 'client_id' parameter."
   *  }
   * </pre>
   *
   * Unfortunately, not all OIDC servers adhere to it. E.g. Okta returns
   * {@code "errorCode"} and {@code "errorSummary"}, which Nimbus's
   * {@link OIDCTokenResponseParser} fails to parse. In order to get a meaningful
   * error message we make a second pass in case error details are incomplete
   * after the first pass.
   *
   * @throws ParseException         if {@link OIDCTokenResponseParser#parse()}
   *                                failed.
   * @throws WeaviateOAuthException if response indicates error. As determined by
   *                                {@link TokenResponse#indicatesSuccess()}.
   */
  static OIDCTokens parse(HTTPResponse httpResponse) throws ParseException {
    var response = OIDCTokenResponseParser.parse(httpResponse);
    if (response.indicatesSuccess()) {
      return ((OIDCTokenResponse) response).getOIDCTokens();
    }

    var error = fromErrorResponse(response.toErrorResponse());
    if (!error.isComplete()) {
      error = fromHttpResponse(httpResponse);
    }
    throw new WeaviateOAuthException("%s (code=%s)".formatted(
        error.description(),
        error.code()));
  }

  private static record ErrorDetails(
      @SerializedName("errorCode") String code,
      @SerializedName("errorSummary") String description) {

    private boolean isComplete() {
      return code != null && description != null;
    }
  }

  private static ErrorDetails fromErrorResponse(ErrorResponse response) {
    var err = response.getErrorObject();
    return new ErrorDetails(err.getCode(), err.getDescription());
  }

  private static ErrorDetails fromHttpResponse(HTTPResponse response) {
    return JSON.deserialize(response.getBody(), ErrorDetails.class);
  }
}
