var currentType = "All";
var currentGenre = "All";
var currentLocation = "All";

function filterByType(elt, type)
{
	if (elt === null || elt.hasClass("selected"))
		return;

	$(".category").removeClass('selected');
	elt.addClass('selected');
	currentType = type;
	filter();
}

function filterByLocation(location)
{
	if (location === null || location === currentLocation)
		return;
	currentLocation = location;
	filter();
}

function filterByGenre(genre)
{
	if (genre === null || genre === currentGenre)
		return;
	currentGenre = genre;
	filter();
}

function filter()
{
	$("body > div").animate({scrollTop: 0}, 500);
	if (type === "favorites")
	{
		
	}
	else
	{
		$(mediaList).fadeTo(200, 0, function () {
			for (var key in medias)
			{
				var div = $('#' + key);
				var elt = medias[key];
				if ((currentType === "All" || elt.info.type === currentType)
						&& (currentLocation === "All" || elt.info.location === currentLocation)
						&& (currentGenre === "All" || elt.info.genres.indexOf(currentGenre) !== -1))
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
}