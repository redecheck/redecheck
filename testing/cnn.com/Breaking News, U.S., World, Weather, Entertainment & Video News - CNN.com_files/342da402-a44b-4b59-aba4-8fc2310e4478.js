// Copyright 2006-2015 ClickTale Ltd., US Patent Pending
// Generated on: 5/7/2015 10:03:04 AM (UTC 5/7/2015 3:03:04 PM)

if (typeof(ct_dispatcher) == 'undefined')
{
	ct_dispatcher = {
		configName : null,
		cookieName : "ct_configName",
		getParameterByName : function (name)
		{
			 name = name.replace(/[\[]/, "\\\[").replace(/[\]]/, "\\\]");
			 var regexS = "[\\?&]" + name + "=([^&#]*)";
			 var regex = new RegExp(regexS, "i");
			 var results = regex.exec(window.location.search);
			 if(results == null)
			   return "";
			 else
			   return decodeURIComponent(results[1].replace(/\+/g, " "));
		},
		createCookie: function (name,value,days) 
		{
			if (days) 
			{
				var date = new Date();
				date.setTime(date.getTime( )+( days*24*60*60*1000));
				var expires = "; expires="+date.toGMTString( );
			}
			else var expires = "";
			document.cookie = name+"="+value+expires+"; path=/";
		},
		readCookie : function (name) 
		{
			var nameEQ = name + "=";
			var ca = document.cookie.split( ';');
			for( var i=0;i < ca.length;i++) 
			{
				var c = ca[i];
				while ( c.charAt( 0)==' ') c = c.substring( 1,c.length);
				if ( c.indexOf( nameEQ) == 0) return c.substring( nameEQ.length,c.length);
			}
			return null;
		}
	};
		
	// Read from querystring
	var ct_pdc_qs_val = ct_dispatcher.getParameterByName(ct_dispatcher.cookieName);
	if (ct_pdc_qs_val)
	{
		// Override/create cookie
		ct_dispatcher.createCookie(ct_dispatcher.cookieName, ct_pdc_qs_val, 14);
		ct_dispatcher.configName = ct_pdc_qs_val;
	}
	else
	{
		// Read from cookie
		ct_dispatcher.configName = ct_dispatcher.readCookie(ct_dispatcher.cookieName);
	}

	
}

	if (typeof (ClickTaleCreateDOMElement) != "function")
{
	ClickTaleCreateDOMElement = function(tagName)
	{
		if (document.createElementNS)
		{
			return document.createElementNS('http://www.w3.org/1999/xhtml', tagName);
		}
		return document.createElement(tagName);
	}
}

if (typeof (ClickTaleAppendInHead) != "function")
{
	ClickTaleAppendInHead = function(element)
	{
		var parent = document.getElementsByTagName('head').item(0) || document.documentElement;
		parent.appendChild(element);
	}
}

if (typeof (ClickTaleXHTMLCompliantScriptTagCreate) != "function")
{
	ClickTaleXHTMLCompliantScriptTagCreate = function(code)
	{
		var script = ClickTaleCreateDOMElement('script');
		script.setAttribute("type", "text/javascript");
		script.text = code;
		return script;
	}
}	
		var configFoundPHC = false;
	
if (ct_dispatcher.configName == 'Release_24032015')
{
	configFoundPHC = true;
			(function(){
	var script = ClickTaleXHTMLCompliantScriptTagCreate("\/\/ Copyright 2006-2015 ClickTale Ltd., US Patent Pending\r\n\/\/ PID: 49\r\n\/\/ Generated on: 5\/7\/2015 10:03:04 AM (UTC 5\/7\/2015 3:03:04 PM)\r\n\r\nvar WRInitTime=(new Date()).getTime();\r\n\r\n\/\/ Start of custom user-defined settings code\r\n\r\n\/\/ End of custom user-defined settings code\r\n\r\n");
	ClickTaleAppendInHead(script);	})();
	}
			
	
if (ct_dispatcher.configName == 'Release_06042015')
{
	configFoundPHC = true;
			(function(){
	var script = ClickTaleXHTMLCompliantScriptTagCreate("\/\/ Copyright 2006-2015 ClickTale Ltd., US Patent Pending\r\n\/\/ PID: 49\r\n\/\/ Generated on: 5\/7\/2015 10:03:04 AM (UTC 5\/7\/2015 3:03:04 PM)\r\n\r\nvar WRInitTime=(new Date()).getTime();\r\n\r\n\/\/ Start of custom user-defined settings code\r\n\r\n\/\/ End of custom user-defined settings code\r\n\r\n");
	ClickTaleAppendInHead(script);	})();
	}
			
	
if (ct_dispatcher.configName == 'dev_CNNF-18')
{
	configFoundPHC = true;
			(function(){
	var script = ClickTaleXHTMLCompliantScriptTagCreate("\/\/ Copyright 2006-2015 ClickTale Ltd., US Patent Pending\r\n\/\/ PID: 49\r\n\/\/ Generated on: 5\/7\/2015 10:03:04 AM (UTC 5\/7\/2015 3:03:04 PM)\r\n\r\nvar WRInitTime=(new Date()).getTime();\r\n\r\n\/\/ Start of custom user-defined settings code\r\n\r\n\/\/ End of custom user-defined settings code\r\n\r\n");
	ClickTaleAppendInHead(script);	})();
	}
			
	

	// Default configuration
if (!configFoundPHC)
{
			(function(){
	var script = ClickTaleXHTMLCompliantScriptTagCreate("\/\/ Copyright 2006-2015 ClickTale Ltd., US Patent Pending\r\n\/\/ PID: 49\r\n\/\/ Generated on: 5\/7\/2015 10:03:04 AM (UTC 5\/7\/2015 3:03:04 PM)\r\n\r\nvar WRInitTime=(new Date()).getTime();\r\n\r\n\/\/ Start of custom user-defined settings code\r\n\r\n\/\/ End of custom user-defined settings code\r\n\r\n");
	ClickTaleAppendInHead(script);	})();
	}

