/**
 *	contextpath is derived from the url of index.html page.
 *  restpath can be manually modified. The default value is "/rest/"
 *  configpath can be manually modified. The default value is "_config"
 *  These three properties need to be in sync with their counterparts in the service virtualization server.
 */
var contextpath = location.pathname.replace(/\/$/, "");
var restpath = '/rest/';
var configpath= 'config';
/**
 * Invoked when the setup page opens.
 * @returns
 */
function init() {
	retrieveEntryPageURL();
	cleanupLocalStorage();

	//var customImageurl = "http://localhost:8080/banking/image.jsp";
	//setCookieAcrossDomains(customImageurl);
};

/**
 * Handles all AJAX call to webservicemockserver, using native XMLHttpRequest object instead of any library.
 */
function makeAjaxCall(requestURI, callback) {
	var xhr = new XMLHttpRequest();
	xhr.responseType = 'json';
	xhr.open('POST', requestURI, true);
	xhr.setRequestHeader('Content-type', 'application/json');
	xhr.onload = function() {
	    if (xhr.status === 200) {
	        
	    	console.debug("ajax call successful.");
	    	
	    	if(callback) {
	    		callback(xhr.response);
	        }
	    }
	    else {
	        console.debug("ajax call failed:" + xhr.status);
	    }
	};
	xhr.send();
};

/**
 * Retrieves entry page URL when the app is loaded.
 */
function retrieveEntryPageURL() {
	
	var entryPageUrlCookie = getCookie("entryPageUrl");
	
	if (!entryPageUrlCookie) {
		console.debug("entryPageUrl cookie is not loaded yet.");
		var uri = contextpath + restpath + configpath + "?retrieveEntryPageURL"
		makeAjaxCall(uri, retrieveEntryPageURLCallback);
	} else {
		retrieveEntryPageURLCallback({entryPageUrl: entryPageUrlCookie});
	}
};

/**
 * Updates the link entryPageUrl.
 * @param response
 * @returns
 */
function retrieveEntryPageURLCallback(response) {
	console.debug("retrieveEntryPageURLCallback is called:" + response);
	var link = document.getElementById("entryPageUrl");
	var entryPage  = response.entryPageUrl;
	link.href= entryPage;
	link.innerHTML= entryPage;
};

function cleanupLocalStorage() {
	localStorage.removeItem("mobilebanking");
};

/**
 * Selects flow and scenario, attaching a random parameter to avoid cache.
 * Opens mobile banking login page in a new browser tab if it is not open yet or closed.
 * 
 * @param uri
 * @returns
 */
function selectFlowScenario(query) {
	
	makeAjaxCall(contextpath + restpath + configpath + "?" +query + "&t=" + Math.random(), openInNewTab);
};

function getCookie(cookieName) {
	
	var cookieArray = document.cookie.split(';');
	
	for (var i = 0; i < cookieArray.length; i++) {
		var cookie = cookieArray[i].split("=");
		var name = cookie[0].trim();
		var value = cookie[1];
		
		if (value) {
			value = value.trim();
		}
				
		if (cookieName.indexOf(name) == 0) {
			return value;
		}
	}
	
	return "";
};

function openTab(evt, appName) {
    var i, tabcontent, tablinks;
    tabcontent = document.getElementsByClassName("tabcontent");
    for (i = 0; i < tabcontent.length; i++) {
        tabcontent[i].style.display = "none";
    }
    tablinks = document.getElementsByClassName("tablinks");
    for (i = 0; i < tablinks.length; i++) {
        tablinks[i].className = tablinks[i].className.replace(" active", "");
    }
    document.getElementById(appName).style.display = "block";
    evt.currentTarget.className += " active";
};

var childWin = null;

function openInNewTab() {
	
	// first check if mobile banking login page is open or not.
	var entryPage = localStorage.mobilebanking;
	var childWinOpen = childWin && !childWin.closed
	
	if (entryPage && childWinOpen) {
		// already open
		console.debug("mobile banking login page is open.");
		return;
	}
	
	// not open yet
	console.debug("mobile banking login page not open yet.");
	
	var link = document.getElementById("entryPageUrl");
	var entryPageUrl = link.href;
	childWin = window.open(entryPageUrl, '_blank');
	if (childWin) {
		childWin.focus();
	}
	
	localStorage.mobilebanking = "mobilebanking";
};

function setCookieAcrossDomains(customImageurl) {
	
	window.onload = function(){
		loadGraphics(customImageurl);
	};
};

function loadGraphics(customImageurl){
 	if (location.href.indexOf("?") > 0) {
     	document.getElementById("loadImage").src = customImageurl + location.href.substring(location.href.indexOf("?"));
 	}
};
 