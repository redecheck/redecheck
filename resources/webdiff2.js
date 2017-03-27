/*
 * WebDiff2 - Webdriver Version of WebDiff
 * @author: Shauvik Roy Choudhary
 * Copyright: Georgia Institute of Technology
 * License: MIT License
 */


/**
 * Code to get absolute coordinates of DOM element
 * @source: http://www.codeproject.com/KB/scripting/dom-element-abs-pos.aspx
 */
//START Absolute position code
function __getIEVersion() {
    var rv = -1; // Return value assumes failure.
    if (navigator.appName == 'Microsoft Internet Explorer') {
        var ua = navigator.userAgent;
        var re = new RegExp("MSIE ([0-9]{1,}[\.0-9]{0,})");
        if (re.exec(ua) != null)
            rv = parseFloat(RegExp.$1);
    }
    return rv;
}

function __getOperaVersion() {
    var rv = 0; // Default value
    if (window.opera) {
        var sver = window.opera.version();
        rv = parseFloat(sver);
    }
    return rv;
}

var __userAgent = navigator.userAgent;
var __isIE =  navigator.appVersion.match(/MSIE/) != null;
var __IEVersion = __getIEVersion();
var __isIENew = __isIE && __IEVersion >= 8;
var __isIEOld = __isIE && !__isIENew;

var __isFireFox = __userAgent.match(/firefox/i) != null;
var __isFireFoxOld = __isFireFox && ((__userAgent.match(/firefox\/2./i) != null) ||
	(__userAgent.match(/firefox\/1./i) != null));
var __isFireFoxNew = __isFireFox && !__isFireFoxOld;

var __isWebKit =  navigator.appVersion.match(/WebKit/) != null;
var __isChrome =  navigator.appVersion.match(/Chrome/) != null;
var __isOpera =  window.opera != null;
var __operaVersion = __getOperaVersion();
var __isOperaOld = __isOpera && (__operaVersion < 10);

function __parseBorderWidth(width) {
    var res = 0;
    if (typeof(width) == "string" && width != null && width != "" ) {
        var p = width.indexOf("px");
        if (p >= 0) {
            res = parseInt(width.substring(0, p));
        }
        else {
     		//do not know how to calculate other values
		//(such as 0.5em or 0.1cm) correctly now
    		//so just set the width to 1 pixel
            res = 1;
        }
    }
    return res;
}

//returns border width for some element
function __getBorderWidth(element) {
	var res = new Object();
	res.left = 0; res.top = 0; res.right = 0; res.bottom = 0;
	if (window.getComputedStyle) {
		//for Firefox
		var elStyle = window.getComputedStyle(element, null);
		res.left = parseInt(elStyle.borderLeftWidth.slice(0, -2));
		res.top = parseInt(elStyle.borderTopWidth.slice(0, -2));
		res.right = parseInt(elStyle.borderRightWidth.slice(0, -2));
		res.bottom = parseInt(elStyle.borderBottomWidth.slice(0, -2));
	}
	else {
		//for other browsers
		res.left = __parseBorderWidth(element.style.borderLeftWidth);
		res.top = __parseBorderWidth(element.style.borderTopWidth);
		res.right = __parseBorderWidth(element.style.borderRightWidth);
		res.bottom = __parseBorderWidth(element.style.borderBottomWidth);
	}

	return res;
}

//returns the absolute position of some element within document
function getElementAbsolutePos(element) {
	var res = new Object();
	res.x = 0; res.y = 0;
	if (element !== null) {
		if (element.getBoundingClientRect) {
			// var viewportElement = document.documentElement;
 	        var box = element.getBoundingClientRect();

		    // var scrollLeft = viewportElement.scrollLeft;
 		   //  var scrollTop = viewportElement.scrollTop;

		    res.x = box.left;
		     // + scrollLeft;
		    res.y = box.top;

			res.x2 = box.right;
			res.y2 = box.bottom;
		     // + scrollTop;

		}
		else { //for old browsers
			res.x = element.offsetLeft;
			res.y = element.offsetTop;

			var parentNode = element.parentNode;
			var borderWidth = null;

			while (offsetParent != null) {
				res.x += offsetParent.offsetLeft;
				res.y += offsetParent.offsetTop;

				var parentTagName =
					offsetParent.tagName.toLowerCase();

				if ((__isIEOld && parentTagName != "table") ||
					((__isFireFoxNew || __isChrome) &&
						parentTagName == "td")) {
					borderWidth = kGetBorderWidth
							(offsetParent);
					res.x += borderWidth.left;
					res.y += borderWidth.top;
				}

				if (offsetParent != document.body &&
				offsetParent != document.documentElement) {
					res.x -= offsetParent.scrollLeft;
					res.y -= offsetParent.scrollTop;
				}


				//next lines are necessary to fix the problem
				//with offsetParent
				if (!__isIE && !__isOperaOld || __isIENew) {
					while (offsetParent != parentNode &&
						parentNode !== null) {
						res.x -= parentNode.scrollLeft;
						res.y -= parentNode.scrollTop;
						if (__isFireFoxOld || __isWebKit)
						{
						    borderWidth =
						     kGetBorderWidth(parentNode);
						    res.x += borderWidth.left;
						    res.y += borderWidth.top;
						}
						parentNode = parentNode.parentNode;
					}
				}

				parentNode = offsetParent.parentNode;
				offsetParent = offsetParent.offsetParent;
			}
		}
	}
    return res;
}
//END Absolute position code


