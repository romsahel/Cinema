var medias = {};
var currentMedia = null;
var currentEpisode = null;
var currentSeason = null;

var mediaList;
var detail;
var seasons;
var episodes;
var model;
var locationsList;
var split;
var detailsToUpdate;
var searchBarParent;
var searchBar;
var resizingTimeout;

function onPageLoaded()
{
	mediaList = document.getElementById("media-list");
	detail = document.getElementById("detail");
	model = document.getElementById("model");
	locationsList = document.getElementById("locationsList");
	split = $("#split");
	searchBarParent = $('#search-bar');
	searchBar = $('input[type="text"]');
	seasons = $("#seasons");
	episodes = $("#episodes");
	detailsToUpdate = {
		"name": $("#detail-title"),
		"year": $("#detail-year"),
		"duration": $("#detail-duration"),
		"genres": $("#detail-genres"),
		"overview": $("#detail-description")
	};

	if (navigator.vendor === "Google Inc.")
		debug();

	mediaList.style.width = $(window).width() / 2;
	updateSplitPane();

	document.onmouseup = up;

	seasons.on('click', 'li', function () {
		onSeasonsClick($(this));
	});

	episodes.on('click', 'li', function () {
		$(this).parent().children().removeClass("selected");
		$(this).addClass("selected");

		var text = $(this).text();
		currentEpisode = {key: text, value: currentSeason.elt[text]};

		$("#watch-buttons").css({top: $(this).index() * $(this).height()});
		$("#watch-buttons").fadeIn('fast');
	});

	showDetail(currentMedia);

}

function onSeasonsClick(elt)
{
	currentSeason = {index: elt.index(), elt: currentMedia.seasons[elt.text()]};
	elt.parent().children().removeClass("selected");
	elt.addClass("selected");
	episodes.children().removeClass("selected");
	var selected = $(episodes.children()[elt.index()]);
	selected.children().removeClass("selected");
	selected.addClass("selected");

	$("#watch-buttons").fadeOut('fast');
}

$('html').click(function () {
	var elements = $("#listsContainer > .select");
	for (var i = 0; i < elements.length; i++) {
		elements.fadeOut("fast");
	}
});

function playMedia(local)
{
	if (local)
	{
		console.local("play local");
	}
	console.log(currentMedia.id);
	console.log(currentSeason.index);
	console.log(currentEpisode);
	app.playMedia(currentMedia.id, currentSeason.index, currentEpisode.key);
}

function updateSearch(search)
{
	console.log("coucou");
	for (var key in medias) {
		var div = $('#' + key);
		var elt = medias[key];
		if ((elt.info.name.toLowerCase()).indexOf(search) === -1)
			div.fadeOut('fast');
		else
			div.fadeIn('fast');
	}
}

function optionClick(elt, option)
{
	$(option).html($(elt).html());
}

function selectSort(elt, list)
{
	var pos = $(elt).position();
	$(list).css({left: pos.left, top: pos.top, width: $(elt).width()});
	$(list).fadeToggle("fast");
	event.stopPropagation();
}

function addMedia(id, array)
{
	var media = model.cloneNode(true);

	media.id = id;
	media.getElementsByTagName("h4")[0].innerText = array.info.name;
	media.children[0].style.backgroundImage = "url('media/posters/" + array.info.img + "')";

	mediaList.appendChild(media);
	medias[id] = array;
}

function addLocation(name)
{
	var newLoc = locationsList.children[0].cloneNode(true);
	newLoc.innerText = name;
	locationsList.appendChild(newLoc);
}

function changeText(element, text)
{
	element.fadeOut(200, function () {
		$(this).text(text).fadeIn(200);
	});
}

function showDetail(elt)
{
	currentMedia = medias[elt.id];

	for (var key in detailsToUpdate)
		changeText(detailsToUpdate[key], currentMedia.info[key]);

	seasons.hide();
	episodes.hide();
	seasons.empty();
	episodes.empty();

	var currSeasons = currentMedia.seasons;
	var seasonsToAppend = "";
	var episodesToAppend = "";
	for (var sKey in currSeasons)
	{
		seasonsToAppend = seasonsToAppend + "<li>" + sKey + "</li>";
		episodesToAppend = episodesToAppend + "<ul>";
		for (var eKey in currSeasons[sKey])
			episodesToAppend = episodesToAppend + "<li>" + eKey + "</li>";
		episodesToAppend = episodesToAppend + "</ul>";
	}

	seasons.append(seasonsToAppend);
	episodes.append(episodesToAppend);

	seasons.fadeIn('fast');
	episodes.fadeIn('fast');
	onSeasonsClick($(seasons.children()[0]));

	$('#detail-poster').attr('src', 'media/posters/' + currentMedia.info.img);

	$("#detail").show();
}

function updateSplitPane()
{
	var width = mediaList.offsetWidth;
	split.css({left: width});
	var detailWidth = $(window).width() - width - 42;
	detail.style.width = detailWidth;
}

function moveSplitbar(e)
{
	mediaList.style.width = e.pageX;
	updateSplitPane();

	return cancelEvent(e);
}

// <editor-fold defaultstate="collapsed" desc="debug function">
function debug()
{
	currentMedia = {'id': 'model', "info": {"name": "Once upon a time"}, 'seasons': {'Season 1': {'Episode 01': '1', 'Episode 02': '2', 'Episode 03': '3'}, 'Season 2': {'Episode 03': '', 'Episode 04': '', 'Episode 05': ''}, 'Season 3': {'Episode 10': '', 'Episode 20': '', 'Episode 30': ''}, }}
	medias['model'] = currentMedia;

	addMedia('160315098', {"id": "160315098", "info": {"duration": "60", "overview": "The service offered by The Lightman Group is truly unique. Simply stated, they can tell if you're lying. It's not the words you speak that give you away, it's what your body and face have to say. Dr. Cal Lightman and his team are experts at reading micro-expressions, the fleeting tics that express, non-verbally, what we are really feeling. With their finely honed interviewing and investigating skills, they have an uncanny ability to dig up the truth.", "img": "1612484692", "imdb": "tt1235099", "year": "2009", "genres": "Drama Crime", "name": "Lie To Me Season 1, 2 & 3"}, "seasons": {"Season 1": {"Episode 01.avi": "C:\\Users\\romsahel\\Videos\\Lie To Me Season 1, 2 & 3 Complete DVDRip HDTV\\Season 1\\Episode 01.avi"}, "Season 2": {"Episode 01.avi": "C:\\Users\\romsahel\\Videos\\Lie To Me Season 1, 2 & 3 Complete DVDRip HDTV\\Season 2\\Episode 01.avi"}, "Season 3": {"Episode 01.avi": "C:\\Users\\romsahel\\Videos\\Lie To Me Season 1, 2 & 3 Complete DVDRip HDTV\\Season 3\\Episode 01.avi"}}})
}
;
// </editor-fold>
