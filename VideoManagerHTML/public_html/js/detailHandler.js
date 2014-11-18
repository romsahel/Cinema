function updateDetails()
{
	var info = currentMedia.info;
	seasons.empty();
	episodes.empty();

	for (var key in detailsToUpdate)
		detailsToUpdate[key].text(info[key]);

	detailsToUpdate.duration.text(detailsToUpdate.duration.text() + " min");

	$("#detail-imdb img").attr("alt", "http://www.imdb.com/title/" + info.imdb + "/");

	updateDetailGenres(info.genres);

	updateDetailFiles();

	onSeasonsClick($(seasons.children()[0]));

	$("#detail").fadeTo(100, 1);

	$('#detail-poster').attr('src', 'media/posters/' + info.img);
}

function updateDetailFiles()
{
	var currSeasons = currentMedia.seasons;
	var seasonsToAppend = "";
	var episodesToAppend = "";
	for (var sKey in currSeasons)
	{
		seasonsToAppend = seasonsToAppend + "<li>" + sKey + "</li>";
		episodesToAppend = episodesToAppend + "<ul>";
		var i = 1;
		for (var eKey in currSeasons[sKey])
		{
			var span = "<span onclick='onSeenToggleClick($(this))'" + (currSeasons[sKey][eKey].seen ? " class=\"seen\"" : "") + "></span>";
			var div = "<div>" + span + "<div>" + eKey + "</div></div>";
			episodesToAppend = episodesToAppend + "<li>" + "<span>" + i + "</span>" + div + "</li>";
			i = i + 1;
		}
		episodesToAppend = episodesToAppend + "</ul>";
	}

	seasons.append(seasonsToAppend);
	episodes.append(episodesToAppend);
}

function updateDetailGenres(genres)
{
	$("#detail-genres a").text(genres[0]);
	$("#detailsGenreList").empty();

	$("#detail-genres").unbind("click");
	if (genres.length > 1)
	{
		$("#detail-genres").click(function () {
			dropDownClick(this, '#detailsGenreList', true);
		});
		$("#detail-genres").css({'cursor': 'pointer'});

		$("#detail-genres span").show();
		var genresToAppend = "";
		for (var i = 0; i < genres.length; i = i + 1)
			genresToAppend = genresToAppend + "<li onclick=\"optionClick(this)\">" + genres[i] + "</li>";
		$("#detailsGenreList").append(genresToAppend);
	}
	else
	{
		$("#detail-genres span").hide();
		$("#detail-genres").click(null);
		$("#detail-genres").css({'cursor': 'default'});
	}
}

function showDetail(elt, refresh)
{
	var newMedia = medias[elt.id];
	currentId = elt.id;
	if (!refresh && newMedia === currentMedia)
		return;

	$(elt).parent().children().removeClass("selected");
	$(elt).addClass("selected");
	currentMedia = newMedia;

	if (!refresh)
		$("#detail").fadeTo(100, 0, updateDetails);
	else
		updateDetails();
}