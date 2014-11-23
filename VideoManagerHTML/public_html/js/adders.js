function addMedia(id, array)
{
	var media = mediaModel.cloneNode(true);

	media.id = id;
	media.getElementsByTagName("h4")[0].innerText = array.info.name;
	media.children[0].style.backgroundImage = "url('media/posters/" + array.info.img + "')";



	var rateToAppend = createStars(array.info.imdbRating);
	rateToAppend = rateToAppend + '\n<p>' + array.info.imdbRating + "/10" + '</p>';
	media.getElementsByClassName("stars")[0].innerHTML = rateToAppend;

	updateGenres(array.info.genres);

	mediaList.appendChild(media);
	medias[id] = array;
}

function createStars(imdbRating)
{
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

function sortMediaList()
{
	var list = document.getElementById('media-list');
	var toSort = list.children;
	toSort = Array.prototype.slice.call(toSort, 0);

	toSort.sort(function (a, b) {
		var aord = medias[a.id].info.name.toLowerCase();
		var bord = medias[b.id].info.name.toLowerCase();
		return (aord > bord) ? 1 : -1;
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
