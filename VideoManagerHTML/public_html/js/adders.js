function addMedia(id, array)
{
	var media = mediaModel.cloneNode(true);

	media.id = id;
	media.getElementsByTagName("h4")[0].innerText = array.info.name;
	media.children[0].style.backgroundImage = "url('media/posters/" + array.info.img + "')";

	updateGenres(array.info.genres);

	mediaList.appendChild(media);
	medias[id] = array;
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
