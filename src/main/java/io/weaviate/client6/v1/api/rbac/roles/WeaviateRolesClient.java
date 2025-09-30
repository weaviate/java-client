package io.weaviate.client6.v1.api.rbac.roles;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import io.weaviate.client6.v1.api.WeaviateApiException;
import io.weaviate.client6.v1.api.rbac.Permission;
import io.weaviate.client6.v1.api.rbac.Role;
import io.weaviate.client6.v1.internal.rest.RestTransport;

public class WeaviateRolesClient {
  private final RestTransport restTransport;

  public WeaviateRolesClient(RestTransport restTransport) {
    this.restTransport = restTransport;
  }

  /**
   * Create a new role.
   *
   * @param roleName    Role name.
   * @param permissions Permissions granted to the role.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @throws IOException          in case the request was not sent successfully
   *                              due to a malformed request, a networking error
   *                              or the server being unavailable.
   */
  public void create(String roleName, Permission... permissions) throws IOException {
    var role = new Role(roleName, permissions);
    this.restTransport.performRequest(new CreateRoleRequest(role), CreateRoleRequest._ENDPOINT);
  }

  /**
   * Check if a role with a given name exists.
   *
   * @param roleName Role name.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @throws IOException          in case the request was not sent successfully
   *                              due to a malformed request, a networking error
   *                              or the server being unavailable.
   */
  public boolean exists(String roleName) throws IOException {
    return this.restTransport.performRequest(new RoleExistsRequest(roleName), RoleExistsRequest._ENDPOINT);
  }

  /**
   * Fetch role definition.
   *
   * @param roleName Role name.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @throws IOException          in case the request was not sent successfully
   *                              due to a malformed request, a networking error
   *                              or the server being unavailable.
   */
  public Role get(String roleName) throws IOException {
    return this.restTransport.performRequest(new GetRoleRequest(roleName), GetRoleRequest._ENDPOINT);
  }

  /**
   * List all existing roles.
   *
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @throws IOException          in case the request was not sent successfully
   *                              due to a malformed request, a networking error
   *                              or the server being unavailable.
   */
  public List<Role> list() throws IOException {
    return this.restTransport.performRequest(null, ListRolesRequest._ENDPOINT);
  }

  /**
   * Delete a role.
   *
   * @param roleName Role name.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @throws IOException          in case the request was not sent successfully
   *                              due to a malformed request, a networking error
   *                              or the server being unavailable.
   */
  public void delete(String roleName) throws IOException {
    this.restTransport.performRequest(new DeleteRoleRequest(roleName), DeleteRoleRequest._ENDPOINT);
  }

  /**
   * Add permissions to a role.
   *
   * @param roleName    Role name.
   * @param permissions Permissions to add to the role.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @throws IOException          in case the request was not sent successfully
   *                              due to a malformed request, a networking error
   *                              or the server being unavailable.
   */
  public void addPermissions(String roleName, Permission... permissions) throws IOException {
    this.restTransport.performRequest(new AddPermissionsRequest(roleName, Arrays.asList(permissions)),
        AddPermissionsRequest._ENDPOINT);
  }

  /**
   * Remove permissions from a role.
   *
   * @param roleName    Role name.
   * @param permissions Permissions to remove from the role.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @throws IOException          in case the request was not sent successfully
   *                              due to a malformed request, a networking error
   *                              or the server being unavailable.
   */
  public void removePermissions(String roleName, Permission... permissions) throws IOException {
    this.restTransport.performRequest(new RemovePermissionsRequest(roleName, Arrays.asList(permissions)),
        RemovePermissionsRequest._ENDPOINT);
  }

  /**
   * Check if a role has a set of permissions.
   *
   * @param roleName   Role name.
   * @param permission Permission to check.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @throws IOException          in case the request was not sent successfully
   *                              due to a malformed request, a networking error
   *                              or the server being unavailable.
   */
  public boolean hasPermission(String roleName, Permission permission) throws IOException {
    return this.restTransport.performRequest(new HasPermissionRequest(roleName, permission),
        HasPermissionRequest._ENDPOINT);
  }

  /**
   * Get IDs of all users this role is assigned to.
   *
   * @param roleName Role name.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @throws IOException          in case the request was not sent successfully
   *                              due to a malformed request, a networking error
   *                              or the server being unavailable.
   */
  public List<String> assignedUserIds(String roleName) throws IOException {
    return this.restTransport.performRequest(new GetAssignedUsersRequest(roleName), GetAssignedUsersRequest._ENDPOINT);
  }

  /**
   * Get IDs of all users this role is assigned to along with their user type.
   *
   * <p>
   * Note that, unlike {@link #assignedUserIds}, this method MAY return multiple
   * entries for the same user ID if OIDCS authentication is enabled: once with
   * "db_*" and another time with "oidc" user type.
   *
   * @param roleName Role name.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @throws IOException          in case the request was not sent successfully
   *                              due to a malformed request, a networking error
   *                              or the server being unavailable.
   */
  public List<UserAssignment> userAssignments(String roleName) throws IOException {
    return this.restTransport.performRequest(new GetUserAssignementsRequest(roleName),
        GetUserAssignementsRequest._ENDPOINT);
  }

  /**
   * Get IDs of all groups this role is assigned to along with their group type.
   *
   * @param roleName Role name.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @throws IOException          in case the request was not sent successfully
   *                              due to a malformed request, a networking error
   *                              or the server being unavailable.
   */
  public List<GroupAssignment> groupAssignments(String roleName) throws IOException {
    return this.restTransport.performRequest(new GetGroupAssignementsRequest(roleName),
        GetGroupAssignementsRequest._ENDPOINT);
  }
}
