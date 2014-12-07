function goToLink(elt)
{
	var url = $(elt).attr('alt');
	console.log(url);
	if (navigator.vendor === "Google Inc.")
		window.open(url);
	else
		app.openLink(url);
}

function preventEnter(e)
{
	if (e.keyCode === 13)
		return cancelEvent(e);
	return;
}

var timeout = null
function updateSearch(search)
{
	console.log("coucou")
	console.log(search !== "")
	console.log("coucou")
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
	if (search !== "")
		timeout = setTimeout(function () {
			var visible = $("#media-list > div:visible");
			console.log(visible.size());
			if (visible.size() > 0)
			{
				visible[0].click();
			}
		}, 250);
}

$(document).keypress(function (e) {
	if (!searchBar.is(":focus"))
	{
		if (e.which !== 0 && e.charCode !== 0
				&& !e.ctrlKey && !e.metaKey && !e.altKey)
		{
			if (e.keyCode === 60)
			{
				app.reload();
				return;
			}
			if ((e.keyCode | e.charCode) !== 13)
				searchBar.focus();
		}
	}
});

$(document).keyup(function (e) {
	if (e.keyCode === 13)
	{
		if (searchBar.is(":focus"))
			searchBar.blur();
	}     // enter
	if (e.keyCode === 27)
	{
		if (searchBar.is(":focus"))
			searchBar.blur();
		else if (searchBar.val() !== "")
		{
			searchBar.val("");
			updateSearch("");
		}
	}   // esc
});

function handleSearchFocus(onFocus)
{
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