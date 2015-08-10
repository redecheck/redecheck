$(document).ready(function() {

	/*
	if($().flexigrid) {
		$('.service-table, .sort-table').flexigrid({
			height:'auto',
			striped:false,
			resizable:false,
			colMove:false

		});
	}
	*/
	
	if($().tablesorter) {
		$(".service-table, .sort-table").tablesorter(); 
	}

	if($().customInput) {
		$('input').customInput();
	}

	$('[data-depend-on]').each(function(){

		var self = this;
		var	input_name = $(this).attr("data-depend-on");
		var	value_visible = $(this).attr("data-value-visible");
		var	input_value = $('input[name="' + input_name + '"]').val();

		if(input_value == value_visible) {
			$(self).show();
		} else {
			$(self).hide();
		}

		$("input[name='" + input_name + "'], select[name='" + input_name + "']").change(function(){

			input_value = $(this).val();

			if(input_value == value_visible) {
				$(self).show();
			} else {
				$(self).hide();
			}

		});

	});


    if ( $('#city-search').length > 0 ) {
        refreshCitySearch();
    }
    $('#state-search').change(function() {
        refreshCitySearch();
    });

	// in registration form
    $('#registration_country').change(function() {
        renameZipCode();
    });
	
	$('#registration_country').change();
	
	// in new bid form
	$('select[name=fee_structure]').change(function() {
		var fee_structure = $(this).val();
		
		console.log(fee_structure);
		
		if (fee_structure == 'Tiered') {
			$(".only-tiered-fields").css("display","block");
			$("#label_processing_rate").html("Qualified Rate&nbsp;")
			$("#label_authorization_fee").html("Qualified Authorization Fee&nbsp;")
		} else {
			$(".only-tiered-fields").css("display","none");
			$("#label_processing_rate").html("Processing Rate&nbsp;")
			$("#label_authorization_fee").html("Authorization Fee&nbsp;")
		}
	});
	
    
  $('.split-btn').splitdropbutton({
      toggleDivContent: '<i class="fa fa-chevron-down"></i>' // optional html content for the clickable toggle div
  })    
  
  $("#video_popup_overlay").css("height", $(document).height() + "px");
  
});

function open_popup() {
    $("#video_popup").css("display", "block");
    $("#video_popup_overlay").css("display", "block");
}

function close_popup() {
    $("#video_popup").css("display", "none");
    $("#video_popup_overlay").css("display", "none");
}

function refreshCitySearch()
{
    var url = $('#state-search').attr('data-cities');

    $.ajax({
        'url'       : url,
        'type'      : 'get',
        'dataType'  : 'html',
        'context'   : $('#city-search'),
        'data'      : {
            'state' : $('#state-search').val()
        },
        'success'   : function(data) {
            $(this).html( data );

            if (search_city != undefined) {
                $('#city-search').val(search_city);
            }
        }
    });
}

function renameZipCode()
{
	var country = $('#registration_country').val();
	
	if (country == 'US') {
		$('#registration_zipcode_label').text('Zip Code');
	} else {
		$('#registration_zipcode_label').text('Postal Code');
	}
}