$(function() {
	pageInitialize();

	function pageInitialize() {
		$(':radio[name="useProxy"]').change(function(e) {
			if ($(this).val() == 1) {
				$('#proxySettings').show();
			} else {
				$('#proxySettings').hide();
			}
		});

		$(':radio[name="requestMethod"]').change(function(e) {
			if ($(this).val() == 'post') {
				$('#requestBodyRow').show();
			} else {
				$('#requestBodyRow').hide();
			}
		});

		$('.btn_add').click(function(e) {
			var request = {
				targetUrl: $('#targetURL').val(),
				requestMethod : $(':radio[name="requestMethod"]:checked').val(),
				useProxy : ($(':radio[name="useProxy"]:checked').val() == 1) ? true : false,
				proxyHost : $('#proxyURL').val(),
				proxyPort : parseInt($('#proxyPort').val()),
				body : $('#requestBody').val()
			};
//			console.log('Send request');
//			console.log(JSON.stringify(request));

			$.ajax({
				type : "POST",
				url : './sendRequest',
				cache : false,
				contentType : 'application/json',
				data : JSON.stringify(request)
			}).success(function(response, textStatus, xhr) {
				$('#responseBody').text("");
				$('#statusCode').text(xhr.status);
				$('#responseBody').text(response);
				
				console.log(response);
			}).fail(function(response) {
				$('#responseBody').text("");
				$('#statusCode').text(response.status);
				$('#responseBody').text(response.responseText);
				console.log(response);
			});

		})
	}
});