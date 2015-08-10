/*! Barlesque version 4, orb/api
 *  Copyright (c) 2014 BBC, all rights reserved.
 */
define("orb/async/_footerpromo",["orb/lib/_$"],function(e){"use strict";var t=3e3,n=function(t,n,r){return e.addCSS(n)&&e.addHTML(t,r)},r={load:function(r,i,s){var o=s.onSuccess||function(){},u=s.onError||function(){},a=s.onAlways||function(){},f,l;f=function(e){e&&e.status==="success"?n(i,e.style,e.html)?o(e):u():u(),a()},l={timeout:t,error:function(){u(),a()},callbackName:"navpromo"},e.script.jsonp(r,f,l)},_ioc:function(t){e=t.$}};return r});