/**
 * Helper function to get an applied CSS style
 */
function getStyle(oElm, strCssRule){
	var strValue = "";
	if(document.defaultView && document.defaultView.getComputedStyle && oElm.nodeName!="#comment"){
		strValue = document.defaultView.getComputedStyle(oElm, "").getPropertyValue(strCssRule);
		}
	else if(oElm.currentStyle){
		strCssRule = strCssRule.replace(/\-(\w)/g, function (strMatch, p1){
			return p1.toUpperCase();
		});
		strValue = oElm.currentStyle[strCssRule];
	}
	return strValue;
}

/**
 * Returns the XPath of a DOM node
 * @param node
 * @returns {String}
 */
function getXPath(node){
    var path = "";
    for (; node && node.nodeType == 1; node = node.parentNode)
    {
  	var idx = getElementIdx(node);
	var xname = node.tagName;
	if (idx > 1) xname += "[" + idx + "]";
	  path = "/" + xname + path;
    }

    return path;
}

/**
 * Helper function for getXPath
 */
function getElementIdx(node){
    var count = 1;
    for (var sib = node.previousSibling; sib ; sib = sib.previousSibling){
        if(sib.nodeType == 1 && sib.tagName == node.tagName)	count++;
    }
    return count;
}

function getDOMCoords(node){
	try{
		var pos = getElementAbsolutePos(node);
		//var x = Math.round(pos.x);
		//var y = Math.round(pos.y);
		// var boxes = node.getClientRects();
		// console.log(boxes.length);
		var x = pos.x;
		var y = pos.y;
		var x2 = pos.x2;
		var y2 = pos.y2;
		//return "["+[x, y, x+node.offsetWidth, y+node.offsetHeight]+"]";
		return "["+[x, y, x2, y2]+"]";
	}catch(e){
	        return "["+[-1,-1,-1,-1]+"]";
	}
}

function getContentRectangle(node){
	try{
		var pos = getElementAbsolutePos(node);
		var win = document.defaultView || window, style, styleNode = [];
		var cs = win.getComputedStyle(node, null);

		// var paddingX = parseFloat(cs.paddingLeft) + parseFloat(cs.paddingRight);
		// var paddingY = parseFloat(cs.paddingTop) + parseFloat(cs.paddingButtom);
        //
		// var borderX = parseFloat(cs.borderLeftWidth) + parseFloat(cs.borderRightWidth);
		// var borderY = parseFloat(cs.borderTopWidth) + parseFloat(cs.borderBottomWidth);

		// Element width and height minus padding and border
		// elementWidth = element.offsetWidth - paddingX - borderX;
		// elementHeight = element.offsetHeight - paddingY - borderY;
		var contentX = pos.x + parseFloat(cs.paddingLeft) + parseFloat(cs.borderLeftWidth);
		var contentY = pos.y + parseFloat(cs.paddingTop) + parseFloat(cs.borderTopWidth);
		var contentX2 = pos.x2 - parseFloat(cs.paddingRight) - parseFloat(cs.borderRightWidth);
		var contentY2 = pos.y2 - parseFloat(cs.paddingBottom) - parseFloat(cs.borderBottomWidth);
		return "["+[contentX, contentY, contentX2, contentY2]+"]";
	} catch(e){
		return "["+[-1,-1,-1,-1]+"]";
	}
}

function isClickable(node){
	if(node.nodeType == 1){
		if(node && node.attributes){
			var attr = "";
			if(node.getAttribute('onclick')){
				return 1;
			}
		}
	}
	return 0;
}

function isVisible(node){
	if(node.nodeType!=1){
		return 0;
	}
	else if(node.nodeType==1){
		var style_opacity = getStyle(node,"opacity");
		var style_visiblity = getStyle(node,"visibility");
		var style_display = getStyle(node,"display");
		var style_overflow = getStyle(node,"overflow");
		//The check below is for IE
		if(typeof(style_opacity)=='undefined'){
			style_opacity = getStyle(node,"filter");
			//Comes back with nothing since its a layout element
			if(style_opacity.length==0){
				style_opacity=100;
			}
			else{
				style_opacity=style_opacity.substring((style_opacity.indexOf("=")+1),(style_opacity.length-1));
			}
		}
		if((style_opacity!='0') && (style_visiblity != 'hidden') && (style_display != 'none') && (style_visiblity != 'collapse')){
			return 1;
		}
		else{
			return 0;
		}
	}
	else{
		return 0;
	}
}

function getOverflow(node){
	if(node.nodeType!=1){
		return 0;
	}
	else if(node.nodeType==1) {
		var style_overflow = getStyle(node, "overflow");
		if(style_overflow!='hidden') {
			return 1;
		} else {
			return 0;
		}
	}
	return 0;
}
/**
 *
 * @param node
 */
