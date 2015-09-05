simulator {
    // TODO: define status and reply messages here
}

tiles {

    valueTile("smoke", "device.smoke", width: 2, height: 2){
    	//state ("clear", label: "OK", unit: "Smoke", backgroundColor:"#44B621")
		//state ("detected", icon:"st.alarm.smoke.smoke", label:"SMOKE", backgroundColor:"#e86d13", unit: "Smoke")
		//state ("tested", label:"TEST", backgroundColor:"#003CEC", unit: "Smoke")
        state "smoke", label: 'Smoke ${currentValue}', unit:"smoke",
        	backgroundColors: [
                [value: "clear", color: "#44B621"],
                [value: "detected", color: "#e86d13"],
                [value: "tested", color: "#003CEC"]
            ]
	}
	valueTile("carbonMonoxide", "device.carbonMonoxide"){
    	//state("clear", backgroundColor:"#44B621", unit: "CO")
		//state("detected", label:"CO", icon:"st.alarm.smoke.smoke", backgroundColor:"#e86d13", unit: "CO")
		//state("tested", label:"TEST", icon:"st.alarm.smoke.test", backgroundColor:"#003CEC", unit: "CO")
        state("carbonMonoxide", label: 'CO ${currentValue}', unit:"CO", backgroundColors: [
                [value: "clear", color: "#44B621"],
                [value: "detected", color: "#e86d13"],
                [value: "tested", color: "#003CEC"]
            ]
        )
	}
    valueTile("battery", "device.battery"){
    	//state("OK", backgroundColor:"#44B621", unit: "Batt")
		//state("Low", label:"Low", backgroundColor:"#e86d13")
        state("battery", label: 'Battery ${currentValue}', unit:"battery", backgroundColors: [
                [value: "OK", color: "#44B621"],
                [value: "Low", color: "#e86d13"]
            ]
        )
	}
    standardTile("refresh", "device.smoke", inactiveLabel: false, decoration: "flat") {
        state "default", action:"polling.poll", icon:"st.secondary.refresh"
    }
	main "smoke"
    details(["smoke", "carbonMonoxide", "battery", "refresh"])
}
}

// parse events into attributes
def parse(String description) {

}

def auto() {
log.debug "Executing 'auto'"
}

def poll() {
log.debug "Executing 'poll'"
api('status', []) {
data.topaz = it.data.topaz.getAt(settings.mac.toUpperCase())

    data.topaz.smoke_status = data.topaz.smoke_status == 0? "clear" : "detected"
    data.topaz.co_status = data.topaz.co_status == 0? "clear" : "detected"
    data.topaz.battery_health_state = data.topaz.battery_health_state  == 0 ? "OK" : "Low"

    sendEvent(name: 'smoke', value: data.topaz.smoke_status)
    sendEvent(name: 'carbonMonoxide', value: data.topaz.co_status)
    sendEvent(name: 'battery', value: data.topaz.battery_health_state )
    log.debug settings.mac
    log.debug data.topaz.wifi_mac_address
    log.debug data.topaz.smoke_status
    log.debug data.topaz.co_status
    log.debug data.topaz.battery_health_state
}
}

def api(method, args = [], success = {}) {
if(!isLoggedIn()) {
log.debug "Need to login"
login(method, args, success)
return
}

def methods = [
    'status': [uri: "/v2/mobile/${data.auth.user}", type: 'get']
]

def request = methods.getAt(method)

log.debug "Logged in"
doRequest(request.uri, args, request.type, success)
}

// Need to be logged in before this is called. So don't call this. Call api.
def doRequest(uri, args, type, success) {
log.debug "Calling $type : $uri : $args"

if(uri.charAt(0) == '/') {
    uri = "${data.auth.urls.transport_url}${uri}"
}

def params = [
    uri: uri,
    headers: [
        'X-nl-protocol-version': 1,
        'X-nl-user-id': data.auth.userid,
        'Authorization': "Basic ${data.auth.access_token}"
    ],
    body: args
]

if(type == 'post') {
    httpPostJson(params, success)
} else if (type == 'get') {
    httpGet(params, success)
}
}

