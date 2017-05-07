/**
 * Created by Romsahel on 15/03/2017.
 */
const Configuration = {
    Init: function ()
    {
        if (!fs.existsSync(CinemaFolder))
        {
            fs.mkdir(CinemaFolder);
        }
    },
    /**
     *
     * @param {string} path
     * @param {MediaListManager} mediaListManager
     * @constructor
     */
    Load: function (path, mediaListManager)
    {
        let medias;
        try
        {
            medias = jsonfile.readFileSync(path);
        }
        catch (err)
        {
            medias = [];
        }

        return Configuration.LoadFromObject(medias);
    },

    LoadFromObject: function (medias)
    {
        let mediaListManager = new MediaListManager();

        let sorted = Object.keys(medias).sort(function (a, b) {return medias[a].title.localeCompare(medias[b].title); })
        sorted.forEach(function (key)
        {
            const media = Cinema.Media.Cast(medias[key]);
            media.available = false;
            mediaListManager.Medias[media.id] = media;
            //mediaListManager.Add(media);
        });

        mediaListManager.ProcessQueries();

        return mediaListManager;
    },
    /**
     *
     * @param {string} path
     * @param {MediaListManager} mediaListManager
     * @constructor
     */
    Save: function (path, mediaListManager)
    {
        if (fs.existsSync(path))
        {
            const date = new Date();
            let formattedDate = date.getDate() + '-' + date.getMonth() + '-' + date.getYear();
            formattedDate += " " + date.getHours() + '.' + date.getMinutes() + "." + date.getSeconds();
            let backup_path = path.replace(/\.db/, ' ' + formattedDate + '.log');
            fs.writeFileSync(backup_path, fs.readFileSync(path));
        }

        jsonfile.writeFileSync(path, mediaListManager.Medias);
    },

    CopySettings: function (src, dest)
    {
        const SavedSettings = {
            'DetailHandler': [
                'CurrentId',
                'CurrentSeason',
                'CurrentEpisode'
            ],
            'MediaFinder': [
                'Locations',
                'MergedMedias'
            ]
        };

        for (let cat in SavedSettings)
        {
            for (let i = 0; i < SavedSettings[cat].length; i++)
            {
                const setting = SavedSettings[cat][i];

                if (!(cat in dest))
                    dest[cat] = {};

                if (cat in src)
                    dest[cat][setting] = src[cat][setting];
            }
        }
    },

    LoadSettings(path)
    {
        let settings = jsonfile.readFileSync(path);
        let loaded = {};
        Configuration.CopySettings(settings, loaded);

        MediaFinder.MergedMedias = (loaded.MediaFinder.MergedMedias) ? loaded.MediaFinder.MergedMedias : MediaFinder.MergedMedias;

        if (loaded.MediaFinder && loaded.MediaFinder.Locations)
        {
            loaded.MediaFinder.Locations.forEach(function (loc)
            {
                MediaFinder.AddMediasFromLocation(loc.path);
            });
        }

        DetailHandler.UpdateDetails(mediaListManager.Medias[loaded.DetailHandler.CurrentId], true);
        DetailHandler.GetSeasonHTML(loaded.DetailHandler.CurrentSeason).find("button").click();
        DetailHandler.GetEpisodeHTML(loaded.DetailHandler.CurrentEpisode).find("button").click();
    },

    SaveSettings(path)
    {
        const settings = {};
        Configuration.CopySettings({
            "DetailHandler": DetailHandler,
            "MediaFinder": MediaFinder
        }, settings);

        jsonfile.writeFileSync(path, settings);
    },
};

module.exports = Configuration;
