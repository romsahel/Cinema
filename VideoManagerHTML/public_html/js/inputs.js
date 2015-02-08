function goToLink(elt)
{
	var url = $(elt).attr('alt');
	console.log(url);
	if (navigator.vendor === "Google Inc.")
		window.open(url);
	else
		app.openLink(url);
}

var timeout = null;
function updateSearch(search)
{
	if (timeout !== null)
		clearTimeout(timeout);

	for (var key in medias) {
		var div = $('#' + key);
		var elt = medias[key];
		if ((elt.info.name.toLowerCase()).indexOf(search) === -1)
		{
			if (currentId === key)
			{
				currentId = "";
				$(detail).stop().fadeTo(150, 0);
				$("#media-list .selected").removeClass("selected");
				currentMedia = null;
			}
			div.stop().fadeOut(200);
		}
		else
			div.stop().fadeIn(200);
	}

	if (currentMedia === null)
		timeout = setTimeout(function () {
			selectFirstVisibleMedia();
		}, 250);
}

function focusSearch(search)
{
	if (!searchBar.is(":focus"))
	{
		searchBar.val(searchBar.val() + search);
		searchBar.focus();
		updateSearch(searchBar.val());
	}
}

function unfocusSearch(isEnter)
{
	if (searchBar.is(":focus"))
		searchBar.blur();
	else
	{
		if (isEnter)
			playMedia();
		else if (searchBar.val() !== "")
		{
			searchBar.val("");
			updateSearch("");
		}
	}
}

function selectSiblingMedia(down)
{
	if (!searchBar.is(":focus") && down !== null)
	{
		$("#files").focus();
		var selection = $("#episodes > ul.selected > li.selected");
		selection = (down) ? selection.next() : selection.prev();
		if (selection.length > 0)
		{
			var topPos = selection.offset().top - selection.parent().offset().top;
			var height = selection.height() * ((down) ? 1 : 0);

			var scrollTop = episodes.scrollTop();
			var areaHeight = episodes.height();

			selection.click();
			if (topPos < scrollTop || (topPos + height > areaHeight + scrollTop))
				episodes.scrollTop(topPos + height);
		}
	}
}

function handleSearchFocus(onFocus)
{
	searchBarParent.finish();
	if (onFocus)
	{
		searchBarParent.data('width', searchBarParent.width());
		searchBarParent.animate({width: split.position().left - ($(window).width() - (searchBarParent.offset().left + searchBarParent.width()))}, 200);
	}
	else
		searchBarParent.animate({width: searchBarParent.data('width')}, 200);
}

function up()
{
	document.onmousemove = null;
}

function down()
{
	document.onmousemove = moveSplitbar;
}

//  prevent the mousedown event to trigger any other event
function cancelEvent(e)
{
	if (e.stopPropagation)
		e.stopPropagation();
	if (e.preventDefault)
		e.preventDefault();
	e.cancelBubble = true;
	e.returnValue = false;
	return false;
}

function onResize()
{
	moveSplitbar({pageX: $(window).width() / 2});
	clearTimeout(resizingTimeout);
	resizingTimeout = setTimeout(onResizeEnd, 100);

	function onResizeEnd()
	{
		if (searchBar.is(":focus"))
			searchBarParent.animate({width: split.position().left - ($(window).width() - (searchBarParent.offset().left + searchBarParent.width()))}, 50);
	}
}

$(document).keydown(function (e)
{
	if (!searchBar.is(":focus"))
		return cancelEvent(e);
});
$(document).keypress(function (e)
{
	if (!searchBar.is(":focus"))
		return cancelEvent(e);
});