def login(method = null, args = [], success = {}) { 
def params = [
uri: 'https://home.nest.com/user/login',
body: [username: settings.username, password: settings.password]
]

httpPost(params) {response -> 
    data.auth = response.data
    data.auth.expires_in = Date.parse('EEE, dd-MMM-yyyy HH:mm:ss z', response.data.expires_in).getTime()
    log.debug data.auth

    api(method, args, success)
}
}

def isLoggedIn() {
if(!data.auth) {
log.debug "No data.auth"
return false
}

def now = new Date().getTime();
return data.auth.expires_in > now
}

1 Like

 
kmugh
Sep '14
@ecamodeo Seems to be working now! Many thanks.



2 MONTHS LATER

wakkigy
Samuel Wong
Nov '14
Found two things with the device

None of the icons show up on the ST app (all are just green circles with -- in the middle)
After adding the device and it shows up with the correct status, is that suppose to be updated in the activity?


 
NorCalLights
Paul
Nov '14
Just curious... Is it possible to use the wired Nest Protects as motion detectors too?



 
beckwith
Nov '14
NorCalLights:
Is it possible to use the wired Nest Protects as motion detectors too?
Nest has indicated they don't intend on exposing the motion detection through their API.



 
tdh
Dustin H
Nov '14   
I too am having the same issue of

wakkigy:
None of the icons show up on the ST app (all are just green circles with -- in the middle)
You state:

wakkigy:
After adding the device and it shows up with the correct status, is that suppose to be updated in the activity?
So I'm wondering what I need to do in order to get it working...?



 
tdh
Dustin H
Nov '14   
Is there a way we can get all this code put into one Code section? It's broken up into some code tags here within the post and some of it isn't. Is the stuff that is in code blocks the stuff that is different from the original post?

Thanks!



 
NorCalLights
Paul
Nov '14   
Thanks for the information, @beckwith



8 DAYS LATER

Heather_A
Nov '14   
I just can not get this device type to work. I've got the devices created, the device type created using this code and I just get an Unknown thing in my thing list for each of my protects (I created 3 devices)

I was thinking it was the MAC address field causing the issue. I've tried both MAC addresses I grab from the Nest Website as well as the serial number for the protect. No luck

Anyone have any suggestions?



 
Heather_A
Nov '14 1 
Update... this morning my app has changed from Unknown for the Protects to the circles with -- no info on the Protects though. So still not sure where to go from here.

Update2... I re-copied the code above and re-pasted it into my device type and it seemed to fix things and now my protects are showing in the app. smile



 
wakkigy
Samuel Wong
Nov '14
When you say its showing in app, do you mean its showing as Damage & Danger dashboard? Were you able to resolve the image issue with the dashes?



 
korban_hadley
Helpful
Nov '14
I have the same issue. All I see is a green circle with --. I have tried copying the code a few times. I double checked the Mac address as well. I waited 24 hours to see if it would refresh or change. Under activity I am still not getting any updates.



 
friedpope
Nov '14 2 
I'm stuck at the -- display issue too. I think there might be something wrong with the tile definition, I already noticed a missing parenthesis and corrected that but still no luck. I'm planning on devoting a 1/2 hour or so to walking through the code, I'll post an update if I find a fix.

Just as a side note, has anyone tested a protect with this device type to see if they actually send an alert to smartthings when set off?

Update: So more than 1/2 hour later, 3 actually... I have no idea what the problem is. I can see that the data is being updated as the timestamp from Nest changes in data.topaz. ST just seems to think the device is in some sort of "INACTIVE" state, perhaps that's why we see -- where the current data should be.



 
tracstarr
Nov '14
@friedpope I've been doing the same. I've managed to get it working in the simulator, changing colors and display text, but as a real device...nodda. It's very frustrating.



17 DAYS LATER

john_mielko
Dec '14   
great job.. thank you



 
cyberhiker
Chris Burton
Dec '14
I have the -- happening as well. Just so that I am clear, which MAC are we supposed to put in?



 
CudaNet
Cuda
Dec '14
On my Android (S4), I'm exhibiting the same behavior as the rest of the community. However If I install the app on my iPad, voila - works just fine. Hmm ....



