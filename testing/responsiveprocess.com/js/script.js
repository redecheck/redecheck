/* Author:

*/


//$(document).ready(function(){
	
	// Run Matt Kersley's jQuery Responsive menu plugin (see plugins.js)
//	if ($.fn.mobileMenu) {
//		$('ol#id').mobileMenu({
	//		switchWidth: 768,                   // width (in px to switch at)
		//	topOptionText: 'Choose a page',     // first option text
//			indentString: '&nbsp;&nbsp;&nbsp;'  // string for indenting nested items
//		});
//	}

	// Run Mathias Bynens jQuery placeholder plugin (see plugins.js)
//	if ($.fn.placeholder) {
//		$('input, textarea').placeholder();		
//	}
//});


/* Author:
	Steve Fisher - Yellow Pencil Inc
*/


$(document).ready(function(){
	
	// Run Matt Kersley's jQuery Responsive menu plugin (see plugins.js)
	if ($.fn.mobileMenu) {
		$('ol#id').mobileMenu({
			switchWidth: 768,                   // width (in px to switch at)
			topOptionText: 'Choose a page',     // first option text
			indentString: '&nbsp;&nbsp;&nbsp;'  // string for indenting nested items
		});
	}

	// Run Mathias Bynens jQuery placeholder plugin (see plugins.js)
	if ($.fn.placeholder) {
		$('input, textarea').placeholder();		
	}
});



$(document).ready(function() {
	
	//
	// jQuery SmoothScroll | Version 11-09-30
	//

	$('a[href*=#]').click(function() {

		// skip SmoothScroll on links inside sliders or scroll boxes also using anchors or if there is a javascript call
		if($(this).parent().hasClass('scrollable_navigation') || $(this).attr('href').indexOf('javascript')>-1) return;

		// duration in ms
		var duration=1000;

		// easing values: swing | linear
		var easing='swing';

		// get / set parameters
		var newHash=this.hash;
		var oldLocation=window.location.href.replace(window.location.hash, '');
		var newLocation=this;

		// make sure it's the same location		
		if(oldLocation+newHash==newLocation)
		{
			// get target
			var target=$(this.hash+', a[name='+this.hash.slice(1)+']').offset().top;

			// adjust target for anchors near the bottom of the page
			if(target > $(document).height()-$(window).height()) target=$(document).height()-$(window).height();			

			// set selector
			if($.browser.safari) var animationSelector='body:not(:animated)';
			else var animationSelector='html:not(:animated)';

			// animate to target and set the hash to the window.location after the animation
			$(animationSelector).animate({ scrollTop: target }, duration, easing, function() {

				// add new hash to the browser location
				window.location.href=newLocation;
			});

			// cancel default click action
			return false;
		}
	});

});

$(document).ready(function(){

	//Hide (Collapse) the toggle containers on load
	$(".toggle_container").hide(); 

	//Switch the "Open" and "Close" state per click then slide up/down (depending on open/close state)
   $('.toggle').click(function(){
     $('div.toggle_container').slideToggle("medium");
   });

});


/* Flexslider */
  $(document).ready(function() {
    $('.flexslider').flexslider();
  });

$(window).load(function () {
/* TOOLTIPS ---------- */
  /* Positiong and options for adding tooltips */

  function foundationTooltipsInit() {
    var targets = $('.has-tip'),
    tipTemplate = function(target, content) {
      return '<span data-id="' + target + '" class="tooltip">' + content + '<span class="nub"></span></span>';
    };
    targets.each(function(){
      var target = $(this),
      content = target.attr('title'),
      classes = target.attr('class'),
      id = target.attr('id'),
      tip = $(tipTemplate(id, content));
      tip.addClass(classes).removeClass('has-tip').appendTo('body');
      if (Modernizr.touch) {
        tip.append('<span class="tap-to-close">tap to close </span>');
      }
      reposition(target, tip, classes);
      tip.hide();
    });
    $(window).resize(function() {
      var tips = $('.tooltip');
      tips.each(function() {
        var target = $('#' + $(this).data('id')),
        tip = $(this),
        classes = tip.attr('class');
        reposition(target, tip, classes);
      });
        
    });
    
    function reposition(target, tip, classes) {
      var width = target.data('width'),
      nub = tip.children('.nub'),
      nubHeight = nub.outerHeight(),
      nubWidth = nub.outerWidth();
      
      function nubPos(nub, top, right, bottom, left) {
        nub.css({
          'top' : top,
          'bottom' : bottom,
          'left' : left,
          'right' : right
        });
      }

      tip.css({
        'top' : (target.offset().top + target.outerHeight() + 10),
        'left' : target.offset().left,
        'width' : width
      });
      nubPos(nub, -nubHeight, 'auto', 'auto', 10);

      if ($(window).width() < 767) {
        var row = target.parents('.row');
        tip.width(row.outerWidth() - 20).css('left', row.offset().left);
        nubPos(nub, -nubHeight, 'auto', 'auto', target.offset().left);
      } else {
        if (classes.indexOf('top') > -1) {
          var top = target.offset().top - tip.outerHeight() - nubHeight;
          tip.css({
            'top' : top,
            'left' : target.offset().left,
            'width' : width
          });
          nubPos(nub, 'auto', 'auto', -nubHeight, 'auto');
        } else if (classes.indexOf('left') > -1) {
          tip.css({
            'top' : target.offset().top - (target.outerHeight() / 2) - (nubHeight / 2),
            'left' : target.offset().left - tip.outerWidth() - 10,
            'width' : width
          });
          nubPos(nub, (tip.outerHeight() / 2) - (nubHeight / 2), -nubHeight, 'auto', 'auto');
        } else if (classes.indexOf('right') > -1){
          tip.css({
            'top' : target.offset().top - (target.outerHeight() / 2) - (nubHeight / 2),
            'left' : target.offset().left + target.outerWidth() + 10,
            'width' : width
          });
          nubPos(nub, (tip.outerHeight() / 2) - (nubHeight / 2), 'auto', 'auto', -nubHeight);
        }
      }
    }
    if (Modernizr.touch) {
      $('.tooltip').live('click touchend', function(e) {
        e.preventDefault();
        $(this).hide();
      });
      targets.live('click touchend', function(e){
        e.preventDefault();
        targets.hover(function() {
          $('span[data-id=' + $(this).attr('id') + ']').show();
          targets.attr('title', "");
        }, function() {
          $('span[data-id=' + $(this).attr('id') + ']').hide();
        }); 
      });

    } else {
      targets.hover(function() {
        $('span[data-id=' + $(this).attr('id') + ']').show();
        targets.attr('title', "");
      }, function() {
        $('span[data-id=' + $(this).attr('id') + ']').hide();
      }); 
    }
  }
  foundationTooltipsInit();
});



