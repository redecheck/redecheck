/*
Anti-spam plugin
No spam in comments. No captcha.
wordpress.org/plugins/anti-spam/
*/

(function($) {

	function anti_spam_init() {
		$('.antispam-group').hide(); // hide inputs from users

		var answer = $('.antispam-group .antispam-control-a').val(); // get answer
		$('.antispam-group-q .antispam-control-q').val(answer); // set answer into other input instead of user
		$('.antispam-group-e .antispam-control-e').val(''); // clear value of the empty input because some themes are adding some value for all inputs

		var current_date = new Date();
		var current_year = current_date.getFullYear();
		var dynamic_control = '<input type="hidden" name="antspm-q" class="antispam-control antispam-control-q" value="'+current_year+'" />';

		$.each($('#comments form'), function(index, commentForm) { // add input for every comment form if there are more than 1 form
			if ($(commentForm).find('.antispam-control-q').length == 0) {
				$(commentForm).append(dynamic_control);
			}
		});

		$.each($('#respond form'), function(index, commentForm) { // add input for every comment form if there are more than 1 form
			if ($(commentForm).find('.antispam-control-q').length == 0) {
				$(commentForm).append(dynamic_control);
			}
		});

		$.each($('form#commentform'), function(index, commentForm) { // add input for every comment form if there are more than 1 form
			if ($(commentForm).find('.antispam-control-q').length == 0) {
				$(commentForm).append(dynamic_control);
			}
		});
	}

	$(document).ready(function() {
		anti_spam_init();
	});

	$(document).ajaxSuccess(function() { // add support for comments forms loaded via ajax
		anti_spam_init();
	});

})(jQuery);