#CURRENTLY RATE LIMITED SINCE I HAVE NOT PURCHASED THE PREMIUM SUBSCRIPTION
#WAITING FOR PROOF OF CONCEPT WITH NECESSARY STEPS BEFORE PURCHASING

from polygon import RESTClient
import requests
from datetime import date, timedelta
import json

client = RESTClient(api_key="")

tickers = ["AAPL"] #Going to be used for multiple calls.  Rate limited right now so other queries stop working when I use a list
ticker = "AAPL"

#List Aggregates (Bars)
today = date.today()
yesterday = today - timedelta(days = 1) #necessary because we don't have real time data.  Need to pull from yesterday to get a response
#for ticker in range(1):
bars = client.get_aggs(ticker=ticker, multiplier=1, timespan="day", from_=yesterday, to=yesterday)
for bar in bars:
    print(bar)
        

headers = {"Authorization" : "Bearer <API KEY HERE>"}



BaseURL = 'https://api.polygon.io'
#------------------SMAStock parameters----------------------------USED TO IDENTIFY OUTLIERS IN THE STOCK PRICE AT MARKET CLOSE
stockTicker = ticker
SMAStock = f'/v1/indicators/sma/{stockTicker}'

#------------------optionsAggregates parameters---------------------------- USE ONCE AN OPTION TICKER IS IDENTIFIED AND MORE INFO IS REQUIRED
optionsTicker = "O:SPY251219C00650000"
multiplier = 3
timespan = "day"
fromDate = today
toDate = today
optionsAggregates = f'/v2/aggs/ticker/{optionsTicker}/range/{multiplier}/{timespan}/{fromDate}/{toDate}'
#------------------optionsContracts parameters---------------------------- CAN PULL OPTIONS TICKER FOR RE-USE LATER
underlyingTicker = "AAPL"
strikePrice = 0
optionsContracts = f'/v3/reference/options/contracts?underlying_ticker={underlyingTicker}&conract_type=call&strike_price={strikePrice}'
#------------------RSIOptions parameters----------------------------
RSIOptions = f'/v1/indicators/rsi/{optionsTicker}'

#------------------optionsChain parameters----------------------------
underlyingAsset = ticker
optionsChain = f'/v3/snapshot/options/{underlyingAsset}'

#------------------API Call function-----------------------------------------
def apiCall(api):
    response = requests.get(BaseURL + api, headers=headers)
    jsonBody = json.loads(response.content.decode("utf-8"))
    prettyJson = json.dumps(jsonBody, indent = 2)
    return(prettyJson)

optionsContractsVal = apiCall(optionsContracts)
print(optionsContractsVal)
#Options Contracts https://polygon.io/docs/options/get_v3_reference_options_contracts to pull weird option ticker
# Won't be able to query the URL option without the correct ticker e.g. the optionsTicker variable on ln 20, can pull from query above
#Need to figure out how to query options contracts based on volume.  Thought process is as follows:
    #Determine highly traded stocks with above average volume
    #Get their respective tickers and store them in a array
    #Iterate through the array and query those tickers using the Options Contracts API 
        #/v3/reference/options/contracts
    #Find options weekly(?) options that are trading at a higher than normal rate i.e. 2000+ purchases
    #Pull calls and puts with strike prices above/below current trading price respectively
    #Potentially also compare against Relative Strength Index (RSI) with this api /v1/indicators/rsi/{optionsTicker}
        #More reading material about RSI https://www.fidelity.com/learning-center/trading-investing/technical-analysis/technical-indicator-guide/RSI#:~:text=The%20Relative%20Strength%20Index%20(RSI,and%20oversold%20when%20below%2030.
    #Start small maybe with a fixed set of tickers?  Big ones like AAPL, NVDA, AMD, META, etc?
    #Also use other technical indicators like SMA and EMA https://polygon.io/docs/options/get_v1_indicators_sma__optionsticker
