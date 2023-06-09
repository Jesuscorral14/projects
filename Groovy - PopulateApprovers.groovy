import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.event.type.EventDispatchOption
import com.atlassian.jira.user.ApplicationUser
import com.atlassian.jira.bc.user.search.UserSearchService
import com.atlassian.jira.issue.MutableIssue
import com.onresolve.scriptrunner.runner.customisers.PluginModule
import com.onresolve.scriptrunner.runner.customisers.WithPlugin
import com.akelesconsulting.jira.plugins.rest.LookupService
import org.apache.log4j.Logger
import org.apache.log4j.Level
import groovy.json.JsonSlurper
import groovy.json.JsonOutput
import groovyx.net.http.ContentType
import groovyx.net.http.HttpResponseDecorator
import groovyx.net.http.RESTClient
import com.atlassian.jira.component.ComponentAccessor
@WithPlugin("com.akelesconsulting.jira.plugins.LookupManager")
@PluginModule
LookupService lookupService
 
def cfManager = ComponentAccessor.getCustomFieldManager()
def issueManager = ComponentAccessor.getIssueManager()
def userManager = ComponentAccessor.getUserManager()
def userSearchService = ComponentAccessor.getComponent(UserSearchService)
def watcherManager = ComponentAccessor.getWatcherManager()
def user = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser()
def changeType = cfManager.getCustomFieldObject(XXXXX) //get Change type field
def approvers = cfManager.getCustomFieldObject(XXXXX) //get approvers field
def peerRev = cfManager.getCustomFieldObject(XXXXX) //get User(s) field
def checkbox = cfManager.getCustomFieldObject(XXXXX) // get the DML/DDL field
def checkboxVal = issue.getCustomFieldValue(checkbox).toString()
def changeTypeVal = issue.getCustomFieldValue(changeType).toString()
def peerRevVal = issue.getCustomFieldValue(peerRev) as List
ApplicationUser peerRevUser
 
final externalUrl = "<application URL>"
 
// If the API accepts different types then you can change this and format the data accordingly
def body = "{NTID:\"${issue.reporter.name}\"}"
 
def postResponse = post(externalUrl, "applicationEndpoint", body) //Defines the URL to POST to
def SUPapprover
// If GET response is successful then the response can be cast to a Map which will allow to interact with the data
if (postResponse) {
 
    def responsePostMap = postResponse as Map
    responsePostMap.each {element ->
        if(element.toString().contains("ManagerNT") || element.toString().contains("SupervisorNT")){ //Pulls only the value of the direct supervisor NTID
            SUPapprover = element.toString().split("=")[1] != null ? element.toString().split("=")[1] : SUPapprover
        }
    }
}
 
peerRevUser = peerRevVal ? userManager.getUserByName(peerRevVal.first().name.toString()) : null //if peerRevVal exists, get User selected as applicationUser, otherwise null
ApplicationUser SUPapproverUser = SUPapprover ? userManager.getUserByName(SUPapprover.toString()) : null //Defines variable for supervisor approver retrieved from API Call
// Integration with lookup manager table
def table = "PLASMA Approvers CPM" //define table we're pulling
int tableId = lookupService.getTableIdByName(table) // Get table id by name
Set appr = [] //define appr set
Collection<ApplicationUser> usersList = []
def plasmaApp = lookupService.getTableEntriesById(tableId) //pull table based on given tableID
def parsedplasmaApp = new JsonSlurper().parseText( plasmaApp ) //parse returned results from plasmaApp
Set changeTypeApp = []
Set hotfixApp = []
if(checkboxVal.contains("None") || !checkboxVal){ //checkboxVal does not equal true
    parsedplasmaApp.each{
        if(it."219" == changeTypeVal && changeTypeVal != "Emergency" && it."220" != "DML/DDL"){ //Pulls the value selected for Change type and uses that to pull approvers.  Ignores emergency selection
            changeTypeApp.add(it."221")
        }
        else if(changeTypeVal == "Emergency" && it."219".toLowerCase() == "emergency" && it."220" != "DML/DDL"){ //if the Change type selected is Emergency, it will pull all of the appropriate users from the table
            hotfixApp.add(it."221")
        }
    } 
}
else if(checkboxVal.contains("DML") || checkboxVal.contains("DDL")){ //checkbox app is true and DML/DDL is required
    parsedplasmaApp.each{
        if(it."219" == changeTypeVal && changeTypeVal != "Emergency"){ //Pulls the value selected for Change type and uses that to pull approvers.  Ignores emergency selection
            changeTypeApp.add(it."221")
        }
        else if(changeTypeVal == "Emergency" && it."219".toLowerCase() == "emergency"){ //if the Change type selected is Emergency, it will pull all of the appropriate users from the table
            hotfixApp.add(it."221")
        }
    } 
}
if(hotfixApp && peerRevUser){ //if the hotfixApp array exists, it will use this line to add the approvers
    appr = hotfixApp + appr
    appr.each {
        def tempUser = userManager.getUserByName(it.toString())
        usersList.add(tempUser)
    }
    SUPapprover ? usersList.add(SUPapproverUser) : usersList //does SUPapprover exist? if so, add that user to the list, otherwise leave it alone
    peerRevUser ? usersList.add(peerRevUser) : usersList //Sets peer reviewer selected by developer if it exists otherwise the list stays the same
    issue.setCustomFieldValue(approvers, usersList) //Set values in approvers custom field
    issueManager.updateIssue(user, issue, EventDispatchOption.ISSUE_UPDATED, false) //persistence
}
else{ //if the hotfixApp array doesn't exist, it will use this line to set the approvers
    appr = changeTypeApp + appr
    appr.each {
        def tempUser = userManager.getUserByName(it.toString())
        usersList.add(tempUser)
    }
    SUPapprover ? usersList.add(SUPapproverUser) : usersList
    peerRevUser ? usersList.add(peerRevUser) : usersList //Sets peer reviewer selected by developer if it exists otherwise the list stays the same
    issue.setCustomFieldValue(approvers, usersList) //Set values in approvers custom field
    issueManager.updateIssue(user, issue, EventDispatchOption.ISSUE_UPDATED, false) //persistence
}
def post(def hostUrl, def endpoint, def bodyJson) { //function created to perform REST API call to SOI server
    def client = new RESTClient(hostUrl)
    client.setHeaders([
            'Content-Type': 'application/json'
            // If authentication mechanism of the API requires a special header, it can be added here
    ])
    client.handler.success = { HttpResponseDecorator response, json ->
        json
    }
    client.handler.failure = { HttpResponseDecorator response ->
        // Failure can be handled here
        log.error response.entity.content.text
        [:]
    }
    client.post(
            path: endpoint,
            contentType: ContentType.JSON,
            body: bodyJson
    )
}
