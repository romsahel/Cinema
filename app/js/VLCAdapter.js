/**
 * Created by Romsahel on 25/03/2017.
 */
const styleHTML = $("head").find("style").html();

const VLCAdapter = {
    CurrentEpisodeElt: function (iSeason, iEpisode)
    {
        return DetailHandler.EpisodeHolder.children().eq(iSeason).children().eq(iEpisode);
    },
    OnWatchButtonClicked: function ()
    {
        let episode = DetailHandler.GetCurrentEpisode();
        let html = DetailHandler.GetEpisodeHTML(DetailHandler.CurrentEpisode);

        console.log("Checking if VLC is already launched...")
        VLCRemote.IsConnected(function (isConnected, status)
        {
            if (isConnected)
            {
                if (!VLCAdapter.Playlist)
                    VLCAdapter.Playlist = {};

                VLCRemote.PlayFile(episode.path);
            }
            else
            {
                VLCAdapter.Playlist = {};
                shell.openExternal(episode.path);
            }
            if (episode.position > 99)
            {
                episode.position = 0;
                episode.time = 0;
            }

            VLCRemote.Seek(episode, (firstStatus) =>
            {
                VLCAdapter.FirstStatus = firstStatus;
                VLCAdapter.CurrentID = firstStatus.currentplid;
                VLCAdapter.Playlist[firstStatus.currentplid] = {
                    episode: episode,
                    html: html,
                    index: DetailHandler.CurrentEpisode
                };
                console.log("First episode has ID: " + VLCAdapter.CurrentID);

                if ($("#all-button").hasClass("activated"))
                {
                    VLCAdapter.AddAllFollowing();
                }

                VLCAdapter.StatusUpdateCallback(firstStatus);
            });
        });
    },
    AddAllFollowing: function ()
    {
        let season = DetailHandler.GetCurrentSeason();
        for (let i = 1; i < (season.files.length - DetailHandler.CurrentEpisode); ++i)
        {
            const episode = season.files[DetailHandler.CurrentEpisode + i];

            VLCAdapter.Playlist[VLCAdapter.CurrentID + i] = {
                episode: episode,
                html: DetailHandler.GetEpisodeHTML(DetailHandler.CurrentEpisode + i),
                index: DetailHandler.CurrentEpisode + i
            };
            VLCRemote.AddFileToPlaylist(episode.path);
        }
    },
    StatusUpdateCallback: function (firstStatus)
    {
        const stopCallback = () =>
        {
            //VLCRemote.StopWatchStatus();
        };

        VLCRemote.StartWatchStatus(4000, function (status)
        {
            let current = null;
            if (status.information != undefined && status.currentplid in VLCAdapter.Playlist)
            {
                current = VLCAdapter.Playlist[status.currentplid];
            }

            if (current == null)
            {
                stopCallback();
                return;
            }

            VLCRemote.CheckIfEpisodeIsCurrent(current.episode, status, function (episodeIsCurrent)
            {
                if (!episodeIsCurrent)
                    stopCallback();

                current.episode.time = status.time;
                current.episode.position = Math.ceil(status.position * 100);
                VLCAdapter.UpdateProgressBar(current.episode, current.html, current.index);
                if (current.index == DetailHandler.EpisodeHolder.data().radioIndex)
                {
                    VLCAdapter.SetGlobalProgressBar(current.episode.GetPosition());
                }
            });
        });
    },
    UpdateProgressBar(episode, episodeElt)
    {
        episodeElt.find("span").width(episode.GetPosition() + '%');
    },
    SetGlobalProgressBar: function (position)
    {
        $("head").find("style").html($("head").find("style").html().replace(/width: .*?%/, "width: " + position + "%"));
    }
};

module.exports = VLCAdapter;