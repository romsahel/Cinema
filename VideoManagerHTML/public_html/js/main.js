var medias = {};
var mediaList;
var detail;
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
}

$('html').click(function () {
  var elements = $("#listsContainer > .select");
  for (var i = 0; i < elements.length; i++) {
	elements.fadeOut("fast");
  }
});

$(document).keypress(function (e) {
  var keycode = e.keyCode;

  if (!searchBar.is(":focus"))
  {
	var valid = (keycode > 47 && keycode < 58) || // number keys
			(keycode === 32) || // spacebar & return key(s) (if you want to allow carriage returns)
			(keycode > 64 && keycode < 91) || // letter keys
			(keycode > 95 && keycode < 112) || // numpad keys
			(keycode > 185 && keycode < 193) || // ;=,-./` (in order)
			(keycode > 218 && keycode < 223);   // [\]' (in order)
	if (valid)
	  searchBar.focus();
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
	else
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

function updateSearch(search)
{
  for (var key in medias) {
	var div = $('#' + key);
	var elt = medias[key];
	if ((elt.name).indexOf(search) === -1)
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

function changeText(element, text)
{
  element.fadeOut(200, function () {
	$(this).text(text).fadeIn(200);
  });
}

function showDetail(elt)
{
  var current = medias[elt.id];
  for (var key in detailsToUpdate)
	changeText(detailsToUpdate[key], current[key]);

  $('#detail-poster').attr('src', 'media/posters/' + current.img);

  $("#detail").show();
}

function up()
{
  document.onmousemove = null;
}

function down()
{
  document.onmousemove = moveSplitbar;
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
  updateSplitPane();
  clearTimeout(resizingTimeout);
  resizingTimeout = setTimeout(onResizeEnd, 100);

  function onResizeEnd()
  {
	if (searchBar.is(":focus"))
	  searchBarParent.animate({width: split.position().left - ($(window).width() - (searchBarParent.offset().left + searchBarParent.width()))}, 50);
  }
}

// <editor-fold defaultstate="collapsed" desc="debug function">
function debug()
{
  addMedia('527773278', {'duration': '30', 'overview': 'A lot of girls move to New York City to "make it". Max and Caroline are just trying to make their rent. In this fun, outrageous comedy series, two girls from very different backgrounds – Max, poor from birth, and Caroline, born wealthy but down on her luck – wind up as waitresses in the same colorful Brooklyn diner and strike up an unlikely friendship that could lead to a successful business venture. All they need to do is come up with $250,000 in start-up expenses. "2 Broke Girls" infuses the classic comedy with something new, current and young, proving life can be fun – even if you’re broke.', 'img': 'unknown.jpg', 'imdb': 'tt1845307', 'year': '2011', 'genres': 'Comedy', 'name': '2 Broke Girls Season 2', })
  addMedia('-479429750', {'duration': '30', 'overview': 'A lot of girls move to New York City to "make it". Max and Caroline are just trying to make their rent. In this fun, outrageous comedy series, two girls from very different backgrounds – Max, poor from birth, and Caroline, born wealthy but down on her luck – wind up as waitresses in the same colorful Brooklyn diner and strike up an unlikely friendship that could lead to a successful business venture. All they need to do is come up with $250,000 in start-up expenses. "2 Broke Girls" infuses the classic comedy with something new, current and young, proving life can be fun – even if you’re broke.', 'img': 'unknown.jpg', 'imdb': 'tt1845307', 'year': '2011', 'genres': 'Comedy', 'name': '2 Broke Girls Season 3', })
  addMedia('1387505398', {'duration': '30', 'overview': 'A lot of girls move to New York City to "make it". Max and Caroline are just trying to make their rent. In this fun, outrageous comedy series, two girls from very different backgrounds – Max, poor from birth, and Caroline, born wealthy but down on her luck – wind up as waitresses in the same colorful Brooklyn diner and strike up an unlikely friendship that could lead to a successful business venture. All they need to do is come up with $250,000 in start-up expenses. "2 Broke Girls" infuses the classic comedy with something new, current and young, proving life can be fun – even if you’re broke.', 'img': 'unknown.jpg', 'imdb': 'tt1845307', 'year': '2011', 'genres': 'Comedy', 'name': '2 Broke Girls S01', })
  addMedia('-2090594287', {'duration': '95', 'overview': 'Tom (Joseph Gordon-Levitt), greeting-card writer and hopeless romantic, is caught completely off-guard when his girlfriend, Summer (Zooey Deschanel), suddenly dumps him. He reflects on their 500 days together to try to figure out where their love affair went sour, and in doing so, Tom rediscovers his true passions in life.', 'img': 'unknown.jpg', 'imdb': 'tt1022603', 'year': '2009', 'genres': 'Comedy Drama Romance Indie', 'name': '500 Days of Summer', })
  addMedia('2048989698', {'duration': '60', 'overview': 'The series sees S.H.I.E.L.D. Agent Phil Coulson putting together a small team of S.H.I.E.L.D. agents to handle strange new cases.  In the first season, Coulson and his team investigate Project Centipede and their leader, "The Clairvoyant", eventually uncovering that Project Centipede is backed by Hydra, and they must deal with Hydra\'s infiltration of, and the destruction of, S.H.I.E.L.D. In the second season, Coulson and his team look to restore trust from the government and public following S.H.I.E.L.D.\'s collapse.', 'img': 'unknown.jpg', 'imdb': 'tt2364582', 'year': '2013', 'genres': 'Action Drama Science Fiction Adventure Fantasy', 'name': 'Agents of S H I E L D S01E01-22', })
  addMedia('-726140210', {'duration': '30', 'overview': 'Six young people, on their own and struggling to survive in the real world, find the companionship, comfort and support they get from each other to be the perfect antidote to the pressures of life.', 'img': 'unknown.jpg', 'imdb': 'tt0108778', 'year': '1994', 'genres': 'Comedy', 'name': 'Friends Season 1', })
  addMedia('796752840', {'duration': '30', 'overview': 'Six young people, on their own and struggling to survive in the real world, find the companionship, comfort and support they get from each other to be the perfect antidote to the pressures of life.', 'img': 'unknown.jpg', 'imdb': 'tt0108778', 'year': '1994', 'genres': 'Comedy', 'name': 'Friends Season 10', })
  addMedia('-194161654', {'duration': '30', 'overview': 'Six young people, on their own and struggling to survive in the real world, find the companionship, comfort and support they get from each other to be the perfect antidote to the pressures of life.', 'img': 'unknown.jpg', 'imdb': 'tt0108778', 'year': '1994', 'genres': 'Comedy', 'name': 'Friends Season 9', })
  addMedia('-477789090', {'duration': '60', 'overview': 'The service offered by The Lightman Group is truly unique. Simply stated, they can tell if you\'re lying. It\'s not the words you speak that give you away, it\'s what your body and face have to say. Dr. Cal Lightman and his team are experts at reading micro-expressions, the fleeting tics that express, non-verbally, what we are really feeling. With their finely honed interviewing and investigating skills, they have an uncanny ability to dig up the truth.', 'img': 'unknown.jpg', 'imdb': 'tt1235099', 'year': '2009', 'genres': 'Drama Crime', 'name': 'Lie To Me Season 1, 2 & 3', })
  addMedia('957033482', {'duration': '84', 'overview': 'In 1979 Clive Sinclair, British inventor of the pocket calculator, frustrated by the lack of home investment in his project,the electric car, also opposes former assistant Chris Curry\'s belief that he can successfully market a micro-chip for a home computer. A parting of the ways sees Curry, in partnership with the Austrian Hermann Hauser and using whizz kid Cambridge students, set up his own, rival firm to Sinclair Radionics, Acorn. Acorn beat Sinclair to a lucrative contract supplying the BBC with machines for a computer series. From here on it is a battle for supremacy to gain the upper hand in the domestic market.', 'img': 'unknown.jpg', 'imdb': 'tt1459467', 'year': '2009', 'genres': 'Adventure Drama History', 'name': 'Micro Expression', })
  addMedia('860752843', {'duration': '124', 'overview': 'Sin City is a neo-noir crime thriller based on Frank Miller\'s graphic novel series of the same name.The film is primarily based on three of Miller\'s works: The Hard Goodbye, about a man who embarks on a brutal rampage in search of his one-time sweetheart\'s killer; The Big Fat Kill, which focuses on a street war between a group of prostitutes and a group of mercenaries; and That Yellow Bastard, which follows an aging police officer who protects a young woman from a grotesquely disfigured serial killer.', 'img': 'unknown.jpg', 'imdb': 'tt0401792', 'year': '2005', 'genres': 'Action Crime Thriller', 'name': 'Sin City', })
}
// </editor-fold>
