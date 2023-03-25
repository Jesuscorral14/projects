import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.context.GlobalIssueContext
import com.atlassian.servicedesk.api.requesttype.RequestTypeService
import groovy.json.JsonSlurper
import com.atlassian.jira.workflow.WorkflowTransitionUtil
import com.atlassian.jira.util.JiraUtils
import com.atlassian.jira.util.json.JSONObject
import com.atlassian.jira.issue.CustomFieldManager
 
def userManager = ComponentAccessor.getUserManager()
def requestTypeService = ComponentAccessor.getOSGiComponentInstanceOfType(RequestTypeService)
def robotUser = userManager.getUserByName("automationbot")
def reqQ = requestTypeService.newQueryBuilder().issue(issue.id).build()
def reqT = requestTypeService.getRequestTypes(robotUser, reqQ)
def requestTypeName = reqT.getResults()[0].getName()
 
def url = "http://confluence/rest/api/space"
 
def basicAuth = "Basic <CREDENTIALS HERE>" //input base64 encoded credentials after the space
def connection = url.toURL().openConnection()
def customFieldManager = ComponentAccessor.getCustomFieldManager()
 
if (issue.issueType.name != "New Project" || requestTypeName != "Add a new Space in Confluence"){
    log.warn("Not requesting a new space")
    return
}
 
connection.setRequestProperty("Authorization", basicAuth)
connection.connect()
 
//Use the selected space to to find the space key from the rest call return.
def json = new JsonSlurper().parseText(connection.getInputStream().getText())
def spaceKey = issue.getCustomFieldValue(customFieldManager.getCustomFieldObject("customfield_XXXXX"))
 
def groupManager = ComponentAccessor.getGroupManager()
  
  def permissions    
          
  permissions = "&view=true&removeOwn=true&createEditPage=true&removePage=true&editBlog=true&createAttachment=true&removeAttachment=true&comment=true&setPagePermissions=true&exportSpace=true"             
 
 //Get the selected users to apply the permissions to.   
  def userField = customFieldManager.getCustomFieldObject("customfield_XXXXX")   
  def users = issue.getCustomFieldValue(userField)
  def commentFlag = 0
  //Loop through the users and make the rest call to the rest end point we created in Confluence.   
  users.each { user ->                
    url = "http://confluence/rest/scriptrunner/latest/custom/updatePermissions?" + "spaceKey=" + spaceKey + "&user=" + user.getUsername() + permissions       
    connection = url.toURL().openConnection()       
    connection.setRequestProperty("Authorization", basicAuth)       
    connection.connect()                
    //Log an error in if the return code is not 200.       
    if(connection.getResponseCode().equals(200) && commentFlag == 0) //Create comment for manual error handling and team notification      
    {   
        commentFlag = 1
        final boolean dispatchEvent = true
        final String commentBody = """Permissions for the space were updated appropriately!  All users specified should have admin privileges."""
 
        ComponentAccessor.commentManager.create(issue, robotUser, commentBody, dispatchEvent)
    }
      else if (connection.getResponseCode() != 200 && commentFlag == 0){ //transition the issue to approved status if response code is 200
          
        commentFlag = 1
        final boolean dispatchEvent = true
        final String commentBody = """There was an issue updating the permissions.  Please verify the space was created and check the error logs for details"""
 
        ComponentAccessor.commentManager.create(issue, robotUser, commentBody, dispatchEvent)
      }
  }
