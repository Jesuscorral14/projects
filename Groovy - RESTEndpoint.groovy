import com.onresolve.scriptrunner.runner.rest.common.CustomEndpointDelegate
 
import groovy.transform.BaseScript
 
import com.atlassian.sal.api.component.ComponentLocator
import com.atlassian.confluence.spaces.SpaceManager
import com.atlassian.confluence.spaces.Space
import com.atlassian.confluence.security.DefaultSpacePermissionManager
import com.atlassian.confluence.security.SpacePermissionManager
import com.atlassian.confluence.security.SpacePermission
import com.atlassian.confluence.user.ConfluenceUserManager
import com.atlassian.user.UserManager
import com.atlassian.confluence.user.ConfluenceUser
 
import javax.ws.rs.core.MultivaluedMap
import javax.ws.rs.core.Response
import com.atlassian.confluence.internal.security.SpacePermissionSaverInternal
import com.atlassian.confluence.internal.security.SpacePermissionContext
 
@BaseScript CustomEndpointDelegate delegate
 
//REST Endpoint definition with GET method
updatePermissions(httpMethod: "GET"){ MultivaluedMap queryParams ->
 
  def spaceManager = ComponentLocator.getComponent(SpaceManager) //define space Manager to get space objects
  def spacePermissionManager = ComponentLocator.getComponent(SpacePermissionManager) //define space permission manager to alter permissions on a space
  def userManager = ComponentLocator.getComponent(UserManager)
  def space = spaceManager.getSpace(queryParams.getFirst("spaceKey") as String)  //pulls the first instance of spaceKey in the parameters passed by the post-function
  def user = userManager.getUser(queryParams.getFirst("user") as String) as ConfluenceUser //pulls the first instance of user in the parameters passed by the post-function
  SpacePermissionContext context = SpacePermissionContext.createDefault()
  def spacePermissionToSave
  log.warn("user is: " + user) //log the user and the space for debugging purposes
  log.warn("spaceKey is: " + space)
 
  //Apply the permissions selected based on the parameters in the REST call. Note, permissions I never want used will never be set, i.e. no backdoor to just make the REST call to get admin permissions.
  if (queryParams.getFirst("view") == "true")
  {
    spacePermissionToSave = SpacePermission.createUserSpacePermission(SpacePermission.VIEWSPACE_PERMISSION, space, user)
    spacePermissionManager.savePermission(spacePermissionToSave)
  }
  if (queryParams.getFirst("removeOwn") == "true")
  {
    spacePermissionToSave = SpacePermission.createUserSpacePermission(SpacePermission.REMOVE_OWN_CONTENT_PERMISSION, space, user)
    spacePermissionManager.savePermission(spacePermissionToSave)
  }
  if (queryParams.getFirst("createEditPage") == "true")
  {
    spacePermissionToSave = SpacePermission.createUserSpacePermission(SpacePermission.CREATEEDIT_PAGE_PERMISSION, space, user)
    spacePermissionManager.savePermission(spacePermissionToSave)
  }
  if (queryParams.getFirst("removePage") == "true")
  {
    spacePermissionToSave = SpacePermission.createUserSpacePermission(SpacePermission.REMOVE_PAGE_PERMISSION, space, user)
    spacePermissionManager.savePermission(spacePermissionToSave)
  }
  if (queryParams.getFirst("editBlog") == "true")
  {
    spacePermissionToSave = SpacePermission.createUserSpacePermission(SpacePermission.EDITBLOG_PERMISSION, space, user)
    spacePermissionManager.savePermission(spacePermissionToSave)
  }
  if (queryParams.getFirst("createAttachment") == "true")
  {
    spacePermissionToSave = SpacePermission.createUserSpacePermission(SpacePermission.CREATE_ATTACHMENT_PERMISSION, space, user)
    spacePermissionManager.savePermission(spacePermissionToSave)
  }
  if (queryParams.getFirst("removeAttachment") == "true")
  {
    spacePermissionToSave = SpacePermission.createUserSpacePermission(SpacePermission.REMOVE_ATTACHMENT_PERMISSION, space, user)
    spacePermissionManager.savePermission(spacePermissionToSave)
  }
  if (queryParams.getFirst("comment") == "true")
  {
    spacePermissionToSave = SpacePermission.createUserSpacePermission(SpacePermission.COMMENT_PERMISSION, space, user)
    spacePermissionManager.savePermission(spacePermissionToSave)
  }
  if (queryParams.getFirst("setPagePermissions") == "true")
  {
    spacePermissionToSave = SpacePermission.createUserSpacePermission(SpacePermission.SET_PAGE_PERMISSIONS_PERMISSION, space, user)
    spacePermissionManager.savePermission(spacePermissionToSave)
  }
  if (queryParams.getFirst("exportSpace") == "true")
  {
    spacePermissionToSave = SpacePermission.createUserSpacePermission(SpacePermission.EXPORT_SPACE_PERMISSION, space, user)
    spacePermissionManager.savePermission(spacePermissionToSave)
       
    spacePermissionToSave = SpacePermission.createUserSpacePermission(SpacePermission.ADMINISTER_SPACE_PERMISSION, space, user)
    spacePermissionManager.savePermission(spacePermissionToSave)
    
    spacePermissionToSave = SpacePermission.createUserSpacePermission(SpacePermission.REMOVE_MAIL_PERMISSION, space, user)
    spacePermissionManager.savePermission(spacePermissionToSave)
       
    spacePermissionToSave = SpacePermission.createUserSpacePermission(SpacePermission.REMOVE_BLOG_PERMISSION, space, user)
    spacePermissionManager.savePermission(spacePermissionToSave)
       
    spacePermissionToSave = SpacePermission.createUserSpacePermission(SpacePermission.REMOVE_COMMENT_PERMISSION, space, user)
    spacePermissionManager.savePermission(spacePermissionToSave)
  }
 
  return Response.ok().build();
}
