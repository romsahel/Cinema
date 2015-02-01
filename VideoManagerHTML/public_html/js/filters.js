
function filterByType(elt, type)
{
	if (elt.hasClass("selected"))
		return;

	$("html, body").animate({scrollTop: 0}, 500);
	$(".category").removeClass('selected');
	elt.addClass('selected');
	$(mediaList).fadeTo(200, 0, function () {
		for (var key in medias)
		{
			var div = $('#' + key);
			if (!div.is(":visible"))
				continue;
			var elt = medias[key];
			if (type === undefined || elt.info.type === type)
				div.show();
			else
			{
				if (div.hasClass("selected"))
					currentMedia = null;
				div.hide();
			}
		}
		_setSelection();
		$(mediaList).fadeTo(300, 1, function () {
			selectFirstVisibleMedia();
		});
	});
}

var currentGenre = "All";
function filterByCategory(genre)
{
	if (currentGenre === genre)
		return;
	currentGenre = genre;
	$("html, body").animate({scrollTop: 0}, 500);
	$(mediaList).fadeTo(200, 0, function () {
		for (var key in medias)
		{
			var div = $('#' + key);
			if (!div.is(":visible"))
				continue;
			var elt = medias[key];
			var found = false;
			if (genre !== "All")
			{
				if (elt.info.genres !== undefined)
					for (var i = 0; i < elt.info.genres.length; i++)
						if (elt.info.genres[i] === genre)
						{
							div.show();
							found = true;
							break;
						}
				if (!found)
				{
					if (div.hasClass("selected"))
						currentMedia = null;
					div.hide();
				}
			}
			else
				div.show();
		}
		_setSelection();
		$(mediaList).fadeTo(300, 1, function () {
			selectFirstVisibleMedia();
		});
	});
}

var currentLocation = "All";
function filterByLocation(location)
{
	if (currentLocation === location)
		return;
	currentLocation = location;
	$("html, body").animate({scrollTop: 0}, 500);
	$(mediaList).fadeTo(200, 0, function () {
		for (var key in medias)
		{
			var div = $('#' + key);
			if (!div.is(":visible"))
				continue;
			var elt = medias[key];
			if (location !== "All")
			{
				if (elt.info.location === location)
					div.show();
				else
				{
					if (div.hasClass("selected"))
						currentMedia = null;
					div.hide();
				}
			}
			else
				div.show();
		}
		_setSelection();
		$(mediaList).fadeTo(300, 1, function () {
			selectFirstVisibleMedia();
		});
	});
}
