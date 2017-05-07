/**
 * Created by Romsahel on 18/03/2017.
 */
function AnimateScrollTo(end, time)
{
    let cb = function (value, step, it, delay)
    {
        $("#detail-episodes").scrollTop(value + step * delay);
        if (it > 0)
            setTimeout(() => cb(value + step * delay, step, it - 1, delay), delay);
    };

    let start = $("#detail-episodes").scrollTop();
    let step = (end - start) / time;
    let delay = 10;
    cb(start, step, time / delay, delay);
}

const DetailHandler = {
    Init: function ()
    {
        this.SeasonModel = $("#model-container .season");
        this.EpisodeModel = $("#model-container .episode");

        this.SeasonHolder = $("#detail-seasons");
        this.EpisodeHolder = $("#detail-episodes");

        this.Poster = $('#detail-poster');
        this.Details = {
            "name": $("#detail-title"),
            "year": $("#detail-year"),
            "duration": $("#detail-duration"),
            "overview": $("#detail-description")
        };
        //UpdateDetails(mediaListManager.Medias[this.id])
    },

    GetCurrentSeason()
    {
        return DetailHandler.Current.seasons[DetailHandler.CurrentSeason];
    },

    GetCurrentEpisode()
    {
        return DetailHandler.GetCurrentSeason().files[DetailHandler.CurrentEpisode];
    },

    GetEpisodeHTML(index)
    {
        return DetailHandler.EpisodeHolder.children().eq(DetailHandler.CurrentSeason).children().eq(index)
    },

    GetSeasonHTML(index)
    {
        return DetailHandler.SeasonHolder.children().eq(index);
    },

    /**
     *
     * @param {Cinema.Media} media
     * @constructor
     */
    UpdateDetails: function (media, loading)
    {
        if (DetailHandler.Current == media)
        {
            return;
        }

        $("#detail-top").css('visibility', 'visible');

        if (DetailHandler.Current != null)
            $("#" + DetailHandler.CurrentId).removeClass("activated");

        DetailHandler.CurrentSeason = -1;

        DetailHandler.Current = media;
        DetailHandler.CurrentId = media.id;

        $("#" + DetailHandler.CurrentId).addClass("activated");

        this.Details.name.text(media.title);
        this.Details.overview.text(media.GetPlot());
        this.Poster.css("background-image", "url(" + media.GetImg().replace(/\\/g, '\\\\') + ")");

        this.UpdateDetailFiles(media);

        if (!loading)
        {
            VLCAdapter.SetGlobalProgressBar(media.seasons[0].files[0].GetPosition());
            let unwatched = media.GetFirstUnwatchedSeason().index;
            InputHandler.OnSeasonClicked(DetailHandler.SeasonHolder.children().eq(unwatched).children("button"), 0);
        }
    },

    UpdateDetailFiles: function (media)
    {
        this.SeasonHolder.empty();
        this.EpisodeHolder.empty();

        let newSeasonsHtml = "";
        let newEpisodesHtml = "";
        for (let i = 0; i < media.seasons.length; i++)
        {
            let currentSeason = media.seasons[i];
            let seasonElement = this.SeasonModel.clone(false);
            seasonElement.html(seasonElement.html().replace("__title__", currentSeason.title));

            newSeasonsHtml += seasonElement[0].outerHTML;

            newEpisodesHtml += "<div>";
            for (let j = 0; j < currentSeason.files.length; j++)
            {
                let currentEpisode = currentSeason.files[j];
                let episodeElement = this.EpisodeModel.clone(false);
                episodeElement.html(episodeElement.html().replace("__title__", currentEpisode.title));

                episodeElement.find("span").width(currentEpisode.GetPosition() + '%');

                newEpisodesHtml += episodeElement[0].outerHTML;
            }
            newEpisodesHtml += "</div>";
        }

        this.SeasonHolder.html(newSeasonsHtml);
        this.EpisodeHolder.html(newEpisodesHtml);
    },

    UpdateDetailGenres: function (genres)
    {
        if (genres && genres.length > 0)
            $("#detail-genres a").text(genres[0]);
        else
            $("#detail-genres a").text("N/A");

        $("#detailsGenreList").empty();
        $("#detail-genres").unbind("click");
        if (genres && genres.length > 1)
        {
            $("#detail-genres").click(function ()
            {
                dropDownClick(this, '#detailsGenreList', true, true);
            });
            $("#detail-genres").css({'cursor': 'pointer'});

            $("#detail-genres span").show();
            let genresToAppend = "";
            for (let i = 0; i < genres.length; i = i + 1)
                genresToAppend = genresToAppend + "<li onclick=\"optionClick(this)\">" + genres[i] + "</li>";
            $("#detailsGenreList").append(genresToAppend);
        }
        else
        {
            $("#detail-genres span").hide();
            $("#detail-genres").click(null);
            $("#detail-genres").css({'cursor': 'default'});
        }
    },

    UpdateIndicators: function ()
    {
        let indicator = app.getEpisodeIndicator(currentMedia.id, currentSeason.key, currentEpisode.key);
        $("#availability").text(indicator[0]);
        $("#subtitles-indicator").text(indicator[1]);
    }
}

module.exports = DetailHandler;