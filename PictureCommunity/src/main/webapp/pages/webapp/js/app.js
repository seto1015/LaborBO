// Global variables

var APP_TOKEN = null;
var APP_USERNAME = null;
var APP_API = 'http://127.0.0.1:8080/PictureCommunity/REST/';

// some helper functions
function showPage(pageName) {
	$('#pageContent > .page').hide();
	$('#pageContent > div.page-' + pageName).show();
}

function reloadFriends() {

	// remove all friends
	$('.friendList').find('a').remove();

	$('.friendList > .spinner').show();

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
				var state = 'unknown';
				if (user.state != undefined) {
					if (user.state == 1) {
						state = 'online';
					} else {
						state = 'offline';
					}
				}
				$('.friendList').append('<a class="state-' + state + '" href="#' + user.username + '">' + user.username + '</a>');
			});
		}
	});
}

function displayForeignPictures(username) {

	// set topic
	if (username == '*') {
		$('.page-foreignPictures > h1').text('Öffentliche Bilder');
	} else if (username == APP_USERNAME) {
		$('.page-foreignPictures > h1').text('Eigene Bilder');
	} else {
		$('.page-foreignPictures > h1').text('Bilder von ' + username);
	}

	// clear thumbnails
	$('.page-foreignPictures > .thumbnails').empty().addClass('thumbnails-loading');

	// display page
	showPage('foreignPictures');

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
			$(data).each(function(key, image) {
				var url = APP_API + 'picture/' + image.id + '?width=160&height=120&token=' + APP_TOKEN + '&_tmp=' + Math.random();
				$('.page-foreignPictures > .thumbnails').append('<img src="' + url + '" x-image-id="' + image.id + '" x-image-description="' + image.description + '" x-image-title="' + image.name + '" />');
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

	$('#dlgPicture').dialog({
		modal : true,
		autoOpen : false,
		width: 850,
		heigth: 700,
		close: function() {
			$('#dlgPicture').find('img').attr('src', 'img/preload-image.gif');
		}
	});

	$('#dlgUpload').dialog({
		modal : true,
		autoOpen : false,
		buttons : {
			Ok : function() {
				$(this).dialog('close');
			}
		}
	});

	$('#btnReloadFriends').button({
		icons : {
			primary : 'ui-icon-refresh'
		},
		text : false
	}).click(reloadFriends);

	$('#fileupload').fileupload({
		url : APP_API + 'newpicture',
		formData : function(form) {
			return [{
				name : 'token',
				value : APP_TOKEN
			}, {
				name : 'description',
				value : $('.page-uploadImage').find('textarea[name="description"]').val()
			}, {
				name : 'public',
				value : $('.page-uploadImage').find('input[name="public"]').is(':checked')
			}];

		},
		add : function(e, data) {
			data.context = $('#btnUpload').unbind('click').click(function() {
				$(this).attr('disabled', 'disabled').button('option', 'label', 'Uploading..');
				data.submit();
			});
		},
		done : function(e, data) {
			$('#dlgUpload').dialog('open');
			$('#btnUpload').removeAttr('disabled').button('option', 'label', 'Bild hochladen');
		}
	});

	$('#btnUpload').button({

	});

	$('#btnLogout').button({
		icons : {
			primary : 'ui-icon-locked'
		}
	}).click(function() {

		// reset token
		APP_TOKEN = null;
		APP_USERNAME = null;

		// hide userinfo
		$('#header > .userInfo').hide().find('span.username').text('');

		// hide menu
		$('#header > .menu').hide();

		// hide and clear friendlist
		$('.friendList').hide().find('a').remove();

		showPage('login');
	});

	$('#btnLogin').button().click(function() {
		$(this).attr('disabled', 'disabled');

		$.ajax({
			url : APP_API + 'token',
			type : 'GET',
			dataType : 'json',
			data : {
				username : $('.page-login').find('input[name="username"]').val(),
				password : $('.page-login').find('input[name="password"]').val()
			},
			complete : function() {
				$('#btnLogin').removeAttr('disabled');
			},
			success : function(data) {
				//console.debug(data);
				if (data.token != undefined && data.token != null) {

					APP_USERNAME = $('.page-login').find('input[name="username"]').val();

					// save token
					APP_TOKEN = data.token;

					// show userinfo
					$('#header > .userInfo').find('span.username').text(APP_USERNAME);
					$('#header > .userInfo').show();

					// show menu
					$('#header > .menu').show();

					// show friendlist
					$('.friendList').show();
					reloadFriends();

					// reset form-fields
					$('.page-login').find('input[name="username"]').val('');
					$('.page-login').find('input[name="password"]').val('');

					// show welcome-page
					showPage('welcome');

				} else {
					// login error
					$('#loginError').show('fast');
				}
			}
		});
	});

	$('.friendList').delegate('a', 'click', function(event) {
		event.preventDefault();
		var username = $(this).attr('href').substr(1);
		displayForeignPictures(username);
	});

	$('.page-foreignPictures > .thumbnails').delegate('img', 'click', function(event) {
		event.preventDefault();
		var id = $(this).attr('x-image-id');
		var title = $(this).attr('x-image-title');
		var description = $(this).attr('x-image-description');

		var url = APP_API + 'picture/' + id + '?width=800&height=600&token=' + APP_TOKEN + '&_tmp=' + Math.random();

		// display image
		$('#dlgPicture').find('span.description').text(description);
		$('#dlgPicture').find('img').attr('src', url);
		$('#dlgPicture').dialog('open');
		$('#dlgPicture').dialog({
			'title' : title
		});

	});

	$('#header .menu a[href="#allImages"]').click(function(event) {
		event.preventDefault();
		displayForeignPictures('*');
	});

	$('#header .menu a[href="#ownImages"]').click(function(event) {
		event.preventDefault();
		displayForeignPictures(APP_USERNAME);
	});

	$('#header .menu a[href="#uploadImage"]').click(function(event) {
		event.preventDefault();
		showPage('uploadImage');
	});

	$('#loginError').click(function() {
		$(this).hide('fast');
	});

});
