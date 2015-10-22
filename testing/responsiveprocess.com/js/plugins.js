
// usage: log('inside coolFunc', this, arguments);
window.log = function(){
log.history = log.history || [];   // store logs to an array for reference
log.history.push(arguments);
if(this.console) {
arguments.callee = arguments.callee.caller;
console.log( Array.prototype.slice.call(arguments) );
}
};
(function(b){function c(){}for(var d="assert,count,debug,dir,dirxml,error,exception,group,groupCollapsed,groupEnd,info,log,markTimeline,profile,profileEnd,time,timeEnd,trace,warn".split(","),a;a=d.pop();)b[a]=b[a]||c})(window.console=window.console||{});


/*
 * jQuery Responsive menu plugin by Matt Kersley
 * Converts menus into a select elements for mobile devices and low browser widths
 * github.com/mattkersley/Responsive-Menu
 */
(function(b){var c=0;b.fn.mobileMenu=function(g){function f(a){return a.attr("id")?b("#mobileMenu_"+a.attr("id")).length>0:(c++,a.attr("id","mm"+c),b("#mobileMenu_mm"+c).length>0)}function h(a){a.hide();b("#mobileMenu_"+a.attr("id")).show()}function k(a){if(a.is("ul, ol")){var e='<select id="mobileMenu_'+a.attr("id")+'" class="mobileMenu">';e+='<option value="">'+d.topOptionText+"</option>";a.find("li").each(function(){var a="",c=b(this).parents("ul, ol").length;for(i=1;i<c;i++)a+=d.indentString;
c=b(this).find("a:first-child").attr("href");a+=b(this).clone().children("ul, ol").remove().end().text();e+='<option value="'+c+'">'+a+"</option>"});e+="</select>";a.parent().append(e);b("#mobileMenu_"+a.attr("id")).change(function(){var a=b(this);if(a.val()!==null)document.location.href=a.val()});h(a)}else alert("mobileMenu will only work with UL or OL elements!")}function j(a){b(window).width()<d.switchWidth&&!f(a)?k(a):b(window).width()<d.switchWidth&&f(a)?h(a):!(b(window).width()<d.switchWidth)&&
f(a)&&(a.show(),b("#mobileMenu_"+a.attr("id")).hide())}var d={switchWidth:768,topOptionText:"Select a page",indentString:"&nbsp;&nbsp;&nbsp;"};return this.each(function(){g&&b.extend(d,g);var a=b(this);b(window).resize(function(){j(a)});j(a)})}})(jQuery);



/*
 * jQuery Extended Selectors plugin. (c) Keith Clark freely distributable under the terms of the MIT license.
 * Adds missing -of-type pseudo-class selectors to jQuery 
 * github.com/keithclark/JQuery-Extended-Selectors  -  twitter.com/keithclarkcouk  -  keithclark.co.uk
 */
(function(g){function e(a,b){for(var c=a,d=0;a=a[b];)c.tagName==a.tagName&&d++;return d}function h(a,b,c){a=e(a,c);if(b=="odd"||b=="even")c=2,a-=b!="odd";else{var d=b.indexOf("n");d>-1?(c=parseInt(b,10)||parseInt(b.substring(0,d)+"1",10),a-=(parseInt(b.substring(d+1),10)||0)-1):(c=a+1,a-=parseInt(b,10)-1)}return(c<0?a<=0:a>=0)&&a%c==0}var f={"first-of-type":function(a){return e(a,"previousSibling")==0},"last-of-type":function(a){return e(a,"nextSibling")==0},"only-of-type":function(a){return f["first-of-type"](a)&&
f["last-of-type"](a)},"nth-of-type":function(a,b,c){return h(a,c[3],"previousSibling")},"nth-last-of-type":function(a,b,c){return h(a,c[3],"nextSibling")}};g.extend(g.expr[":"],f)})(jQuery);



/*! http://mths.be/placeholder v1.8.5 by @mathias */
(function(g,a,$){var f='placeholder' in a.createElement('input'),b='placeholder' in a.createElement('textarea');if(f&&b){$.fn.placeholder=function(){return this};$.fn.placeholder.input=$.fn.placeholder.textarea=true}else{$.fn.placeholder=function(){return this.filter((f?'textarea':':input')+'[placeholder]').bind('focus.placeholder',c).bind('blur.placeholder',e).trigger('blur.placeholder').end()};$.fn.placeholder.input=f;$.fn.placeholder.textarea=b;$(function(){$('form').bind('submit.placeholder',function(){var h=$('.placeholder',this).each(c);setTimeout(function(){h.each(e)},10)})});$(g).bind('unload.placeholder',function(){$('.placeholder').val('')})}function d(i){var h={},j=/^jQuery\d+$/;$.each(i.attributes,function(l,k){if(k.specified&&!j.test(k.name)){h[k.name]=k.value}});return h}function c(){var h=$(this);if(h.val()===h.attr('placeholder')&&h.hasClass('placeholder')){if(h.data('placeholder-password')){h.hide().next().show().focus().attr('id',h.removeAttr('id').data('placeholder-id'))}else{h.val('').removeClass('placeholder')}}}function e(){var l,k=$(this),h=k,j=this.id;if(k.val()===''){if(k.is(':password')){if(!k.data('placeholder-textinput')){try{l=k.clone().attr({type:'text'})}catch(i){l=$('<input>').attr($.extend(d(this),{type:'text'}))}l.removeAttr('name').data('placeholder-password',true).data('placeholder-id',j).bind('focus.placeholder',c);k.data('placeholder-textinput',l).data('placeholder-id',j).before(l)}k=k.removeAttr('id').hide().prev().attr('id',j).show()}k.addClass('placeholder').val(k.attr('placeholder'))}else{k.removeClass('placeholder')}}}(this,document,jQuery));


