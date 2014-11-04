var medias = {};
var mediaList;
var detail;
var model;
var locationsList;
var split;

function updateSplitPane()
{
  var width = mediaList.offsetWidth;
  split.style.left = width;
  var detailWidth = $(window).width() - width - 42;
  detail.style.width = detailWidth;
  if (Number(detailWidth) < 874)
  {
    console.log("coucou");
  }
}

function onPageLoaded()
{
  mediaList = document.getElementById("media-list");
  detail = document.getElementById("detail");
  model = document.getElementById("model");
  locationsList = document.getElementById("locationsList");
  split = document.getElementById("split");

  updateSplitPane();
  document.onmouseup = up;
}

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
  var media = model.cloneNode(true);

  media.getElementsByTagName("h4")[0].innerText = name;
  media.children[0].style.backgroundImage = "url('" + img + "')";

  mediaList.appendChild(media);
  medias[name] = media;
}

function addLocation(name)
{
  var newLoc = locationsList.children[0].cloneNode(true);
  newLoc.innerText = name;
  locationsList.appendChild(newLoc);
}

function showDetail()
{
  $("#episodes li").click($("#watch-buttons").fadeIn);
}

function up()
{
  document.onmousemove = null;
}

function down()
{
  document.onmousemove = moveSplitbar;
}

function moveSplitbar(e)
{
  mediaList.style.width = e.pageX;
  updateSplitPane();

  return cancelEvent(e);
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
  split.style.left = mediaList.offsetWidth;
}