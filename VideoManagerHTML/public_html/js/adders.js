function addMedia(id, array)
{
	var media = mediaModel.cloneNode(true);

	media.id = id;
	media.getElementsByTagName("h4")[0].innerText = array.info.name;
	var img = (array.info.img === null) ? "unknown.jpg" : array.info.img;
	img = (array.info.loading) ? "media/loading.gif" : "media/posters/" + img;
	media.children[0].style.backgroundImage = "url('" + img + "')";

	var rateToAppend = "";
	if (array.info.loading)
		rateToAppend = '\n<p>Fetching data...</p>';
	else
	{
		var rating = array.info.imdbRating;
		if (rating)
			rateToAppend = createStars(rating) + '\n<p>' + rating + "/10" + '</p>';
		else
			rateToAppend = "<p>No rating found...</p>";
	}

	media.getElementsByClassName("stars")[0].innerHTML = rateToAppend;

	updateGenres(array.info.genres);

	mediaList.appendChild(media);
	medias[id] = array;

	return media;
}

function createStars(imdbRating)
{
	if (!imdbRating)
		return "<p>N/A</p>";
	var remainingStars = 5;
	var rateToAppend = "";
	var rating = parseFloat(imdbRating) / 2;
	var max = Math.floor(rating);
	rating = rating - max;
	for (; max > 0; max--)
	{
		rateToAppend = rateToAppend + '<span class="full_star"></span>';
		remainingStars--;
	}
	if (rating > 0.3)
	{
		rateToAppend = rateToAppend + '<span class="half_star"></span>';
		remainingStars--;
	}
	for (; remainingStars > 0; remainingStars--) {
		rateToAppend = rateToAppend + '<span class="empty_star"></span>';
	}
	return rateToAppend;
}

function updateGenres(genres)
{
	var changed = false;
	for (var i in genres)
	{
		var g = genres[i];
		if (!allGenres.map[g.toLowerCase()])
		{
			changed = true;
			allGenres.array.push(g);
			allGenres.map[g.toLowerCase()] = true;
		}
	}
	if (changed)
	{
		allGenres.array.sort();
		var toAppend = "<li onclick=\"optionClick(this, '#genreOption')\">All</li>";
		for (var i in allGenres.array)
		{
			toAppend = toAppend + "<li onclick=\"optionClick(this, '#genreOption')\">" + allGenres.array[i] + "</li>";
		}
		genresList.empty();
		genresList.append(toAppend);
	}
}

var currentSorter = "name";
function sortMediaList(sorter, factor)
{
	if (sorter && sorter === currentSorter)
		return;

	sorter = (sorter) ? sorter : "name";
	factor = (factor) ? factor : 1;
	currentSorter = sorter;

	var list = document.getElementById('media-list');
	if (!list)
		return;
	var toSort = list.children;
	toSort = Array.prototype.slice.call(toSort, 0);

	toSort.sort(function (a, b) {
		var aord = medias[a.id].info[sorter].toLowerCase();
		var bord = medias[b.id].info[sorter].toLowerCase();
		return ((aord > bord) ? 1 : -1) * factor;
	});

	list.innerHTML = "";
	for (var i = 0, l = toSort.length; i < l; i++) {
		list.appendChild(toSort[i]);
	}
}

function addNewLocation()
{
	addLocation(app.addNewLocation());
}


function addLocation(name)
{
	var newLoc = locationsList.children[0].cloneNode(true);
	newLoc.innerText = name;
	locationsList.appendChild(newLoc);
}

function addFavorite(elt)
{
	$(elt).toggleClass("favorited");
	currentMedia.info["favorite"] = "True";
	app.toggleFavorite(currentId);
}