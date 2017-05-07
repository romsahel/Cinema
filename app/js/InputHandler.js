/**
 * Created by Romsahel on 23/03/2017.
 */

const InputHandler = {
    Init: function ()
    {
        this.SetupDetailButtons();

        document.documentElement.addEventListener('keydown', this.OnKeydownCallback, true);
    },
    SetupDetailButtons: function ()
    {
        $(".imdb-btn").click(function (event)
        {
            eventObject.preventDefault();
        });

        $(".toggle-btn").click(function ()
        {
            $(this).toggleClass("activated");
        });

        $(".radio-buttons").click(function (eventObject)
        {
            if ($(eventObject.target).is("button"))
                InputHandler.OnRadioButtonClicked($(this), $(eventObject.target));
        });

        $("#subs-button").click(function (event)
        {
            $(this).siblings("#subs-languages").slideToggle(150);
            event.stopPropagation();
        });

        $("#subs-languages button").click(function (event)
        {
            let dir = path.dirname(DetailHandler.GetCurrentEpisode().path);
            const language = $(this).text().toLowerCase();
            let lang = null;
            if (language == "french")
            {
                lang = 'fr';
            }
            else if (language == "english")
            {
                lang = 'en';

            }
            else if (language == "spanish")
            {
                lang = 'es';
            }

            var exec = require('child_process').exec;
            var cmd = 'subliminal download -l ' + lang + ' "' + dir + '"';

            console.log(cmd);

            exec(cmd, function (error, stdout, stderr)
            {
                if (error)
                    console.error(error);
                if (stderr)
                    console.error(stderr);
                if (stdout)
                    console.log(stdout);
            });


        });

        $("#watch-button").click(function (event)
        {
            VLCAdapter.OnWatchButtonClicked();
        });

        $("#detail-seasons").click(function (eventObject)
        {
            if ($(eventObject.target).is("button"))
                InputHandler.OnSeasonClicked($(eventObject.target), 300);
        });

        $("#detail-episodes").click(function (eventObject)
        {
            if ($(eventObject.target).is("button"))
                InputHandler.OnEpisodeClicked($(eventObject.target), 300);
        });
    },
    OnSeasonClicked: function (target, animSpeed)
    {
        const index = $(target).parent().index();
        if (index == DetailHandler.CurrentSeason)
            return;

        const seasons = DetailHandler.EpisodeHolder.children();
        //DetailHandler.EpisodeHolder.fadeOut(animSpeed * 0.5, function ()
        //{
        seasons.hide(0);
        seasons.eq(index).show(0);
        //DetailHandler.EpisodeHolder.fadeIn(animSpeed);
        //});

        InputHandler.OnRadioButtonClicked(DetailHandler.SeasonHolder, $(target));
        DetailHandler.CurrentSeason = index;

        DetailHandler.CurrentEpisode = -1;
        let i = DetailHandler.Current.GetFirstUnwatchedEpisode(DetailHandler.CurrentSeason).index;
        InputHandler.OnEpisodeClicked(DetailHandler.GetEpisodeHTML(i).find("button"))
    },
    OnEpisodeClicked: function (target)
    {
        const index = $(target).parent().index();
        if (index == DetailHandler.CurrentEpisode)
            return;

        let episode = DetailHandler.GetCurrentSeason().files[index];
        VLCAdapter.SetGlobalProgressBar(episode.GetPosition());

        DetailHandler.CurrentEpisode = index;

        let parent = $("#detail-episodes").children().eq(DetailHandler.CurrentSeason);
        const scrollToValue = (DetailHandler.CurrentEpisode / parent.children().length) * parent.height();
        $("#detail-episodes").animate({scrollTop: scrollToValue}, 500);

        InputHandler.OnRadioButtonClicked(DetailHandler.EpisodeHolder, $(target));
    },
    OnRadioButtonClicked: function (parent, target)
    {
        parent.data("radioIndex", target.parent().index());
        parent.find("button").removeClass("activated");
        target.addClass("activated");
    },
    OnKeydownCallback: function (event)
    {
        let searchBar = $("#search-bar input");
        let isFocused = searchBar.is(":focus");


        if (event.key == "ArrowDown" || event.key == "ArrowUp")
        {
            const funNext = (element) => element.next();
            const funPrev = (element) => element.prev();

            const isDown = (event.key == "ArrowDown");
            const fun = (isDown) ? funNext : funPrev;
            const scrollDir = (isDown) ? 1 : -1;
            let element = null;

            if (event.ctrlKey) // change media
            {
                element = fun($(".media.activated"));
            }
            else if (event.shiftKey) // change season
            {
                element = fun($(".season .activated").parent()).children();
            }
            else // change episode
            {
                element = fun($(".episode .activated").parent()).children();
            }

            if (element.is(":visible"))
            {
                element.click();
            }
            event.preventDefault();
        }
        else
        {
            if (event.key == "Enter")
            {
                if (isFocused)
                {
                    searchBar.blur();
                }
                else
                {
                    VLCAdapter.OnWatchButtonClicked();
                }
            }
            else if (event.key == "F12")
            {
                remote.getCurrentWindow().toggleDevTools();
            }
            else
            {
                if (event.key == "Escape")
                {
                    searchBar.val("");
                    searchBar[0].oninput();
                }
                else if (!isFocused && !event.altKey && !event.ctrlKey)
                {
                    searchBar.focus();
                }
            }
        }
    },
    OnMediaClicked: function(media, element, event)
    {
        const target = $(event.target);

        if (target.is(".imdb-btn"))
        {
            shell.openExternal('www.imdb.com/title/' + mediaListManager.Medias[element.attr('id')].infos.imdbID);
        }
        else
        {
            DetailHandler.UpdateDetails(mediaListManager.Medias[media.id]);
        }
    },
};

module.exports = InputHandler