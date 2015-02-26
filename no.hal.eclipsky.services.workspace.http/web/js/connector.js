var connector = (function() {
	var webSocket = null,
		url,
		
		// All subscribers need to implement a 'notify()'-function
		subscribers = [], 

		XHRpostfix = "&format=json";
	
	// Publish to all listeners
	function publish(data) {
		console.log('publishing: ', data);
		for (var i = subscribers.length - 1; i >= 0; i--) {
			subscribers[i].notify(data);
		}
	};
	
	
	// Private function for sending XHR requests
	function sendXHRdata(data) {
		var xmlHttp = new XMLHttpRequest();
		xmlHttp.open("POST", url + XHRPostfix, true);
		var startTime = new Date();
		xmlHttp.onreadystatechange = function () {
			if (xmlHttp.readyState == 4 && xmlHttp.status == 200) {
				publish(xmlHttp.responseText);
			}
		}
		xmlHttp.send(data);
	};
	
	function sendWSdata(data) {
		webSocket.send(data);
	};
	
	function initializeWebsocket(url) {
		var url = document.createElement('a');
		url.href = url;
		url.protocol = "ws";
	    webSocket = new WebSocket(url, "json");

	    webSocket.onerror = function(event) {
			// TODO: Reconnect?
	    	console.log('ws error', event);
	    	// publish(event);
	    };

	    webSocket.onclose = function(event) {
	    	// TODO: Notify?
	        console.log('ws close', event);
	        // publish(event);
	    };

	    webSocket.onmessage = function(event) {
	    	publish(event);
	    }
	};
	
	
	return {
		init : function(config) {
			url = config.url;
			initializeWebsocket(url);
		},

		send : function(data) {
			if (webSocket.readyState === webSocket.OPEN) {
				sendWSdata(data);
			} else {
				sendXHRdata(data);
			}
		},
		
		subscribe : function(subscriber) {
			subscribers.push(subscriber);
		},
		
		unsubscribe : function(subscriber) {
			for (var i = subscribers.length - 1; i >= 0; i--) {
				if (subscribers[i] === subscriber) {
					subscribers.splice(i, 1);
					return;
				}
			}
		}
	};
	
})();