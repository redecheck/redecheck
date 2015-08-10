/*! Barlesque version 4, orb/api
 *  Copyright (c) 2014 BBC, all rights reserved.
 */
define("orb/api",function(){"use strict";var e={layout:[]},t,n=window.orb.fig(),r={},i={layout:function(t){e.layout.push(t)},trigger:function(t,n){if(e[t])for(var r=0,i=e[t].length;r<i;r++)e[t][r](n)},config:function(e,t){if(arguments.length===0)return r;if(arguments.length===1)return r[e];r[e]=t}};return i});