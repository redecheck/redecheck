// Customized JS functions - www.fantassin.fr by Joffrey Jochum


parent_page = function(){
	
	var parent_id = $('body').attr('data-id');
	$('.page-item-' + parent_id).addClass('current');


// End of function	
};



anchors = function(){
	
	// http://www.htmlzengarden.com/2009/10/ancres_et_deplacement_progressif_de_l_ascenseur/
	
    var speed = 1000;
	
    jQuery('a[href^="#"]').bind('click',function(){
		
		var id = jQuery(this).attr('href');
		var rel = jQuery(this).attr('rel');
		
		if ( rel != 'no-target' ) {
			if( id == '#' )
            	goTo('body');
			else
				goTo(id);
			return(false);
		}
    });
	
    function goTo(ancre){
    	jQuery( 'html,body' ).animate({scrollTop:jQuery(ancre).offset().top},speed,'swing',function(){
            if ( ancre != 'body' )
                window.location.hash = ancre;
            else
                window.location.hash = '#';
            jQuery(ancre).attr('tabindex','-1');
            jQuery(ancre).focus();
            jQuery(ancre).removeAttr('tabindex');
        });
    }

// End of function	
};



properties = function(){
	
	// Add Class to header elements for header to be minified on scroll
	$('.menu li a').addClass('fast');
	
	// When we arrive on the anchor via a direct link, ensure the header is colorized
	if ( $(document).scrollTop() > 1) { $('header').addClass('colorized'); }

	// Add and remove classes to the header on scroll
	$(window).scroll( function() {
		var top = $(this).scrollTop();
		if ( top > 1 ) {
			$('header').addClass('colorized');
		}
		if ( top < 1 ) {
			$('header').removeClass('colorized');
		}		
		if ( top > 400 ) {
			$('header').addClass('scrolled');
			$('.subheader').addClass('scrolled');
		}
		if ( top < 400 ) {
			$('header').removeClass('scrolled');
			$('.subheader').removeClass('scrolled');
		}
		if ( top > $(window).height() ) {
			$('#top_page').removeClass('invisible');
		}
		if ( top < $(window).height() ) {
			$('#top_page').addClass('invisible');
		}
	});
	
	// Hover states of dispatchers	
	$('.dispatcher').find('.previous').mouseenter(function() {
        $('.dispatcher').addClass('hover-left');
    });
	$('.dispatcher').find('.previous').mouseleave(function() {
        $('.dispatcher').removeClass('hover-left');
    });
	
	$('.dispatcher').find('.next').mouseenter(function() {
        $('.dispatcher').addClass('hover-right');
    });
	
	$('.dispatcher').find('.next').mouseleave(function() {
        $('.dispatcher').removeClass('hover-right');
    });		
	
	// Full width for project-planner-form fields	
	$('#briefing_form input').addClass('full-width');
	$('#briefing_form textarea').addClass('full-width');
	
	$('#briefing_form .button').removeClass('full-width');
	$('#briefing_form .button-cancel').removeClass('full-width');	
	
	$('#briefing_form .full-width').each(function( index ) {
		var largeur = $(this).parent('div').width() - 48;
		$(this).css( 'width' , largeur + 'px' );
	});

	// Embed size
	$('.video').each( function(e){
		var nouvelle_largeur = $('.container-article').width() - 30;
		var nouvelle_hauteur = ( $(this).height() * nouvelle_largeur ) / $(this).width();
		
		$(this).attr({
			width : nouvelle_largeur + 'px',
			height : nouvelle_hauteur + 'px'	
		});
	});

	// Blog : FB comments width
	var fb_comments_width = $('.container-article').width() - 30;

	$('.fb-comments').attr({
		width : fb_comments_width + 'px'
	});

// End of function
};