/*
 *	Page Scroller - jQuery Plugin (minified)
 *	Simple plugin to create a smooth scrolling page with updating links
 *
 *	Support at: http://dairien.com
 *
 *	Copyright (c) 2012 Dairien Boyd. All Rights Reserved
 *
 *	Version: BETA - IE Still Unsupported (2/2/2012)
 *	Requires: jQuery v1.3+
 *
 *	This library is released under the BSD license:
 *	
 *	Redistribution and use in source and binary forms, with or without
 *	modification, are permitted provided that the following conditions are met:
 *	
 *	Redistributions of source code must retain the above copyright notice, this
 *	list of conditions and the following disclaimer. Redistributions in binary
 *	form must reproduce the above copyright notice, this list of conditions and
 *	the following disclaimer in the documentation and/or other materials
 *	provided with the distribution. Neither the name Dairirien Boyd nor
 *	the names of its contributors may be used to endorse or promote products
 *	derived from this software without specific prior written permission. 
 *	
 *	THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 *	AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 *	IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 *	ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
 *	ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 *	DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 *	SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 *	CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 *	LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 *	OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
 *	DAMAGE.
 *
 */

eval(function(p,a,c,k,e,d){e=function(c){return(c<a?'':e(parseInt(c/a)))+((c=c%a)>35?String.fromCharCode(c+29):c.toString(36))};if(!''.replace(/^/,String)){while(c--){d[e(c)]=k[c]||e(c)}k=[function(e){return d[e]}];e=function(){return'\\w+'};c=1};while(c--){if(k[c]){p=p.replace(new RegExp('\\b'+e(c)+'\\b','g'),k[c])}}return p}('7 2={};(3(c){c.1i.J({2:3(f){f=c.J({o:0,F:"1g",q:"1e",U:1c,l:15,C:[],S:3(){},V:3(){},Z:3(){}},f);k=3(d,a){c.1a.19=5;d.E(\'<I B="k"></I>\');2.r=c(".k",d);2.r.D("17");2.6=c("."+a.F,d);2.K=c(16);2.s=c(11);2.L=c("X");2.n=2.s.w();2.o=2.s.T();2.4=a;d.H({M:"R"});2.6.P(3(b){7 d=c(x);d.H({M:"R",1b:0});d.D(2.4.F+"G"+(b+1));2.4.C.j?2.r.E(\'<u B="\'+a.q+" "+a.q+"G"+(b+1)+\'"><a Q="#k\'+b+\'">\'+2.4.C[b]+"</a></u>"):2.r.E(\'<u B="\'+a.q+" "+a.q+"G"+(b+1)+\'"><a Q="#k\'+b+\'">14 \'+(b+1)+"</a></u>")});2.m=c("u",2.r);2.m.P(3(b){1d=c(x);N=c("a",x);N.1j(3(c){c.1m();a.S();e(d,2.6.8(b),b)})});2.1o=3(){7 b=2.4.o+1;9(b!=2.6.j){7 a=2.6.8(b);e(d,a,b)}};2.1p=3(){7 b=2.4.o-1;0>=b&&(b=0);7 a=2.6.8(b);e(d,a,b)};2.1q=3(a){7 a=a-1,c=2.6.8(a);e(d,c,a)};2.s.1n(3(){h()});1k(3(){0==2.n&&h()},1f)};7 h=3(){2.n=2.s.w();2.O=2.n+2.o;1h(i=0;i<2.6.j;i++){7 d=2.6.8(i).y().z;2.4.l&&(d-=2.4.l);7 a=0;9(i<2.6.j-1)7 a=2.6.8(i+1),a=2.4.l?a.y().z-2.4.l:a.y().z,b=2.m.8(i),c=2.m.8(2.6.j-1);9(2.L.W(":Y"))v!1;9(2.K.T()==2.O){9(!c.A("t"))v p=2.6.j-1,g(k,p),!1}10 9(a){9(2.n>=d&&2.n<a&&!b.A("t"))v p=i,g(k,p),!1}10 9(2.n>=d&&i==2.6.j-1&&!2.m.8(2.6.j-1).A("t"))v p=2.6.j-1,g(k,p),!1}},e=3(d,a,b){a=a.y().z;2.4.l&&(a-=2.4.l);7 e=c("1l, X"),f=c(11).w();a!=f&&!e.W(":Y")&&e.12({w:a},2.4.U,3(){g(d,b);2.4.V()})},g=3(c,a){2.m.18("t");2.m.8(a).D("t");2.4.o=a;2.4.Z()};9(!2.4)v k(x,f)}})})(13);',62,89,'||pageScroller|function|options||sections|var|eq|if||||||||||length|pageScroll|scrollOffset|pageLinks|scrollPosition|currentSection|updateTo|navigationClass|navi|scrollWindow|active|li|return|scrollTop|this|offset|top|hasClass|class|navigation|addClass|append|sectionClass|_|css|ul|extend|scrollDocument|scrollBody|position|pageAnchor|scrollDistance|each|href|relative|animationBefore|height|animationSpeed|animationComplete|is|body|animated|onChange|else|window|animate|jQuery|Navigation|20|document|left|removeClass|interval|fx|margin|500|pageLink|scrollNav|200|section|for|fn|click|setTimeout|html|preventDefault|scroll|next|prev|goTo'.split('|'),0,{}))