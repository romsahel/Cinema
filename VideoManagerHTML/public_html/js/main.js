$(document).ready(function () {
});

$('html').click(function () {
  var elements = $(".select");
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
  var pos = $(elt).parent().position();
  $(list).css({left: pos.left, top: pos.top, width: $(elt).parent().width()});
  $(list).fadeToggle("fast");
  event.stopPropagation();
}

function addMedia(name)
{
  var main = document.getElementById("main");
  var media = document.getElementById("model").cloneNode(true);

  media.id = name;
  media.getElementsByTagName("h4")[0].innerText = name;

  main.appendChild(media);
}