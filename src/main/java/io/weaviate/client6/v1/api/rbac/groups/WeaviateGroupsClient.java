package io.weaviate.client6.v1.api.rbac.groups;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import io.weaviate.client6.v1.api.WeaviateApiException;
import io.weaviate.client6.v1.api.rbac.Role;
import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.rest.RestTransport;

public class WeaviateGroupsClient {
  private final RestTransport restTransport;

  public WeaviateGroupsClient(RestTransport restTransport) {
    this.restTransport = restTransport;
  }

  /**
   * Get the roles assigned an OIDC group.
   *
   * @param groupId OIDC group ID.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @throws IOException          in case the request was not sent successfully
   *                              due to a malformed request, a networking error
   *                              or the server being unavailable.
   */
  public List<Role> assignedRoles(String groupId) throws IOException {
    return this.restTransport.performRequest(GetAssignedRolesRequest.of(groupId), GetAssignedRolesRequest._ENDPOINT);
  }

  /**
   * Get the roles assigned an OIDC group.
   *
   * @param groupId OIDC group ID.
   * @param fn      Lambda expression for optional parameters.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @throws IOException          in case the request was not sent successfully
   *                              due to a malformed request, a networking error
   *                              or the server being unavailable.
   */
  public List<Role> assignedRoles(String groupId,
      Function<GetAssignedRolesRequest.Builder, ObjectBuilder<GetAssignedRolesRequest>> fn) throws IOException {
    return this.restTransport.performRequest(GetAssignedRolesRequest.of(groupId, fn),
        GetAssignedRolesRequest._ENDPOINT);
  }

  /**
   * Get the names of known OIDC groups.
   *
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @throws IOException          in case the request was not sent successfully
   *                              due to a malformed request, a networking error
   *                              or the server being unavailable.
   */
  public List<String> knownGroupNames() throws IOException {
    return this.restTransport.performRequest(null, GetKnownGroupNamesRequest._ENDPOINT);
  }

  /**
   * Assign roles to OIDC group.
   *
   * @param groupId   OIDC group ID.
   * @param roleNames Role names.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @throws IOException          in case the request was not sent successfully
   *                              due to a malformed request, a networking error
   *                              or the server being unavailable.
   */
  public void assignRoles(String groupId, String... roleNames) throws IOException {
    this.restTransport.performRequest(new AssignRolesRequest(groupId, Arrays.asList(roleNames)),
        AssignRolesRequest._ENDPOINT);
  }

  /**
   * Revoke roles from OIDC group.
   *
   * @param groupId   OIDC group ID.
   * @param roleNames Role names.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @throws IOException          in case the request was not sent successfully
   *                              due to a malformed request, a networking error
   *                              or the server being unavailable.
   */
  public void revokeRoles(String groupId, String... roleNames) throws IOException {
    this.restTransport.performRequest(new RevokeRolesRequest(groupId, Arrays.asList(roleNames)),
        RevokeRolesRequest._ENDPOINT);
  }
}
