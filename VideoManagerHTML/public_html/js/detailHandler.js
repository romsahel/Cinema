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

	detailsToUpdate.duration.text(detailsToUpdate.duration.text());

	$("#detail-imdb img").attr("alt", "http://www.imdb.com/title/" + info.imdb + "/");
	$(banner).find(".stars").empty();
	$(banner).find(".stars").append(createStars(info.imdbRating));

	updateDetailGenres(info.genres);

	var indexes = updateDetailFiles();
	if (indexes === null)
		indexes = {season: 1, episode: 1};

	if (!selection.set)
	{
		onSeasonsClick($(seasons.children()[indexes.season - 1]),
				function () {
					onEpisodesClick($(episodes.find(".selected > li")[indexes.episode - 1]), true);
				});
	}
	else if (selection.episode)
	{
		onSeasonsClick(seasons.children(".tmp"),
				function () {
					onEpisodesClick(episodes.find(".tmp"), true);
				});
	}
	selection = {"set": false};

	$("#detail").fadeTo(200, 1);

	var img = (info.img === null) ? "unknown.jpg" : info.img;
	$('#detail-poster').attr('src', 'media/posters/' + img);
}

function updateDetailFiles()
{
	var currSeasons = currentMedia.seasons;
	var sIndex = 1;
	var seasonsToAppend = "";
	var episodesToAppend = "";
	var selectedTag = " class=\"selected\"";
	var result = null;
	for (var sKey in currSeasons)
	{
		var isCurrentSeason = (sKey === selection.season);
		seasonsToAppend = seasonsToAppend + "<li class=\"" + (isCurrentSeason ? "tmp" : "") + "\">" + sKey + "</li>";
		episodesToAppend = episodesToAppend + "<ul" + (isCurrentSeason ? selectedTag : "") + ">";
		var eIndex = 1;
		for (var eKey in currSeasons[sKey])
		{
			var spanClass = "";
			if (currSeasons[sKey][eKey].seen)
				spanClass = "seen"
			else if (result === null)
				result = {season: sIndex, episode: eIndex};
			if (navigator.appVersion.indexOf("Mac") !== -1)
				spanClass += " mac";

			var span = "<span class=\"" + spanClass + "\"></span>";
			var div = "<div>" + span + "<div>" + eKey + "</div></div>";
			episodesToAppend = episodesToAppend + "<li class=\"" + ((eKey === selection.episode) ? "tmp" : "") + "\"><span>" + eIndex + "</span>" + div + "</li>";
			eIndex++;
		}
		episodesToAppend = episodesToAppend + "</ul>";
		sIndex++;
	}

	seasons.append(seasonsToAppend);
	episodes.append(episodesToAppend);
	return result;
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
			dropDownClick(this, '#detailsGenreList', true, true);
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

function toggleSeenSeason(reset)
{
	var list = $("#episodes .selected li > div > span");
	var toSet;
	if (reset)
		toSet = false;
	else
		toSet = !$(list[0]).hasClass("seen");

	for (var i = 0; i < list.length; i++)
		toggleSeen($(list[i]), toSet, false, reset);
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
	console.log(this);
	toggleSeen($(this));
	return cancelEvent(e);
}

function toggleSeen(elt, toSet, noJavaCall, reset)
{
	if (elt === null)
		elt = $("#episodes .selected li.selected > div > span");

	var seenValue = elt.hasClass("seen");
	if (toSet === true || (!seenValue && toSet === undefined))
		elt.addClass("seen");
	else
		elt.removeClass("seen");

	toSet = !currentSeason.value[elt.parent().text()].seen || toSet;
	currentSeason.value[elt.parent().text()].seen = toSet;

	if (seenValue !== toSet && !noJavaCall)
	{
		var episode = elt.parent().parent();
		app.toggleSeen(currentMedia.id, currentSeason.key, $(episode.children()[1]).text(), toSet, reset);
	}

	return false;
}