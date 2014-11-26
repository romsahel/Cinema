function updateDetails(media)
{
	var info;
	if (media)
		info = media.info;
	else
		info = currentMedia.info;

	seasons.empty();
	episodes.empty();

	for (var key in detailsToUpdate)
		detailsToUpdate[key].text(info[key]);

	detailsToUpdate.duration.text(detailsToUpdate.duration.text() + " min");

	$("#detail-imdb img").attr("alt", "http://www.imdb.com/title/" + info.imdb + "/");
	$(banner).find(".stars").empty();
	$(banner).find(".stars").append(createStars(info.imdbRating));

	updateDetailGenres(info.genres);

	updateDetailFiles();

	if (!selection.set)
		onSeasonsClick($(seasons.children()[0]));
	else if (selection.episode)
	{
		onSeasonsClick(seasons.children(".tmp"),
				function () {
					onEpisodesClick(episodes.find(".tmp"));
				});
	}
	selection = {"set": false};

	$("#detail").fadeTo(200, 1);

	$('#detail-poster').attr('src', 'media/posters/' + info.img);
}

function updateDetailFiles()
{
	var currSeasons = currentMedia.seasons;
	var seasonsToAppend = "";
	var episodesToAppend = "";
	var selectedTag = " class=\"selected\"";
	for (var sKey in currSeasons)
	{
		var isCurrentSeason = (sKey === selection.season);
		seasonsToAppend = seasonsToAppend + "<li class=\"" + (isCurrentSeason ? "tmp" : "") + "\">" + sKey + "</li>";
		episodesToAppend = episodesToAppend + "<ul" + (isCurrentSeason ? selectedTag : "") + ">";
		var i = 1;
		for (var eKey in currSeasons[sKey])
		{
			var spanClass = (currSeasons[sKey][eKey].seen ? "seen" : "");
			if (navigator.appVersion.indexOf("Mac") !== -1)
				spanClass += " mac";

			var span = "<span class=\"" + spanClass + "\"></span>";
			var div = "<div>" + span + "<div>" + eKey + "</div></div>";
			episodesToAppend = episodesToAppend + "<li class=\"" + ((eKey === selection.episode) ? "tmp" : "") + "\"><span>" + i + "</span>" + div + "</li>";
			i = i + 1;
		}
		episodesToAppend = episodesToAppend + "</ul>";
	}

	seasons.append(seasonsToAppend);
	episodes.append(episodesToAppend);
}

function updateDetailGenres(genres)
{
	if (!genres || genres.length <= 0)
		return;

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
		$("#detail").fadeTo(200, 0, updateDetails);
	else
		updateDetails();
}

function toggleSeenSeason()
{
	var list = $("#episodes .selected li > div > span");
	var toSet = !$(list[0]).hasClass("seen");

	for (var i = 0; i < list.length; i++)
	{
		toggleSeen($(list[i]), toSet);
	}
}

function toggleEpisodesUntilThere()
{
	var list = $("#episodes .selected li > div > span");
	var toSet = !$(list[0]).hasClass("seen");

	for (var i = 0; i < list.length; i++)
	{
		var current = $(list[i]);
		toggleSeen(current, toSet);
		if (current.parent().parent().hasClass("selected"))
			break;
	}
}

function onSeenToggleClick(e)
{
	toggleSeen($(this));
	return cancelEvent(e);
}

function toggleSeen(elt, toSet, noJavaCall)
{
	var seenValue = elt.hasClass("seen");
	if (toSet === true || (!seenValue && toSet === undefined))
		elt.addClass("seen");
	else
		elt.removeClass("seen");

	currentSeason.value[elt.parent().text()].seen = !currentSeason.value[elt.parent().text()].seen || toSet;

	if (seenValue !== toSet && !noJavaCall)
	{
		var episode = elt.parent().parent();
		app.toggleSeen(currentMedia.id, currentSeason.key, $(episode.children()[1]).text());
	}

	return false;
}