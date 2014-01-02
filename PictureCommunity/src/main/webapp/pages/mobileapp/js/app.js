// Global variables

var APP_TOKEN = null;
var APP_USERNAME = null;
var APP_API = 'http://127.0.0.1:8080/PictureCommunity/REST/';


function showPage(pageName) {
	console.log('switch to ' + pageName);
	$.mobile.changePage('#pg' + pageName.charAt(0).toUpperCase() + pageName.slice(1));
}



function loadUserPictures(username) {

	// set topic
	if (username == '*') {
		$('.page-foreignPictures > h1').text('Alle Bilder');
	} else if (username == APP_USERNAME) {
		$('.page-foreignPictures > h1').text('Eigene Bilder');
	} else {
		$('.page-foreignPictures > h1').text('Bilder von ' + username);
	}
	
	
	var grid = $('.thumbnail-grid', $.mobile.activePage);
	console.debug(grid);

	// clear thumbnails
	$(grid).empty();


	// load pictures
	$.ajax({
		url : APP_API + 'pictures/' + username,
		type : 'GET',
		dataType : 'json',
		data : {
			token : APP_TOKEN
		},
		complete : function() {
			$('.page-foreignPictures > .thumbnails').removeClass('thumbnails-loading');
		},
		success : function(data) {
			
			// detect window-size
			var width = Math.ceil($(window).width() / 3);
			
			var block = new Array('a','b','c');
			$(data).each(function(key, image) {
				var url = APP_API + 'picture/' + image.id + '?width='+width+'&token=' + APP_TOKEN + '&_tmp=' + Math.random();
				$('.thumbnail-grid', $.mobile.activePage).append('<div class="ui-block-'+block[key % block.length]+'"><img style="width:100%;" src="' + url + '" x-image-id="' + image.id + '" x-image-description="' + image.description + '" x-image-title="' + image.name + '" /></div>');
			});
		}
	});
}



function reloadFriends() {

	// remove all friends
	$('.friendList').find('button').remove();


	$.ajax({
		url : APP_API + 'user/friends/' + APP_USERNAME,
		type : 'GET',
		dataType : 'json',
		data : {
			token : APP_TOKEN
		},
		complete : function() {
			$('.friendList > .spinner').hide();
		},
		success : function(data) {
			$(data).each(function(key, user) {
				var state = 'clock';
				if (user.state != undefined) {
					if (user.state == 1) {
						state = 'check';
					} else {
						state = 'delete';
					}
				}
				$('.friendList').append('<button class="ui-btn ui-icon-'+state+' ui-btn-icon-left" x-username="'+user.username+'">'+user.username+'</button>');
			});
		}
	});
}







$(function() {
	
	$.ajaxSetup({
		cache : false
	});

	$(document).ajaxSend(function() {
		$('#loadingIndicator').show();
	}).ajaxComplete(function() {
		$('#loadingIndicator').fadeOut();
	});
	
	
	
	$('#image').fileupload({
		url : APP_API + 'newpicture',
		formData : function(form) {
			return [{
				name : 'token',
				value : APP_TOKEN
			},  {
				name : 'description',
				value : $('#description').val()
			}, {
				name : 'public',
				value : $('#public').is(':checked')
			}];

		},
		add : function(e, data) {
			data.context = $('#btnUpload').unbind('click').click(function() {
				$(this).attr('disabled', 'disabled').text('Uploading..');
				data.submit();
			});
		},
		done : function(e, data) {
			alert('Das Bild wurde erfolgreich auf den Server übertragen.');
			$('#btnUpload').removeAttr('disabled').text('Bild hochladen');
		}
	});
	
	
	
	
	
	
	$('a[href="#btnLogout"]').click(function(event) {
		event.preventDefault();
		
		// reset token
		APP_TOKEN = null;
		APP_USERNAME = null;

		showPage('login');
	});
	
	$('#btnLogin').click(function() {
		$(this).attr('disabled', 'disabled');

		$.ajax({
			url : APP_API + 'token',
			type : 'GET',
			dataType : 'json',
			data : {
				username : $('#username').val(),
				password : $('#password').val(),
			},
			complete : function() {
				$('#btnLogin').removeAttr('disabled');
			},
			success : function(data) {
				console.debug(data.token);
				if (data.token != undefined && data.token != '-1') {

					APP_USERNAME = $('#username').val();

					// save token
					APP_TOKEN = data.token;

					
					// reset form-fields
					$('#username').val('');
					$('#password').val('');

					
					showPage('ownImages');
					loadUserPictures(APP_USERNAME);

				} else {
					// login error
					$('#loginError').show('fast');
				}
			}
		});
	});
	
	
	
	
	// menu items
	$('a[href="#pgOwnImages"]').click(function(event) {
		event.preventDefault();
		showPage('ownImages');
		loadUserPictures(APP_USERNAME);
	});
	
	$('a[href="#pgAllImages"]').click(function(event) {
		event.preventDefault();
		showPage('allImages');
		loadUserPictures('*');
	});
	
	$('a[href="#pgUploadImage"]').click(function(event) {
		event.preventDefault();
		showPage('uploadImage');
	});
	
	$('a[href="#pgFriends"]').click(function(event) {
		event.preventDefault();
		showPage('friends');
		reloadFriends();
	});


	$('.friendList').delegate('button', 'click', function(event) {
		event.preventDefault();
		var username = $(this).attr('x-username');
		showPage('imagesOfFriend');
		$('span.friend-username', $.mobile.activePage).text(username);
		loadUserPictures(username);
	});
	
	
	$('.thumbnail-grid').delegate('img', 'click', function(event) {
		event.preventDefault();
		var id = $(this).attr('x-image-id');
		var title = $(this).attr('x-image-title');
		var description = $(this).attr('x-image-description');

		var width = $(window).width();
		var url = APP_API + 'picture/' + id + '?width='+width+'&token=' + APP_TOKEN + '&_tmp=' + Math.random();


		var currentPage = $($.mobile.activePage).attr('id').substr(2);
		$('a[href="#back"]').unbind('click').click(function() {
			showPage(currentPage);
			$('#fullImage').attr('src', 'img/blank.png');
		});
		
		showPage('fullImage');

		$('#pgFullImage').find('h1').text(title);

		// display image
		$('#fullImage').attr('src', url);
		$('#fullImageDescription').text(description);

	});

});
