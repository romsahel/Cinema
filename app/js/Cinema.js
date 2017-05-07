/**
 * Created by Romsahel on 17/03/2017.
 */

String.prototype.hashCode = function ()
{
    var hash = 0;
    if (this.length == 0) return hash;
    for (var i = 0; i < this.length; i++)
    {
        var character = this.charCodeAt(i);
        hash = ((hash << 5) - hash) + character;
        hash = hash & hash; // Convert to 32bit integer
    }
    return hash;
}

var Cinema = {
    Season: class {
        constructor(filepath, title)
        {
            this.title = (title) ? title : path.basename(filepath);
            this.path = filepath;

            /** @type {MediaFile[]} */
            this.files = [];
        }

        GetFirstUnwatchedEpisode()
        {
            let i = 0;
            for (; i < this.files.length; i++)
            {
                if (this.files[i].GetPosition() < 98)
                {
                    break;
                }
            }
            return { index: i % this.files.length, found: i < this.files.length };
        }
    },
    File: class {
        constructor(filepath)
        {
            this.title = path.basename(filepath);
            this.path = filepath;

            this.time = 0;
        }

        GetPosition()
        {
            return (this.position) ? this.position : 0;
        }
    },
     Media: class {
        getCleanName(filepath)
        {
            var clean_regex = /(\[.*\]|Complete|HDTV|720p|1080p|x265|AAC|x264|BRRip|HEVC|FRENCH|Bluray|The Ultimate Cut|Integrale).*/g;
            filepath = filepath.replace(/\./g, ' ');
            filepath = filepath.replace(clean_regex, '');

            this.year = this.getYear(filepath);
            if (this.year != -1)
            {
                filepath = filepath.replace(this.year.toString(), '');
            }

            var second_pass_regex = / \)| -|\( |\($|\(\)/g;
            filepath = filepath.replace(second_pass_regex, '');

            return filepath.trim();
        }

        getYear(filepath)
        {
            var year_regex = /\d{4}/g;
            var year_match = filepath.match(year_regex);
            if (year_match != null)
            {
                var year = parseInt(year_match[0]);
                if (year > 1950 && year < 2100)
                {
                    return year;
                }
            }
            return -1;
        }

        constructor(filepath)
        {
            this.id = filepath.hashCode();
            this.title = this.getCleanName(path.basename(filepath));

            this.path = filepath;
            this.img = null;

            /** @type {MediaSeason[]} */
            this.seasons = [];
        }

        GetImg()
        {
            if (this.img === null)
            {
                return (this.infos) ? (AppPosterPath + "unknown.jpg") : (AppPosterPath + "loading.gif");
            }
            return this.img;
        }

        GetPlot()
        {
            if (this.infos && this.infos.Plot)
            {
                return this.infos.Plot;
            }
            return "";
        }

        GetYear()
        {
            if (this.infos && this.infos.Year)
            {
                return this.infos.Year;
            }
            return (this.year && this.year > 0) ? this.year : "";
        }

        GetWatchedThreshold()
        {
            let isSeries = false;
            if (this.infos && this.infos.type)
            {
                isSeries = (this.infos.type == "series");
            }
            else
            {
                isSeries = (this.type && this.type == "series");
            }
            return (isSeries) ? 98 : 92;
        }

        GetFirstUnwatchedEpisode(seasonIndex)
        {
            return this.seasons[seasonIndex].GetFirstUnwatchedEpisode(this.GetWatchedThreshold());
        }

        GetFirstUnwatchedSeason()
        {
            let i = 0;
            for (; i < this.seasons.length; i++)
            {
                let unwatched = this.GetFirstUnwatchedEpisode(i);
                if (unwatched.found)
                {
                    break;
                }
            }
            return { index: i % this.seasons.length, found: i < this.seasons.length };
        }
    }
}

Cinema.Media.Cast = function(obj)
{
    obj.__proto__ = Cinema.Media.prototype;
    for (var i = 0; i < obj.seasons.length; i++)
    {
        let season = obj.seasons[i];
        season.__proto__ = Cinema.Season.prototype;
        for (var j = 0; j < season.files.length; j++)
        {
            season.files[j].__proto__ = Cinema.File.prototype;
        }
    }
    return obj;
};


module.exports = Cinema;