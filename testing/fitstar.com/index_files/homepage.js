if (Modernizr.mq('only screen and (min-width: 769px)')) {

	jQuery(window).ready(function($) {
	    $("#hero-home .bg-blackTrans").addClass("play");
	});
}


// ==========================================================================
// Add white background to header on scroll
// ==========================================================================
if (Modernizr.mq('only screen and (min-width: 769px)')) {

	$(window).scroll(function() {    
	    var scroll = $(window).scrollTop();

	    if (scroll >= 40) {
	        $("header.global").addClass("fix");
	    } else {
	        $("header.global").removeClass("fix");
	    }

	    if (scroll >= 160) {
	        $("header.global").addClass("transition");
	        
	    } else {
	        $("header.global").removeClass("transition");
	    }
	});
}

if (Modernizr.mq('only screen and (min-width: 950px)')) {

	$(window).scroll(function() {    
	    var scroll = $(window).scrollTop();

	    if (scroll >= 160) {
	        $("header.global .brand").addClass("fade");
	        
	    } else {
	        $("header.global .brand").removeClass("fade");
	    }
	});
}


jQuery(window).ready(function($) {
	// ==========================================================================
	// TRIPLE THREAT (SLIDING PANELS)
	// ==========================================================================
	var $sliding_panels = $("#sliding-panels .columns");
	$sliding_panels.each(function (i, panel) {
		$(panel).on('mouseenter touchend',
			function () {
				$sliding_panels.removeClass("static");
				$sliding_panels.removeClass("active");
				$sliding_panels.removeClass("inactive");

				$(this).addClass("active");
				$sliding_panels.not(this).addClass("inactive");
			}
		);
		$(panel).on('mouseleave',
			function () {
				$(panel).removeClass("active");
				$sliding_panels.addClass("static");
				$sliding_panels.not(this).removeClass("inactive");
			}
		);
		$(panel).find('.close-slide').click(function (e) {
			e.preventDefault();
			$(panel).removeClass("active");
			$(panel).addClass("static");
			$sliding_panels.not(this).addClass("static");
			$sliding_panels.not(this).removeClass("inactive");
		});
	});


	// ==========================================================================
	// INTRO TABS
	// ==========================================================================
	var $tabs = $(".tabs.overview li");
	var active = 0;
	$tabs.each(function (i, tab) {

		var $tab = $(tab);
		$tab.click(function (e) {
			e.preventDefault();

			$tabs.removeClass('active above below');

			// previous tab open?
			if (i == (active + 1)) {
				$tabs.eq(active).addClass('above');

				// next tab open?
			} else if (i == (active - 1)) {
				$tabs.eq(active).addClass('below');
			}

			$(this).addClass('active');

			$(".tab_content").hide();
			var selected_tab = $(this).find("a").attr("href");
			$(selected_tab).fadeIn(750);
			active = i;
		});
	});
});



/**
 * Carousel for Real People on mobile
 *
 */
jQuery(window).ready(function($) {

	// Load owl carousel
	$.getScript(AppConfig.base_url + "/_/js/vendor/owl-carousel/owl.carousel.min.js", function(data, textStatus, jqxhr) {

		$(document).ready(function() {

			$("#real-success-stories .owl-carousel").owlCarousel({
				singleItem: true
			});

		});
	});


});