!function($){"use strict";$.fn['bootstrapSwitch']=function(method){var inputSelector='input[type!="hidden"]';var methods={init:function(){return this.each(function(){var $element=$(this),$div,$switchLeft,$switchRight,$label,$form=$element.closest('form'),myClasses="",classes=$element.attr('class'),color,moving,onLabel="ON",offLabel="OFF",icon=false,textLabel=false;$.each(['switch-mini','switch-small','switch-large'],function(i,el){if(classes.indexOf(el)>=0)myClasses=el});$element.addClass('has-switch');if($element.data('on')!==undefined)color="switch-"+$element.data('on');if($element.data('on-label')!==undefined)onLabel=$element.data('on-label');if($element.data('off-label')!==undefined)offLabel=$element.data('off-label');if($element.data('label-icon')!==undefined)icon=$element.data('label-icon');if($element.data('text-label')!==undefined)textLabel=$element.data('text-label');$switchLeft=$('<span>').addClass("switch-left").addClass(myClasses).addClass(color).html(onLabel);color='';if($element.data('off')!==undefined)color="switch-"+$element.data('off');$switchRight=$('<span>').addClass("switch-right").addClass(myClasses).addClass(color).html(offLabel);$label=$('<label>').html("&nbsp;").addClass(myClasses).attr('for',$element.find(inputSelector).attr('id'));if(icon){$label.html('<i class="icon '+icon+'"></i>')}if(textLabel){$label.html(''+textLabel+'')}$div=$element.find(inputSelector).wrap($('<div>')).parent().data('animated',false);if($element.data('animated')!==false)$div.addClass('switch-animate').data('animated',true);$div.append($switchLeft).append($label).append($switchRight);$element.find('>div').addClass($element.find(inputSelector).is(':checked')?'switch-on':'switch-off');if($element.find(inputSelector).is(':disabled'))$(this).addClass('deactivate');var changeStatus=function($this){if($element.parent('label').is('.label-change-switch')){}else{$this.siblings('label').trigger('mousedown').trigger('mouseup').trigger('click')}};$element.on('keydown',function(e){if(e.keyCode===32){e.stopImmediatePropagation();e.preventDefault();changeStatus($(e.target).find('span:first'))}});$switchLeft.on('click',function(e){changeStatus($(this))});$switchRight.on('click',function(e){changeStatus($(this))});$element.find(inputSelector).on('change',function(e,skipOnChange){var $this=$(this),$element=$this.parent(),thisState=$this.is(':checked'),state=$element.is('.switch-off');e.preventDefault();$element.css('left','');if(state===thisState){if(thisState)$element.removeClass('switch-off').addClass('switch-on');else $element.removeClass('switch-on').addClass('switch-off');if($element.data('animated')!==false)$element.addClass("switch-animate");if(typeof skipOnChange==='boolean'&&skipOnChange)return;$element.parent().trigger('switch-change',{'el':$this,'value':thisState})}});$element.find('label').on('mousedown touchstart',function(e){var $this=$(this);moving=false;e.preventDefault();e.stopImmediatePropagation();$this.closest('div').removeClass('switch-animate');if($this.closest('.has-switch').is('.deactivate')){$this.unbind('click')}else if($this.closest('.switch-on').parent().is('.radio-no-uncheck')){$this.unbind('click')}else{$this.on('mousemove touchmove',function(e){var $element=$(this).closest('.make-switch'),relativeX=(e.pageX||e.originalEvent.targetTouches[0].pageX)-$element.offset().left,percent=(relativeX/$element.width())*100,left=25,right=75;moving=true;if(percent<left)percent=left;else if(percent>right)percent=right;$element.find('>div').css('left',(percent-right)+"%")});$this.on('click touchend',function(e){var $this=$(this),$target=$(e.target),$myRadioCheckBox=$target.siblings('input');e.stopImmediatePropagation();e.preventDefault();$this.unbind('mouseleave');if(moving)$myRadioCheckBox.prop('checked',!(parseInt($this.parent().css('left'))<-25));else $myRadioCheckBox.prop("checked",!$myRadioCheckBox.is(":checked"));moving=false;$myRadioCheckBox.trigger('change')});$this.on('mouseleave',function(e){var $this=$(this),$myInputBox=$this.siblings('input');e.preventDefault();e.stopImmediatePropagation();$this.unbind('mouseleave');$this.trigger('mouseup');$myInputBox.prop('checked',!(parseInt($this.parent().css('left'))<-25)).trigger('change')});$this.on('mouseup',function(e){e.stopImmediatePropagation();e.preventDefault();$(this).unbind('mousemove')})}});if($form.data('bootstrapSwitch')!=='injected'){$form.bind('reset',function(){setTimeout(function(){$form.find('.make-switch').each(function(){var $input=$(this).find(inputSelector);$input.prop('checked',$input.is(':checked')).trigger('change')})},1)});$form.data('bootstrapSwitch','injected')}})},toggleActivation:function(){var $this=$(this);$this.toggleClass('deactivate');$this.find(inputSelector).prop('disabled',$this.is('.deactivate'))},isActive:function(){return!$(this).hasClass('deactivate')},setActive:function(active){var $this=$(this);if(active){$this.removeClass('deactivate');$this.find(inputSelector).removeAttr('disabled')}else{$this.addClass('deactivate');$this.find(inputSelector).attr('disabled','disabled')}},toggleState:function(skipOnChange){var $input=$(this).find(':checkbox');$input.prop('checked',!$input.is(':checked')).trigger('change',skipOnChange)},toggleRadioState:function(skipOnChange){var $radioinput=$(this).find(':radio');$radioinput.not(':checked').prop('checked',!$radioinput.is(':checked')).trigger('change',skipOnChange)},toggleRadioStateAllowUncheck:function(uncheck,skipOnChange){var $radioinput=$(this).find(':radio');if(uncheck){$radioinput.not(':checked').trigger('change',skipOnChange)}else{$radioinput.not(':checked').prop('checked',!$radioinput.is(':checked')).trigger('change',skipOnChange)}},setState:function(value,skipOnChange){$(this).find(inputSelector).prop('checked',value).trigger('change',skipOnChange)},setOnLabel:function(value){var $switchLeft=$(this).find(".switch-left");$switchLeft.html(value)},setOffLabel:function(value){var $switchRight=$(this).find(".switch-right");$switchRight.html(value)},setOnClass:function(value){var $switchLeft=$(this).find(".switch-left");var color='';if(value!==undefined){if($(this).attr('data-on')!==undefined){color="switch-"+$(this).attr('data-on')}$switchLeft.removeClass(color);color="switch-"+value;$switchLeft.addClass(color)}},setOffClass:function(value){var $switchRight=$(this).find(".switch-right");var color='';if(value!==undefined){if($(this).attr('data-off')!==undefined){color="switch-"+$(this).attr('data-off')}$switchRight.removeClass(color);color="switch-"+value;$switchRight.addClass(color)}},setAnimated:function(value){var $element=$(this).find(inputSelector).parent();if(value===undefined)value=false;$element.data('animated',value);$element.attr('data-animated',value);if($element.data('animated')!==false){$element.addClass("switch-animate")}else{$element.removeClass("switch-animate")}},setSizeClass:function(value){var $element=$(this);var $switchLeft=$element.find(".switch-left");var $switchRight=$element.find(".switch-right");var $label=$element.find("label");$.each(['switch-mini','switch-small','switch-large'],function(i,el){if(el!==value){$switchLeft.removeClass(el);$switchRight.removeClass(el);$label.removeClass(el)}else{$switchLeft.addClass(el);$switchRight.addClass(el);$label.addClass(el)}})},status:function(){return $(this).find(inputSelector).is(':checked')},destroy:function(){var $element=$(this),$div=$element.find('div'),$form=$element.closest('form'),$inputbox;$div.find(':not(input)').remove();$inputbox=$div.children();$inputbox.unwrap().unwrap();$inputbox.unbind('change');if($form){$form.unbind('reset');$form.removeData('bootstrapSwitch')}return $inputbox}};if(methods[method])return methods[method].apply(this,Array.prototype.slice.call(arguments,1));else if(typeof method==='object'||!method)return methods.init.apply(this,arguments);else $.error('Method '+method+' does not exist!')}}(jQuery);(function($){$(function(){$('.make-switch')['bootstrapSwitch']()})})(jQuery);$('.carousel').carousel();function setCookie(name,value,days){var date=new Date();date.setTime(date.getTime()+(days*24*60*60*1000));var expires="; expires="+ date.toGMTString();document.cookie=name+"="+ value+ expires;}
function readCookie(name){var n=name+"=";var cookie=document.cookie.split(';');for(var i=0;i<cookie.length;i++){var c=cookie[i];while(c.charAt(0)==' '){c=c.substring(1,c.length);}
if(c.indexOf(n)==0){return c.substring(n.length,c.length);}}
return null;}
$(document).ready(function(){var gainter=setInterval(function(){try{if(!readCookie('GA_CLIENT_ID')){var clientid=ga.getAll()[0].get('clientId');setCookie('GA_CLIENT_ID',clientid,730);}else{clearInterval(gainter);}}catch(err){}},500);});$('#sbtn').click(function(e){value=document.getElementById('sinpt').value;if(value==""){window.location='/';}else{window.location='/domain-name-search/'+ encodeURIComponent(value)+'?show='+ show;}});function formatResult(rterm){return'<div class="rterm">'+ rterm.id+'</div>';}
function formatSelection(rterm){return rterm.id;}
if($("#word1").length>0){$("#word1").select2({minimumInputLength:2,maximumSelectionSize:25,multiple:true,tokenSeparators:[","," "],closeOnSelect:false,selectOnBlur:true,initSelection:function(element,callback){var data=[];$(element.val().split(",")).each(function(){data.push({id:this,text:this});});callback(data);},createSearchChoice:function(term){return{id:term};},ajax:{url:"/syn_search/",dataType:'json',quietMillis:1,data:function(term,page){return{term:term,};},results:function(data,page){return{results:data};}},formatResult:formatResult,formatSelection:formatSelection,});}
if($("#word2").length>0){$("#word2").select2({minimumInputLength:2,maximumSelectionSize:25,multiple:true,tokenSeparators:[","," "],closeOnSelect:false,selectOnBlur:true,initSelection:function(element,callback){var data=[];$(element.val().split(",")).each(function(){data.push({id:this,text:this});});callback(data);},createSearchChoice:function(term){return{id:term};},ajax:{url:"/syn_search/",dataType:'json',quietMillis:1,data:function(term,page){return{term:term,};},results:function(data,page){return{results:data};}},formatResult:formatResult,formatSelection:formatSelection});}
function poptitle(e){var domain=$(this).attr('dn');if($(this).hasClass('ui-terms-ext-on')){return'<span class="text-center"><b>'+domain+'</b> is available <i class="icon-smile"></i></span>';}else return'<span class="text-center"><b>'+domain+'</b> is not available <i class="icon-frown"></i></span>';}
function popcontent(e){var domain=$(this).attr('dn');var ran=Math.floor((Math.random()*100)+1);var name=domain.split(".")[0];var ext=domain.split(/\.(.+)?/)[1];$.ajax({url:'https://twitter.com/'+ name,dataType:'jsonp',timeout:5000,statusCode:{0:function(){$(".popover .tc"+ran).append('<span style="color:green;">&nbsp;'+ name
+'</span>');},200:function(){$(".popover .tc"+ran).append('<span style="color:red;">&nbsp;'+ name
+'</span>');}},error:function(jqXHR,textStatus,errorThrown){},});$.ajax({url:'https://twitter.com/'+ name+ ext,dataType:'jsonp',timeout:5000,statusCode:{0:function(){$(".popover .tc"+ran).append('<span style="color:green;">&nbsp;'+ name
+ ext+'</span>');},200:function(){$(".popover .tc"+ran).append('<span style="color:red;">&nbsp;'+ name+ ext
+'</span>');}},error:function(jqXHR,textStatus,errorThrown){},});$.ajax({url:'https://twitter.com/'+ name+'_'+ ext,dataType:'jsonp',timeout:5000,statusCode:{0:function(){$(".popover .tc"+ran).append('<span style="color:green;">&nbsp;'+ name+'_'
+ ext+'</span>');},200:function(){$(".popover .tc"+ran).append('<span style="color:red;">&nbsp;'+ name+'_'
+ ext+'</span>');}},error:function(jqXHR,textStatus,errorThrown){},});$.ajax({url:'https://www.facebook.com/'+ name,dataType:'jsonp',timeout:2000,statusCode:{0:function(){$(".popover .fc"+ran).append('<span style="color:green;">&nbsp;'+ name
+'</span>');},200:function(){$(".popover .fc"+ran).append('<span style="color:red;">&nbsp;'+ name
+'</span>');}},error:function(jqXHR,textStatus,errorThrown){},});$.ajax({url:'https://www.facebook.com/'+ name+ ext,dataType:'jsonp',timeout:2000,statusCode:{0:function(){$(".popover .fc"+ran).append('<span style="color:green;">&nbsp;'+ name
+ ext+'</span>');},200:function(){$(".popover .fc"+ran).append('<span style="color:red;">&nbsp;'+ name+ ext
+'</span>');}},error:function(jqXHR,textStatus,errorThrown){},});$.ajax({url:'https://www.facebook.com/'+ name+'.'+ ext,dataType:'jsonp',timeout:2000,statusCode:{0:function(){$(".popover .fc"+ran).append('<span style="color:green;">&nbsp;'+ name
+'.'+ ext+'</span>');},200:function(){$(".popover .fc"+ran).append('<span style="color:red;">&nbsp;'+ name+'.'
+ ext+'</span>');}},error:function(jqXHR,textStatus,errorThrown){},});$.ajax({url:'/googlecheck/'+ domain,dataType:'jsonp',});var share='<hr><p>Did we help? Help us spread the word!</p><div class="share row">\
	    	<div class="fb col-md-4"><iframe src="//www.facebook.com/plugins/like.php?href=http%3A%2F%2Fwww.namemesh.com&amp;send=false&amp;layout=button_count&amp;width=200&amp;show_faces=false&amp;action=like&amp;colorscheme=light&amp;font&amp;height=35" scrolling="no" frameborder="0" style="border:none; overflow:hidden; width:200px; height:35px;" allowTransparency="true"></iframe></div>\
			<div class="gp col-md-8">\
    		<!-- Place this tag where you want the +1 button to render. -->\
			<iframe style="height:25px;" src="https://plusone.google.com/_/+1/fastbutton?bsv&amp;size=medium&amp;hl=en-US&amp;url=http://www.namemesh.com/&amp;parent=http://www.namemesh.com/" allowtransparency="true" frameborder="0" scrolling="no" title="+1"></iframe>\
    	</div></div><div class="share row">\
	    	<div class="tw col-md-6"><iframe allowtransparency="true" frameborder="0" scrolling="no"\
	      		src="//platform.twitter.com/widgets/follow_button.html?screen_name=namemesh&show_count=false"\
	      		style="width:210px; height:20px;"></iframe></div><div class="twt col-md-6"><iframe allowtransparency="true" frameborder="0" scrolling="no"\
	      	        src="https://platform.twitter.com/widgets/tweet_button.html?url=http://www.namemesh.com&text=Check out NameMesh for name brainstorming and searching domain quickly - "\
	      	          style="width:130px; height:20px;"></iframe>\
	      	</div>\
	    </div>';var regdeal=registrar=='godaddy'?'<br> <small> ($1.99 .COM deal applied automatically) </small>':'';var moredeals=registrar=='godaddy'?'<p class="text-center"><a href="/applycoupon/11391294" target="_blank" onclick="_gaq.push(['
+"'_trackEvent', 'pregister', 'pgh']);"+'"><b>OFFER! - 12 months hosting for $1/mo</b></a></p>':'';_gaq.push(['_trackEvent','pop',registrar,ext]);return'<div class="row soc tc'+ran+'" id="tc"><i class="icon-twitter-sign icon-large"></i>&nbsp;&nbsp;&nbsp;</div>\
			<div class="row soc fc'+ran+'" id="fc"><i class="icon-facebook-sign icon-large"></i>&nbsp;&nbsp;&nbsp;</div>\
	<hr><p class="text-center"><a class="btn btn-primary" onclick="_gaq.push(['
+"'_trackEvent', 'pregister', '"+registrar+"','"+ ext+"'"+']);" href="/register/'+registrar+'/'+domain+'" target="_blank">Register at '+$('#registrar :selected').text()+
regdeal+'</a></p>'+ moredeals;}
$(document).mousedown(function(e){if(e.button==2){_gaq.push(['_trackEvent','right',$(e.target).text()]);return false;}
return true;});function open_dialog(domain,avail,goreg){if($('.slide-out-div').length>0){setTimeout(function(){$('.slide-out-div').tabSlideOut('open')},1000);}
if($('#myModal').length>0){$('#myModal').remove();}
var name=domain.split(".")[0];var ext=domain.split(/\.(.+)?/)[1];_gaq.push(['_trackEvent','register',registrar,ext]);FDUPA.q.push(['conv',{'register':1,'registrar':registrar,'tld':ext}]);return;var head2,body2;var social='<div class="row soc col-md-10 col-md-offset-1 text-center" id="tc"><i class="icon-twitter-sign icon-large"></i>&nbsp;&nbsp;&nbsp;</div>\
		<div class="row soc col-md-10 col-md-offset-1 text-center" id="fc"><i class="icon-facebook-sign icon-large"></i>&nbsp;&nbsp;&nbsp;</div><br><br><br>';var offers='<br><div class="row">\
		<p class="text-center"><a onclick="_gaq.push(['
+"'_trackEvent', 'ac', '47'"
+']);" target="_blank" href="/applycoupon/11016747"><b>$3.00 .COM w/ $1</b> Private Registration from GoDaddy.com!</a></p>\
		<p class="text-center"><a onclick="_gaq.push(['
+"'_trackEvent', 'ac', '63'"
+']);" target="_blank" href="/applycoupon/11115263">Be Where your Customers are - <b>$1.99/mo hosting</b> from GoDaddy!</a></p>\
		<p class="text-center"><a onclick="_gaq.push(['
+"'_trackEvent', 'ac', '40'"
+']);" target="_blank" href="/applycoupon/10907540">20% off Premium <b>SSL Certificates</b> from GoDaddy.com!</a></p>\
		<p class="text-center"><a onclick="_gaq.push(['
+"'_trackEvent', 'ac', '494'"
+']);" target="_blank" href="/applycoupon/10378494">SPECIAL OFFER! Save <b>50%* on Hosting</b> Plans - GoDaddy</a></p>\
		<p class="text-center"><a onclick="_gaq.push(['
+"'_trackEvent', 'ac', '96'"
+']);" target="_blank" href="/applycoupon/11003096"><b>32% off</b> new products now at GoDaddy!</a></p>\
		<p class="text-center"><a onclick="_gaq.push(['
+"'_trackEvent', 'ac', '51'"
+']);" target="_blank" href="/applycoupon/11458551">Featured Offer! C$2.08 COM domains!</a></p>\
		<p class="text-center"><a onclick="_gaq.push(['
+"'_trackEvent', 'ac', '694'"
+']);" target="_blank" href="/applycoupon/10519694">Just point,click and build your very own website with GoDaddy.com!</a></p>\
		\
		</div>';var offers2='<div class="row" style="font-family: Arial;"><h4 class="text-center">Make the most with these amazing deals!</h4><br>\
		<p class="text-center"><i style="color: #e74c3c;" class="icon-tag"></i>&nbsp;<a style="color: #e74c3c;" onclick="_gaq.push(['
+"'_trackEvent', 'code', 'gh'"
+']);" target="_blank" href="/applycoupon/11391294">Exclusive Deal - Only <b>$1/mo</b> web hosting from <b>GoDaddy</b> with FREE Domain!</a></p>\
			<p class="text-center"><i style="color: #e74c3c;" class="icon-tag"></i>&nbsp;<a style="color: #e74c3c;" onclick="window.open('+"'http://www.bluehost.com/track/namemesh', '_blank');_gaq.push([ '_trackEvent', 'code', 'bh']);"+'" href="#">Exclusive Deal - <b>50% Off</b> on hosting from <b>Bluehost</b>!</a></p>\
			<p class="text-center"><i style="color: #e74c3c;" class="icon-tag"></i>&nbsp;<a style="color: #e74c3c;" target="_blank" onclick="_gaq.push(['
+"'_trackEvent', 'code', 'host'"
+']);" href="/applycoupon/11683581"><b>Upto 25% Off</b> on hosting from <b>Hostgator</b>!</a></p>\
			<p class="text-center"><i style="color: #e74c3c;" class="icon-tag"></i>&nbsp;<a style="color: #e74c3c;" target="_blank" onclick="_gaq.push(['
+"'_trackEvent', 'code', 'host'"
+']);" href="/applycoupon/10854181">Web hosting for 1 penny for first month (use code: 1CENT) from <b>Hostgator</b>!</a></p>\
		</div>';var share='<div class="share row">\
    	<div class="fb col-md-2"><iframe src="//www.facebook.com/plugins/like.php?href=http%3A%2F%2Fwww.namemesh.com&amp;send=false&amp;layout=button_count&amp;width=200&amp;show_faces=false&amp;action=like&amp;colorscheme=light&amp;font&amp;height=35" scrolling="no" frameborder="0" style="border:none; overflow:hidden; width:200px; height:35px;" allowTransparency="true"></iframe></div>\
    	<div class="tw col-md-4"><iframe allowtransparency="true" frameborder="0" scrolling="no"\
      		src="//platform.twitter.com/widgets/follow_button.html?screen_name=namemesh&show_count=false"\
      		style="width:210px; height:20px;"></iframe></div><div class="twt col-md-3"><iframe allowtransparency="true" frameborder="0" scrolling="no"\
      	        src="https://platform.twitter.com/widgets/tweet_button.html?url=http://www.namemesh.com&text=Check out NameMesh for name brainstorming and searching domain quickly - "\
      	          style="width:130px; height:20px;"></iframe>\
      	</div>\
    	<div class="gp col-md-3">\
    		<!-- Place this tag where you want the +1 button to render. -->\
    		<iframe allowtransparency="true" frameborder="0" hspace="0" marginheight="0" marginwidth="0" scrolling="no" style="position: static; top: 0px; width: 90px; margin: 0px; border-style: none; left: 0px; visibility: visible; height: 20px; " tabindex="0" vspace="0" width="100%" id="I0_1352725688036" name="I0_1352725688036" src="https://plusone.google.com/_/+1/fastbutton?bsv&amp;size=medium&amp;hl=en-US&amp;origin=http%3A%2F%2Fwww.namemesh.com&amp;url=http%3A%2F%2Fwww.namemesh.com%2F&amp;ic=1&amp;jsh=m%3B%2F_%2Fapps-static%2F_%2Fjs%2Fgapi%2F__features__%2Frt%3Dj%2Fver%3D61DPg0jmPmw.en_GB.%2Fsv%3D1%2Fam%3D!SYcw6mesaJeITQu65A%2Fd%3D1%2Frs%3DAItRSTPmyf3yE48lySS02PiZJP9hUodpxQ#_methods=onPlusOne%2C_ready%2C_close%2C_open%2C_resizeMe%2C_renderstart%2Concircled%2Conload&amp;id=I0_1352725688036&amp;parent=http%3A%2F%2Fwww.namemesh.com" title="+1"></iframe>\
    	</div>\
    </div>';if(avail==1){head2=' is available <i class="icon-smile"></i>';body2='<div class="row reg">\
			<a onclick="_gaq.push(['
+"'_trackEvent', 'register', 'godaddy', '"+ ext+"'"
+']);" class="col-sm-4 col-sm-offset-1 col-xs-4 col-xs-offset-1" target="_blank" href="/register/godaddy/'
+ domain
+'"><img height=40 src="/media/godaddy.png"></img><button class="btn btn-warning">Go Daddy</button></a>\
			<a onclick="_gaq.push(['
+"'_trackEvent', 'register', 'namecheap', '"+ ext+"'"
+']);" class="col-sm-4 col-sm-offset-1 col-xs-4 col-xs-offset-1" target="_blank" href="/register/namecheap/'
+ domain
+'"><img height=40 src="/media/namecheap.png"></img><button class="btn btn-warning">Name Cheap</button></a>\
			<a onclick="_gaq.push(['
+"'_trackEvent', 'register', 'name', '"+ ext+"'"
+']);" class="col-sm-4 col-sm-offset-1 col-xs-4 col-xs-offset-1"  target="_blank" href="/register/name/'
+ domain
+'"><img height=40 src="/media/name.png"></img><button class="btn btn-warning">Name.com</button></a>\
			<a onclick="_gaq.push(['
+"'_trackEvent', 'register', '101', '"+ ext+"'"
+']);" class="col-sm-4 col-sm-offset-1 col-xs-4 col-xs-offset-1"  target="_blank" href="/register/101domain/'
+ domain
+'"><img height=40 src="/media/101domain.png"></img><button class="btn btn-warning">101domain</button></a>\
			</div>';}else{head2=' is not available <i class="icon-frown"></i>';body2='<div class="row reg">\
			<a onclick="_gaq.push(['
+"'_trackEvent', 'nregister', 'godaddy', '"+ ext+"'"
+']);" class="col-md-6" target="_blank" href="/register/godaddy/'
+ domain
+'"><img width=225 height=50 src="/media/godaddy.png"></img><button class="btn btn-warning">Go Daddy</button></a>\
			<a onclick="_gaq.push(['
+"'_trackEvent', 'nregister', 'sedo', '"+ ext+"'"
+']);" class="col-md-6" target="_blank" href="/register/sedo/'
+ domain
+'"><img height=50 width=225 src="/media/sedo.gif"></img><button class="btn btn-warning">Sedo</button></a>\
			</div>';}
var div='<div class="modal fade" id="myModal" tabindex="-1" role="dialog"\
		aria-labelledby="myModalLabel" aria-hidden="true">\
    <div class="modal-dialog">\
      <div class="modal-content">\
        <div class="modal-header">\
          <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>\
          <h4 class="modal-title text-center"> '
+'Awesome! <i class="icon-smile"></i></h4>\
        </div>\
        <div class="modal-body">\
          '
+ offers2
+'\
        </div>\
        <div class="modal-footer">\
          '
+ share+'\
        </div>\
      </div>\
    </div>\
  </div>';$(document.body).append(div);$('#myModal').modal('show');return;$.ajax({url:'http://www.twitter.com/'+ name,dataType:'jsonp',timeout:5000,statusCode:{0:function(){$("#tc").append('<span style="color:green;">&nbsp;'+ name
+'</span>');},200:function(){$("#tc").append('<span style="color:red;">&nbsp;'+ name
+'</span>');}},error:function(jqXHR,textStatus,errorThrown){},});$.ajax({url:'http://www.twitter.com/'+ name+ ext,dataType:'jsonp',timeout:5000,statusCode:{0:function(){$("#tc").append('<span style="color:green;">&nbsp;'+ name+ ext
+'</span>');},200:function(){$("#tc").append('<span style="color:red;">&nbsp;'+ name+ ext
+'</span>');}},error:function(jqXHR,textStatus,errorThrown){},});$.ajax({url:'http://www.twitter.com/'+ name+'_'+ ext,dataType:'jsonp',timeout:5000,statusCode:{0:function(){$("#tc").append('<span style="color:green;">&nbsp;'+ name+'_'+ ext
+'</span>');},200:function(){$("#tc").append('<span style="color:red;">&nbsp;'+ name+'_'+ ext
+'</span>');}},error:function(jqXHR,textStatus,errorThrown){},});$.ajax({url:'http://www.facebook.com/'+ name,dataType:'jsonp',timeout:5000,statusCode:{0:function(){$("#fc").append('<span style="color:green;">&nbsp;'+ name
+'</span>');},200:function(){$("#fc").append('<span style="color:red;">&nbsp;'+ name
+'</span>');}},error:function(jqXHR,textStatus,errorThrown){},});$.ajax({url:'http://www.facebook.com/'+ name+ ext,dataType:'jsonp',timeout:5000,statusCode:{0:function(){$("#fc").append('<span style="color:green;">&nbsp;'+ name+ ext
+'</span>');},200:function(){$("#fc").append('<span style="color:red;">&nbsp;'+ name+ ext
+'</span>');}},error:function(jqXHR,textStatus,errorThrown){},});$.ajax({url:'http://www.facebook.com/'+ name+'.'+ ext,dataType:'jsonp',timeout:5000,statusCode:{0:function(){$("#fc").append('<span style="color:green;">&nbsp;'+ name+'.'+ ext
+'</span>');},200:function(){$("#fc").append('<span style="color:red;">&nbsp;'+ name+'.'+ ext
+'</span>');}},error:function(jqXHR,textStatus,errorThrown){},});$.ajax({url:'/googlecheck/'+ domain,dataType:'jsonp',});}