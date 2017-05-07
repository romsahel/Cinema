/**
 * Created by Romsahel on 25/03/2017.
 */

const host = "localhost";
const port = "8080";
const username = "";
const password = "vlcremote";

function RequestStatus(cmd, successCallback)
{
    return Request("status.json", cmd, successCallback);
}

function RequestPlaylist(successCallback)
{
    return Request("playlist.json", "", successCallback);
}

function Request(target, cmd, successCallback)
{
    let url = "http://"
        + username + ":" + password + '@'
        + host + ":" + port
        + "/requests/" + target
        + cmd;

    return $.getJSON(url, null, successCallback)
}

function Send(cmd, val, id, input, successCallback)
{
    let url = "";
    if (cmd != null)
        url = url + "?command=" + cmd;

    if (val != null)
        url = url + "&val=" + val;

    if (id != null)
        url = url + "&id=" + id;

    if (input != null)
        url = url + "&input=" + input;

    return RequestStatus(url, successCallback);
}

const VLCRemote = {
    Status: class {

        constructor()
        {
            this.fullscreen = false;
            this.stats = {
                "inputbitrate": 0,
                "sentbytes": 0,
                "lostabuffers": 0,
                "averagedemuxbitrate": 0,
                "readpackets": 0,
                "demuxreadpackets": 0,
                "lostpictures": 0,
                "displayedpictures": 0,
                "sentpackets": 0,
                "demuxreadbytes": 0,
                "demuxbitrate": 0,
                "playedabuffers": 0,
                "demuxdiscontinuity": 0,
                "decodedaudio": 0,
                "sendbitrate": 0,
                "readbytes": 0,
                "averageinputbitrate": 0,
                "demuxcorrupted": 0,
                "decodedvideo": 0,
            };
            this.aspectratio = "default";
            this.audiodelay = 0;
            this.apiversion = 3;
            this.currentplid = 4;
            this.time = 0;
            this.volume = 0;
            this.length = 0;
            this.random = false;
            this.audiofilters = {
                "filter_0": ""
            };
            this.rate = 1;
            this.videoeffects = {
                "hue": 0,
                "saturation": 1,
                "contrast": 1,
                "brightness": 1,
                "gamma": 1
            };
            this.state = "",
                this.loop = false;
            this.version = "";
            this.position = 0;
            this.information = {
                "chapter": 1,
                "chapters": [
                    0,
                    1,
                    2,
                    3
                ],
                "title": 0,
                "category": {
                    "meta": {
                        "showName": "",
                        "filename": "",
                        "title": "",
                        "episodeNumber": "",
                        "seasonNumber": ""
                    },
                },
                "titles": [
                    0
                ]
            };
            this.repeat = false;
            this.subtitledelay = 0;
            this.equalizer = [];
        }
    },
    StartWatchStatus: function (delay, callback)
    {
        VLCRemote.Delay = delay;
        VLCRemote.StatusUpdatedCallback = function (status)
        {
            console.log(status);

            callback(status);
            VLCRemote.PreviousStatus = status;

            console.log("VLCRemote.Loop: " + VLCRemote.Loop);
            if (VLCRemote.Loop)
            {
                setTimeout(VLCRemote.GetStatusLoop, VLCRemote.Delay);
            }
        };

        VLCRemote.StatusFailedCallback = function ()
        {
            console.log("VLCRemote.StatusFailedCallback");
            return VLCRemote.Loop = false;
        };
        if (!VLCRemote.Loop)
        {
            VLCRemote.Loop = true;
            setTimeout(VLCRemote.GetStatusLoop, VLCRemote.Delay);
        }
    },
    StopWatchStatus: function ()
    {
        VLCRemote.Loop = false;
    },
    GetStatusLoop: function ()
    {
        RequestStatus('', VLCRemote.StatusUpdatedCallback)
        .fail(VLCRemote.StatusFailedCallback);
    },
    GetStatus: function (successCallback, failCallback = null)
    {
        RequestStatus('', successCallback)
        .fail(failCallback);
    },
    IsConnected: function (callback)
    {
        RequestStatus('', (status) => callback(true, status))
        .fail(() => callback(false, null));
    },
    WaitForConnexion: function (callback)
    {
        VLCRemote.IsConnected(function (isConnected, status)
        {
            console.log("Checking connection: " + isConnected);
            if (!isConnected)
                setTimeout(() => VLCRemote.WaitForConnexion(callback), 200);
            else
                callback(status);
        });
    },
    Seek: function (episode, OnSuccessCallback = null)
    {
        let callback = null;
        const repeatCallback = () => setTimeout(() => VLCRemote.Seek(episode, OnSuccessCallback), 200);

        if (OnSuccessCallback != null)
        {
            callback = function (status)
            {
                if (status.time == episode.time)
                {
                    VLCRemote.CheckIfEpisodeIsCurrent(episode, status, function (success)
                    {
                        if (success)
                        {
                            OnSuccessCallback(status);
                        }
                        else
                        {
                            repeatCallback();
                        }
                    });
                }
                else
                {
                    repeatCallback();
                }
            };
        }

        //if (episode.time <= 1)
        //{
        //    VLCRemote.WaitForConnexion(OnSuccessCallback);
        //}
        //else
        {
            Send("seek", episode.time, null, null, callback).fail(repeatCallback);
        }
    },
    PlayFile: function (filename)
    {
        Send("pl_empty");
        let url = "file:///" + filename.replace(/\\/g, '/');
        Send("in_play", null, null, encodeURI(url));
    },
    AddFileToPlaylist: function (filename)
    {
        let url = "file:///" + filename.replace(/\\/g, '/');
        Send("in_enqueue", null, null, encodeURI(url));
    },
    GetPlaylist: function (callback)
    {
        return RequestPlaylist(callback);
    },
    CheckIfEpisodeIsCurrent: function (episode, status, callback)
    {
        const IS_VLC_CUSTOM_LUA = true;
        if (IS_VLC_CUSTOM_LUA)
        {
            let episodeIsCurrent = false;
            episodeIsCurrent = (path.basename(status.information.category.meta.filename) == path.basename(episode.path));
            console.log(path.basename(status.information.category.meta.filename));
            callback(episodeIsCurrent);
        }
        else
        {
            return RequestPlaylist(function (playlist)
            {
                let episodeIsCurrent = false;
                const items = playlist.children[0].children;
                for (let i = 0; i < items.length; ++i)
                {
                    if ("current" in items[i])
                    {
                        if (path.basename(decodeURI(items[i].uri)) == path.basename(episode.path))
                        {
                            episodeIsCurrent = true;
                        }
                        break;
                    }
                }
                callback(episodeIsCurrent);
            });
        }
    }
};

module.exports = VLCRemote;