var playing = {};
function playMedia()
{
	toggleSeen($("#episodes > .selected > .selected > div > span"), true, true);
	var lastEpisode = playList ? $("#episodes ul.selected li").last().find("div > div").text() : null;
	playing = {"season": currentSeason, "episode": currentEpisode, "element": $("#episodes ul.selected li.selected")};
	app.playMedia(currentMedia.id, currentSeason.key, currentEpisode.key, lastEpisode, withSubtitles);
	setToggles(playList, withSubtitles);
}

function seenNextEpisode(name, index)
{
	if (playing.season === currentSeason)
	{
		var element = $(playing.element.nextAll()[index]);
		toggleSeen(element.find("div > span"), true, true);
		element.click();
	}
	else
		playing.season.value[name].seen = true;
}

var updateTimeouts = {};
var updating = 0;
function updateMedia(id, array)
{
	id = (id) ? id : array.id;
	var elt = $("#" + id);
	if (!elt.html())
	{
		if (updateTimeouts[id])
			clearTimeout(updateTimeouts[id]);

		updateTimeouts[id] = setTimeout(function () {
			delete updateTimeouts[id];
			updateMedia(id, array);
		}, 500);
		return;
	}

	updating = updating + 1;
	var selected = elt.hasClass("selected");

	elt.fadeTo(200, 0, function ()
	{
		$(this).remove();
		if (selected)
			currentMedia = null;
		if (array)
		{
			var newMedia = addMedia(id, array);
			newMedia.style.opacity = 0;
			$(newMedia).fadeTo(600, 1);
		}
		sortMediaList();
		if (selected)
			newMedia.click();
		updating = updating - 1;
	});
}

function mergeAndUpdate(deleted, changed)
{
	if (updating > 0 || Object.keys(updateTimeouts).length > 0)
	{
		setTimeout(function () {
			mergeAndUpdate(deleted, changed);
		}, 500);
		return;
	}

	for (var i in changed)
		if (changed[i])
			updateMedia(null, changed[i]);
	for (var i in deleted)
		if (deleted[i])
			$("#" + deleted[i]).remove();

}

function setMediaLoading(id)
{
	var elt = $("#" + id + " > .poster");
	if (elt.length > 0)
		elt.css({"background-image": "url(media/loading.gif)"});
}

function removeLocation()
{
	var locToRemove = $($("#locationsList li.hover")[0]);
	var text = locToRemove.text();
	locToRemove.slideUp(300, function () {
		$(this).remove();
	});
	return text;
}

function setToggles(list, sub)
{
	if (list)
		$($("#watch-buttons").find("div > div")[1]).click();
	if (sub)
		$($("#watch-buttons").find("div > div")[2]).click();
}

var selection = {"set": false};
function setSelection(id, season, episode)
{
	selection.id = id;
	selection.season = season;
	selection.episode = episode;
	if (id !== null && season !== null && episode !== null)
		selection.set = true;
	_setSelection();
}

function selectFirstVisibleMedia() {
	var visible = $("#media-list > div:visible");
	if (visible.size() > 0)
	{
		visible[0].click();
	}
	else
	{
		seasons.empty();
		episodes.empty();

		currentMedia = null;
		currentSeason = null;
		currentEpisode = null;
	}
}

function emptyMediaList()
{
	var tmp = mediaList;
	mediaList = mediaList.cloneNode(true);
	$(mediaList).empty();
	$(tmp).fadeTo(500, 0, function () {
		$(this).empty();
		$(this).replaceWith(mediaList);
		sortMediaList();
		_setSelection();
	});
}

function _setSelection()
{
	if (selection.id === "")
		return;
	var currentSelection = $("#" + selection.id + ":visible");
	if (currentSelection.length > 0)
	{
		currentSelection.click();
		$("body > div").animate({scrollTop: currentSelection.offset().top - 50}, 500);
	}
	else
	{
		currentId = "";
		$(detail).fadeTo(200, 0);
		$("#media-list .selected").removeClass("selected");
	}
}

function dropDownClick(elt, list, isAbsolute, isOffset, additionalOffset)
{
	if ($(list).is(":visible"))
		return;
	hideSelectOptions();
	var pos;
	var position;
	var offset;
	var width = $(elt).width();

	if (isOffset)
	{
		pos = $(elt).offset();
		offset = [-10, -10];
	}
	else
	{
		pos = $(elt).position();
		offset = [10, 10];
	}
	if (isAbsolute)
		position = "absolute";
	else
		position = "fixed";

	if (additionalOffset)
		offset = additionalOffset;

	$(list).css({
		'left': pos.left + offset[0],
		'top': pos.top + offset[1],
		'min-width': width,
		'position': position
	});
	$(list).fadeToggle(200);
	event.stopPropagation();
}

// <editor-fold defaultstate="collapsed" desc="Split bar">
function updateSplitPane()
{
	var width = mediaList.offsetWidth;
	split.css({left: width});
	var detailWidth = $(window).width() - width - 42;
	detail.style.width = detailWidth;
}

function moveSplitbar(e)
{
	if ($(window).width() - e.pageX - 42 > 666)
	{
		mediaList.style.width = e.pageX;
		updateSplitPane();
	}
	else
	{
		detail.style.width = 666;
		var width = $(window).width() - 42 - 666;
		split.css({left: width});
		mediaList.style.width = width;
	}

	return cancelEvent(e);
}
// </editor-fold>
