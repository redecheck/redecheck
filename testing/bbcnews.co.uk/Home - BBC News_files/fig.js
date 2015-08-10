(function(){
var o={'uk':1,
'ck':1,
'ad':0,
'ap':0,
'tb':0,
'mb':0,
'eu':1};
window.orb=window.orb||{};
if (window.name.match(/ orb_fig_referrer=([^ ]+)$/)) {
    window.orb.referrer = decodeURIComponent(RegExp.$1);
    window.name = window.name.replace(/ orb_fig_referrer=([^ ]+)$/, '');
}

orb.fig=orb.fig||function(k){return (arguments.length)? o[k]:o};
window.define && define('orb/fig', o); })();

orb._clientsideRedirect=function(d,m){var h=false,j;m=m||window;j=(m.document.cookie.match(/ckps_d=(.)/)?RegExp.$1:"");if(orb._redirectionIsEnabled(m)&&orb._dependenciesSatisfied(d,m)){var n=(m.location.hostname||"").toLowerCase(),a=(m.location.href||""),i={isUk:/(^|\.)bbc\.co\.uk$/i.test(n),isInt:/(^|\.)bbc\.com$/i.test(n),isMb:/^m\./i.test(n),isDesk:/^(www|pal)\./i.test(n)},c={isUk:d("uk"),isMb:d("mb")},l,b,k=(m.document.referrer||"").toLowerCase();if(m.bbcredirection.geo===true){if(i.isInt===true&&c.isUk===1){window.name+=" orb_fig_referrer="+encodeURIComponent(document.referrer);b=a.replace(/^(.+?bbc)\.com/i,"$1.co.uk")}else{if(i.isUk===true&&c.isUk===0){window.name+=" orb_fig_referrer="+encodeURIComponent(document.referrer);b=a.replace(/^(.+?bbc)\.co\.uk/i,"$1.com")}}}l=(b||a);if(m.bbcredirection.device===true){if(i.isDesk===true&&(j==="m"||(!j&&c.isMb===1))){window.name+=" orb_fig_referrer="+encodeURIComponent(document.referrer);l=l.replace(/^(https?:\/\/)(www|pal)\./i,"$1m.")}else{if(i.isMb===true&&(j==="d"||(!j&&c.isMb===0))){window.name+=" orb_fig_referrer="+encodeURIComponent(document.referrer);l=l.replace(/^(https?:\/\/)m\./i,"$1www.")}}}if(l&&a.toLowerCase()!==l.toLowerCase()&&k!==l.toLowerCase()&&k!==a.toLowerCase()){h=true;try{m.location.replace(l)}catch(g){h=false;m.require(["istats-1"],function(e){e.log("redirection_fail","",{})})}}}return h};orb._redirectionIsEnabled=function(a){return(a.bbcredirection&&(a.bbcredirection.geo===true||a.bbcredirection.device===true))};orb._dependenciesSatisfied=function(b,a){return(typeof b==="function"&&typeof a.location.replace!=="undefined")};orb.fig.device={};orb.fig.geo={};orb.fig.user={};orb.fig.device.isTablet=function(){return window.orb.fig("no")?undefined:window.orb.fig("tb")};orb.fig.device.isMobile=function(){return window.orb.fig("no")?undefined:window.orb.fig("mb")};orb.fig.geo.isUK=function(){return window.orb.fig("no")?undefined:window.orb.fig("uk")};orb.fig.geo.isEU=function(){return window.orb.fig("no")?undefined:window.orb.fig("eu")};
    orb._clientsideRedirect(window.mockFig || window.orb.fig, window.mockWindow || window);