function getZIndex(node){
	var style = getStyle(node,"z-index");
	var zIndexVal;
	if(style){
		zIndexVal = parseInt(style);
		if(isNaN(zIndexVal)){
			zIndexVal = 0;
		}
	}
	return zIndexVal;
}

/**
 * Checker function while populating JSON object
 * @param key
 * @param value
 * @param func Optional function to be applied to value
 */
function c(key, value, func, enclose){
  if(func && value) value = func.apply(null, [value]);
  if(value == undefined || value === '') return;
  if(enclose) return key + ":'" + value+"'";
  return key + ':' + value;
}

function getAttributes(attr){
	if(attr){
		var data = "{";
		for(var i=0;i<attr.length;i++){
			var at = attr[i];
			if(at.specified){
				var key = at.name;
				var val = encodeURIComponent(at.value);
				if(key.match(/[\w]+/)){
					data+="'"+key+"':'"+val.replace(/'/g,"\\'")+"',";
				}
			}
		}
		if(data.charAt(data.length-1) == ','){
			return data.substr(0, data.length-1)+'}';
		}
	}
	return "{}";
}

function getNodeValue(node){
	if(node){
		var val = encodeURIComponent(node.nodeValue);
		return "'"+val.replace(/'/g,"\\'")+"'";
	}
	return;
}


function getAllStyles(elem) {
	if (!elem) return []; // Element does not exist, empty list.
	var win = document.defaultView || window, style, styleNode = [];
	var styles = "{";

	if (win.getComputedStyle) { /* Modern browsers */
		style = win.getComputedStyle(elem, null);
		for (var i=0; i<style.length; i++) {
			styles += "'" + style[i] + "':'" + style.getPropertyValue(style[i]).replace(",", "") + "',";
		}
		if(styles.charAt(styles.length-1) == ','){
			return styles.substr(0, styles.length-1)+'}';
		}
	} else if (elem.currentStyle) { /* IE */
		style = elem.currentStyle;
		for (var name in style) {
			styleNode.push( name + ':' + style[name] );
			styles += style[i] + ':' + style[name];
		}
	} else { /* Ancient browser..*/
		style = elem.style;
		for (var i=0; i<style.length; i++) {
			styleNode.push( style[i] + ':' + style[style[i]] );
			styles += style[i] + ':' + style[style[i]];
		}
	}
	return "{}";
}




// var iframewindow= frames['main-frame'];
window.scrollTo(0, 0);

/**
 * Perform depth first traversal of DOM and return the DOM
 * data string in JSON format
 */
var data = '[';

var nodes = Array();
var nodeCtr=0;

var maxHeight = 0;

var toIgnore = ["A", "I", "G", "PATH", "AREA", "B", "BLOCKQUOTE",
	"BR", "CANVAS", "CENTER", "CSACTIONDICT", "CSSCRIPTDICT", "CUFON",
	"CUFONTEXT", "DD", "EM", "EMBED", "FIELDSET", "FONT",
	"HEAD", "HR", "IFRAME", "INS", "LEGEND", "LINK", "MAP", "MENUMACHINE",
	"META", "NOFRAMES", "NOSCRIPT", "OBJECT", "OPTGROUP", "OPTION",
	"PARAM", "S", "SCRIPT", "SMALL", "STRIKE", "STRONG",
	"STYLE", "TBODY", "TITLE", "TR", "TT", "U"];

//Process body nodes
var heads = document.getElementsByTagName("head");
if(heads && heads.length > 0){
  nodes.push([heads[0],-1]);
}
nodes.push([document.body,-1]);


while(nodes.length > 0){
  //process node
  var t = nodes.pop(), n = t[0], pid=t[1];
	console.log(getXPath(n));
  var nodeid = nodeCtr++;
  if(n.nodeName && n.nodeName == "#text"){
  }else if (toIgnore.indexOf(n.nodeName) == -1) {
	  // if (isVisible(n) === 1) {
		  var arr = [c('type',1),
			  c('nodeid', nodeid),
			  c('pid', pid),
			  c('xpath', n, getXPath, true),
			  c('visible', n, isVisible),
			  c('overflow', n, getOverflow),
			  c('coord', n, getDOMCoords)
			  // ,
			  // c('styles', n, getAllStyles)
		  ];


		  //filter out & add to data
		  var filtered = [];
		  for(var i in arr){
			  if(typeof arr[i] == "string"){
				  filtered.push(arr[i]);
			  }
		  }
		  data += 	'{'+ filtered.toString()+'},';
	  // }

  }


  //push children
  var cs = n.childNodes;

  for(var ch in cs){
    var child = cs[ch];
    if(child){
    	var nn = child.nodeName;
    	if(nn && nn != "#comment" && nn.charAt(0) != '/'){
    		nodes.push([child,nodeid]);
		}else if (toIgnore.indexOf(nn) != -1) {

			if (child.children.length > 0) {
				nodes.push([child, nodeid]);
			}
		}
    }
  }
}



data=data.substr(0,data.length-1)+']';
return data;