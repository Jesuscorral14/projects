import matplotlib.pyplot as plt
import numpy as np
import requests
import json
import pandas as pd

api_url = "<jira URL>"
headers =  {"Content-Type":"application/json", "Authorization":"Basic <credentials here>"}
payload = { #payload to pull expected results from Jira.  Should only pull the completed items from last year
    "jql": "project = teamPROJ and resolution is NOT EMPTY and created >='2022/01/01' and resolved <= '2022/12/31'",
    "startAt": 0,
    "maxResults": -1,
    "fields": [
        "summary",
        "status",
        "assignee"
    ]
}
response = requests.post(api_url, headers=headers, json = payload) #post function to execute API call
responseBody = response.json()
jsonBody = json.loads(response.content.decode("utf-8")) #saves the response into a JSON format that can be easily parsed

keys = []
status = []
for x in range(len(jsonBody["issues"])): #loops through the number of issues pulled
   keys.append(jsonBody["issues"][x]["key"])
   status.append(jsonBody["issues"][x]["fields"]["status"]["name"])

total = dict(zip(keys, status))
totalArray = np.array(total)

plt.hist(status)
plt.show()
plt.clf()
