var medias = {};
medias["0000"] =  {'duration': '30','overview': 'A lot of girls move to New York City to "make it". Max and Caroline are just trying to make their rent. In this fun, outrageous comedy series, two girls from very different backgrounds – Max, poor from birth, and Caroline, born wealthy but down on her luck – wind up as waitresses in the same colorful Brooklyn diner and strike up an unlikely friendship that could lead to a successful business venture. All they need to do is come up with $250,000 in start-up expenses. "2 Broke Girls" infuses the classic comedy with something new, current and young, proving life can be fun – even if you’re broke.','img': '-27230530','imdb': 'tt1845307','year': '2011','genres': 'Comedy','name': '2 Broke Girls S01',};
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

function addMedia(id, array)
{
  var media = model.cloneNode(true);

  media.id = id;
  media.getElementsByTagName("h4")[0].innerText = array.name;
  media.children[0].style.backgroundImage = "url('media/posters/" + array.img + "')";

  mediaList.appendChild(media);
  medias[id] = array;
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