introduction = function() {

	// Condition 1 : we have a big intro AND we are not on a mobile
	if ( ( $('#big_intro').length != 0 ) && ( $(window).width() > 1024 ) ) {

	// Condition 2 : we have enough height		
		// Window height
		var window_height = $(window).height();

		// Content height
		var intro_height = $('.introduction').outerHeight();

			// Indentation if News
			if ( $('#news').length != 0 ) { 
				intro_height += $('#news').outerHeight(); 
			}

			// If Categories (to do when time will come)
		
		// Ratio
		var available_space = window_height - intro_height;

		// White space under Intro
		var white_space = 50;

		// Available height : if more than "white space" then adapt height
		if ( available_space > white_space+1 ) {
			
			var space_to_distribute = available_space - white_space;

			if ( $('#news').length != 0 ) {
				space_to_distribute += white_space;
			}

			var space_1 = Math.round( space_to_distribute * 0.62 );
			var space_2 = space_to_distribute - space_1;

			$('#big_intro').css('padding' , space_1 + 'px 0px ' + space_2 + 'px 0px' );
		}
	};	

// End of function
};



menu = function(){
	
	// Mobile menu : from 320 to 650px
	
	var menu_status = false;
	
	$('#bt_menu').click( function(){
		event.preventDefault();
		
		if ( menu_status ) {
			$('.menu').removeClass('open');
			$('.menu').addClass('closed');
			
			menu_status = false;
			
			$('#bt_menu').addClass('off');
			$('#bt_menu').removeClass('on');
		} else {
			$('.menu').removeClass('closed');
			$('.menu').addClass('open');
			
			menu_status = true;
			
			$('#bt_menu').addClass('on');
			$('#bt_menu').removeClass('off');
		}
	});

// End of function	
};



accordion = function(){
	
	// Accordion feature
	
	$('.accordion-step').addClass('closed');
		
	$('.accordion-bt').each(function( index ) {
		
		$(this).click(function( event ) {
			event.preventDefault();
			
			if ( $(this).parent('.accordion-step').hasClass('closed') ) {
				
				$('.accordion-step').removeClass('open');
				$('.accordion-step').addClass('closed');
		
				$(this).parent('.accordion-step').removeClass('closed');
				$(this).parent('.accordion-step').addClass('open');
				
				var id = jQuery(this).attr('href');
				jQuery('html,body').animate({scrollTop:jQuery(id).offset().top},1000);
				
			} else {
				
				$('.accordion-step').removeClass('open');
				$('.accordion-step').addClass('closed');
				
			}
		});
	});
	
// End of function
};



forms = function(){
	
	// Empty Newsletter field on click
	var basic_value = $('#mce-EMAIL').attr("value");
	
	$('#mce-EMAIL').focus( function(){
		if ( $('#mce-EMAIL').val() == basic_value ) {
			$('#mce-EMAIL').val("");
		} 
	});
	
	$('#mce-EMAIL').focusout( function(){
		if ( !$('#mce-EMAIL').val() ) {
			$('#mce-EMAIL').val( basic_value );
		}
	});
	
		
	// Briefing form
    var $form = $('#briefing_form');
    $form.on('submit', function(e){
    
        e.preventDefault();
        $('#submit').attr("disabled", "disabled");
        
        $.ajax({
            type: $form.prop('method'),
            url: $form.prop('action'),
            data: $form.serialize(),
            dataType: 'json',
            success: function(data){
                if (data.success) {
                    $('.feedback.good').show();
                } else {
                    $('.feedback.bad').show();
                }
                $form.remove();
            },
            error: function(){
                $('.feedback.bad').show();
                $form.remove();
            }           
        });
        
    });

// End of function
};



jQuery().ready(function($) {
	
    introduction();
    parent_page();
	properties();
	anchors();
	menu();
	accordion();
    forms();
    

    // Delete original size of project thumbs
	$('.project-image img').removeAttr('width');
	$('.project-image img').removeAttr('height');

	// Delete original size of article images
	$('.container-article img').removeAttr('width');
	$('.container-article img').removeAttr('height');
	$('.wp-caption').removeAttr('style');
	
});


$(window).resize(function($) {

	introduction();
	properties();

});

