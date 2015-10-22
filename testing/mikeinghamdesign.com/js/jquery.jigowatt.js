jQuery(document).ready(function(){
	
	$('#contactform').submit(function(){
	
		var action = $(this).attr('action');
		
		$('#submit').attr('disabled','disabled').after('<img src="assets/ajax-loader.gif" class="loader" />');
		
		$("#message").slideUp(750,function() {
		$('#message').hide();			
		
		$.post(action, { 
			name: $('#name').val(),
			email: $('#email').val(),
			phone: $('#phone').val(),
			subject: $('#subject').val(),
			comments: $('#comments').val(),
			verify: $('#verify').val()
		},
			function(data){
				document.getElementById('message').innerHTML = data;
				$('#message').slideDown('slow');
				$('#contactform img.loader').fadeOut('fast',function(){$(this).remove()});
				$('#submit').removeAttr('disabled'); 
				if(data.match('success') != null) $('#contactform').slideUp('slow');
				
			}
		);
		
		});
		
		return false; 
	
	});
	
});