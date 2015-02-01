var medias = {};
var currentMedia = null;
var currentSeason = null;
var currentEpisode = null;

var mediaList;
var mediaModel;

var detailsToUpdate;
var detail;
var seasons;
var episodes;

var allGenres = {"array": [], "map": {}};
var genresList;
var locationsList;
var searchBarParent;
var searchBar;

var split;
var resizingTimeout;

var playList = false;
var withSubtitles = false;

function onPageLoaded()
{
	mediaList = document.getElementById("media-list");
	mediaModel = document.getElementById("model");

	detailsToUpdate = {
		"name": $("#detail-title"),
		"year": $("#detail-year"),
		"duration": $("#detail-duration"),
		"overview": $("#detail-description")
	};
	detail = document.getElementById("detail");
	seasons = $("#seasons");
	episodes = $("#episodes");


	locationsList = document.getElementById("locationsList");
	searchBarParent = $('#search-bar');
	searchBar = $('input[type="text"]');
	genresList = $('#genreList');

	split = $("#split");

	mediaList.style.width = $(window).width() - detail.offsetWidth - 60;
	updateSplitPane();

	document.onmouseup = up;

	seasons.on('click', 'li', function () {
		onSeasonsClick($(this));
	});

	episodes.on('click', 'li', function () {
		onEpisodesClick($(this));
	});

	episodes.on('click', 'li  > div > span', onSeenToggleClick);

	genresList.on('click', 'li', function () {
		filterByCategory($(this).text());
	});

	$("#locationsList").on('click', 'li', function () {
		filterByLocation($(this).text());
	});

	if (navigator.vendor === "Google Inc.")
		debug();
}

function optionClick(elt)
{
	var optionId = elt.parentNode.id;
	optionId = optionId.substring(0, optionId.indexOf("List")) + "Option";
	$('#' + optionId).html($(elt).html());
}

$('html').click(hideSelectOptions);

function hideSelectOptions()
{
	$("#listsContainer > *").fadeOut(150);
	$("#watchList").fadeOut(150);
}

function onEpisodesClick(elt, shouldScroll)
{
	if (shouldScroll)
	{
		var offset = elt.offset().top - episodes.offset().top - 10;
		if (offset > 10)
			episodes.animate({scrollTop: offset}, 500);
	}

	if (elt.hasClass("selected"))
		return;

	elt.parent().children().removeClass("selected");
	elt.addClass("selected");

	var text = $(elt.children()[1]).text();
	currentEpisode = {key: text, value: currentSeason.value[text]};
}

function onSeasonsClick(elt, f)
{
	if (elt.hasClass("selected"))
		return;

	var text = elt.text();
	currentSeason = {key: text, value: currentMedia.seasons[text]};
	episodes.fadeTo(100, 0, function () {
		elt.parent().children().removeClass("selected");
		elt.addClass("selected");
		episodes.children().removeClass("selected");
		var selected = $(episodes.children()[elt.index()]);
		selected.children().removeClass("selected");
		selected.addClass("selected");

		episodes.fadeTo("fast", 1);

		if (f)
			f();
		else
			episodes.find(".selected > li").first().click();
	});
}

function getCurrentId()
{
	if (currentMedia)
		return currentMedia.id;
	else
		return null;
}
