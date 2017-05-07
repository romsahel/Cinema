/**
 * Created by Romsahel on 01/04/2017.
 */
const electron = require('electron')
const {Menu, MenuItem} = electron.remote

const ContextMenuHelper = {

    ChangeEpisodeTime: function (episode, episodeElement, time, isCurrent)
    {
        episode.position = time;
        episode.time = time;
        episodeElement.find("span").width(time + '%');
        if (isCurrent)
            VLCAdapter.SetGlobalProgressBar(0);
    },

    ChangeAllEpisodesFromSeasonTimeUntilIndex: function (time, season, index)
    {
        for (let i = 0; i <= index; i++)
        {
            ContextMenuHelper.ChangeEpisodeTime(
                season.files[i],
                DetailHandler.GetEpisodeHTML(i),
                time,
                true);
        }
        VLCAdapter.SetGlobalProgressBar(time);
    },

    ChangeAllEpisodesTimeUntil: function (time)
    {
        const season = DetailHandler.GetCurrentSeason();
        ContextMenuHelper.ChangeAllEpisodesFromSeasonTimeUntilIndex(time, season, DetailHandler.CurrentEpisode);
    },

    ChangeCurrentEpisodeTime: function (time)
    {
        ContextMenuHelper.ChangeEpisodeTime(
            DetailHandler.GetCurrentEpisode(),
            DetailHandler.GetEpisodeHTML(DetailHandler.CurrentEpisode),
            time,
            true
        );
    },

    ChangeCurrentSeasonTime: function(time)
    {
        const season = DetailHandler.GetCurrentSeason();
        ContextMenuHelper.ChangeAllEpisodesFromSeasonTimeUntilIndex(time, season, season.files.length - 1);
    },

    ChangeAllSeasonTimeUntil: function(time)
    {
        for (let i = 0; i <= DetailHandler.CurrentSeason; i++)
        {
            const season = DetailHandler.Current.seasons[i];
            ContextMenuHelper.ChangeAllEpisodesFromSeasonTimeUntilIndex(time, season, season.files.length - 1);
        }
    }
};

class ContextMenu {
    constructor()
    {
        this.menu = [];
    }

    add(label, click_callback)
    {
        let item = {
            label: label,
            fun: '(' + click_callback.toString() + ')' + "()"
        };
        this.menu.push(item);
        return item;
    }

    add_separator()
    {
        let item = {type: 'separator'};
        this.menu.push(item)
        return item;
    }
}

module.exports = [ContextMenu, ContextMenuHelper];