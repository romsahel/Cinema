$(document).ready(function ()
{
  showDetail();
});

$('html').click(function () {
  var elements = $("#listsContainer > .select");
  for (var i = 0; i < elements.length; i++) {
	elements.fadeOut("fast");
  }
});

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

function addMedia(name, img)
{
  var main = document.getElementById("main");
  var media = document.getElementById("model").cloneNode(true);

  media.id = name;
  media.getElementsByTagName("h4")[0].innerText = name;
  media.children[0].style.backgroundImage = "url('" + img + "')";

  main.appendChild(media);
}

function addLocation(name)
{
  var list = document.getElementById("locationsList");
  var newLoc = list.children[0].cloneNode(true);
  newLoc.innerText = name;
  list.appendChild(newLoc);
}

function showDetail()
{
  $("#episodes li").click(function ()
  {
	$("#watch-buttons").fadeIn(200);
  });
}