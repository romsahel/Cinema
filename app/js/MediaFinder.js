/**
 * Created by Romsahel on 17/03/2017.
 */
const MediaFinder = {
    Init: function ()
    {
        MediaFinder.Locations = [];
        MediaFinder.MergedMedias = {};

        $("#location-adder").click(this.AddLocation);
    },
    AddLocation: function ()
    {
        let dir = dialog.showOpenDialog(remote.getGlobal("mainWindow"), {properties: ['openDirectory']});

        if (dir && dir.length > 0)
        {
            MediaFinder.AddMediasFromLocation(dir[0])
        }
    },
    AddMediasFromLocation: function (folder)
    {
        MediaFinder.medias = [];
        const newLocation = {name: path.basename(folder), path: folder};
        MediaFinder.Locations.push(newLocation);
        $("#locationsList").append("<li>" + newLocation.name + "</li>");
        MediaFinder.ReadDirectory(folder, 0, null);

        let newMediaList = [];
        for (let i = 0; i < MediaFinder.medias.length; i++)
        {
            const current = MediaFinder.medias[i];
            if (!(current.id in mediaListManager.Medias))
            {
                if ((!(current.id in MediaFinder.MergedMedias)))
                {
                    current.available = true;
                    newMediaList.push(current);
                }
            }
            else
            {
                mediaListManager.Medias[current.id].available = true;
            }
        }

        //console.log("In: " + path.basename(folder));
        //console.log("Found " + MediaFinder.medias.length + " medias including " + (MediaFinder.medias.length - newMediaList.length) + " duplicates");
        //console.log(MediaFinder.medias);

        let length = newMediaList.length;
        MediaFinder.medias = newMediaList;

        for (let id in mediaListManager.Medias)
        {
            MediaFinder.medias.push(mediaListManager.Medias[id]);
        }

        MediaFinder.MergeMedias();

        //console.log("Merged " + (length - MediaFinder.medias.length) + " medias");
        //console.log(MediaFinder.medias);

        mediaListManager.AddMedias(MediaFinder.medias);
    },
    isVideo: function (filepath)
    {
        return (VideoExtensions.indexOf(path.extname(filepath.toUpperCase())) != -1);
    },
    AddSeason: function (filepath, media, depth)
    {
        if (media == null)
        {
            let newMedia = new Cinema.Media(filepath);
            this.ReadDirectory(filepath, depth + 1, newMedia);
            if (newMedia.seasons.length > 0)
            {
                MediaFinder.medias.push(newMedia);
            }
        }
        else
        {
            media.seasons.push(new Cinema.Season(filepath));
            this.ReadDirectory(filepath, depth + 1, media);
            if (media.seasons[media.seasons.length - 1].files.length == 0)
            {
                media.seasons.splice(media.seasons.length - 1, 1);
            }
        }
    },
    AddFile: function (filepath, media, depth)
    {
        if (this.isVideo(filepath))
        {
            if (media != null)
            {
                let season;
                if (depth <= 1)
                {
                    if (media.seasons.length == 0)
                    {
                        media.seasons.push(new Cinema.Season(filepath, 'Default'));
                    }
                    season = media.seasons[0];
                }
                else
                {
                    season = media.seasons[media.seasons.length - 1];
                }
                season.files.push(new Cinema.File(filepath));
            }
            else
            {

                let newMedia = new Cinema.Media(filepath.replace(path.extname(filepath), ''));
                MediaFinder.AddFile(filepath, newMedia, depth);
                MediaFinder.medias.push(newMedia);
            }
        }
    },
    ReadDirectory: function (dir, depth, media)
    {
        let files = fs.readdirSync(dir);
        for (let i = 0; i < files.length; i++)
        {
            if ((typeof files[i]) != undefined)
            {
                let filepath = dir + path.sep + files[i];
                if (fs.lstatSync(filepath).isDirectory() && depth <= MaxSearchDepth)
                {
                    this.AddSeason(filepath, media, depth);
                }
                else
                {
                    this.AddFile(filepath, media, depth);
                }
            }
        }
    },
    MergeMedias: function ()
    {
        let merged = {}

        for (var i = 0; i < MediaFinder.medias.length; i++)
        {
            const cleaned = MediaFinder.medias[i].title.replace(SeasonRegex, "").trim();
            if (!merged[cleaned])
            {
                merged[cleaned] = [];
            }
            merged[cleaned].push(MediaFinder.medias[i]);
        }

        MediaFinder.medias = [];

        for (let key in merged)
        {
            if (merged[key].length == 1)
            {
                MediaFinder.medias.push(merged[key][0])
            }
            else
            {
                let media = merged[key][0];
                let seasons = [];
                for (let i = 0; i < merged[key].length; i++)
                {
                    const current = merged[key][i];
                    if (i > 0)
                    {
                        MediaFinder.MergedMedias[current.id] = current.path;
                    }

                    if (media.img === null)
                    {
                        media.img = current.img;
                    }

                    if (!media.infos || !media.infos.Response || media.infos.Response != "True")
                    {
                        media.infos = current.infos;
                    }

                    let name = current.title.replace(key, "").trim();
                    let seasonMatch = name.match(/(Season|Saison) *\d+/g);
                    if (seasonMatch != null && seasonMatch.length > 0)
                        name = seasonMatch[0];

                    if (current.seasons.length == 1)
                    {
                        current.seasons[0].title = name;
                        seasons.push(current.seasons[0]);
                    }
                    else
                    {
                        for (let j = 1; j < merged[key][i].seasons.length; j++)
                        {
                            current.seasons[j - 1].title = name + ' (' + j + ')';
                            seasons.push(current.seasons[j]);
                        }
                    }
                }
                media.title = key;
                media.seasons = seasons.sort((a, b) => { return (a.title).localeCompare(b.title); });
                MediaFinder.medias.push(media);
            }
        }
    }
};

module.exports = MediaFinder;
