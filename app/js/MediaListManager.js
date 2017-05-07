/**
 * Created by Romsahel on 15/03/2017.
 */

class MediaListManager {
    constructor()
    {
        this.MediaModel = $("#model-container .media");
        this.Medias = {}
        this.CurrentMedia = null;
        this.queryList = []
    }

    AddMedias(mediaList)
    {
        $("#media-list").empty();
        mediaListManager.Medias = {};

        let sortedIndexes = Object.keys(mediaList).sort(function (a, b) {return mediaList[a].title.localeCompare(mediaList[b].title); })
        for (var i = 0; i < sortedIndexes.length; i++)
        {
            mediaListManager.Add(mediaList[sortedIndexes[i]]);
        }
        mediaListManager.ProcessQueries();
    }

    /**
     *
     * @param {Cinema.Media} media
     * @constructor
     */
    Add(media)
    {
        let mediaElement = this.MediaModel.clone(false);

        mediaElement.attr('id', media.id);
        mediaElement.click(function (event)
        {
            return InputHandler.OnMediaClicked(media, $(this), event);
        });

        $(mediaElement).children("img").attr("src", media.GetImg());
        this.UpdateInfos(media, mediaElement);

        // updateGenres(array.info.genres);
        this.Medias[media.id] = media;
        $("#media-list").append(mediaElement);
    }

    UpdateInfos(media, mediaElement)
    {
        let title = mediaElement.find("h4");
        title.text(media.title);

        let rateToAppend = "";
        mediaElement.find(".stars").empty();
        if (media.img != null)
        {
            $(mediaElement).children("img.poster").attr("src", media.img);
        }

        if (media.infos)
        {
            const rating = media.infos.imdbRating;
            if (rating)
                rateToAppend = this.CreateStars(rating) + '\n<p>' + rating + "/10" + '</p>';
            else
                rateToAppend = "<p>No rating found...</p>";

            title.html(title.html() + "<span>" + media.GetYear() + "</span>");
        }
        else
        {
            rateToAppend = '\n<p>Fetching data...</p>';
            this.queryList.push({obj: media, html: mediaElement});
        }

        mediaElement.find(".stars")[0].innerHTML = rateToAppend;
    }

    CreateStars(imdbRating)
    {
        let remainingStars = 5;
        let rateToAppend = "";
        let rating = parseFloat(imdbRating) / 2;
        let max = Math.floor(rating);
        rating = rating - max;
        for (; max > 0; max--)
        {
            rateToAppend = rateToAppend + '<span class="full_star"></span>';
            remainingStars--;
        }
        if (rating > 0.3)
        {
            rateToAppend = rateToAppend + '<span class="half_star"></span>';
            remainingStars--;
        }
        for (; remainingStars > 0; remainingStars--)
        {
            rateToAppend = rateToAppend + '<span class="empty_star"></span>';
        }
        return rateToAppend;
    }

    ProcessQueries()
    {
        if (this.queryList.length > 0)
            this.ProcessQuery(0);
    }

    ProcessQuery(index)
    {
        const query = this.queryList[index];
        if (query != null)
        {
            this.queryList[index] = null;

            let title = query.obj.title.replace(SeasonRegex, '');
            if (title != query.obj.title)
            {
                query.obj.type = "series";
            }
            title = title.trim().replace(/ /g, "+");

            this.SearchTitleOMDBAPI(query, title);
        }

        if ((index + 1) < this.queryList.length)
        {
            setTimeout(() => this.ProcessQuery(index + 1), 5000);
        }
        else
        {
            this.queryList = [];
        }
    }

    SearchCustomGoogle(query)
    {
        const manager = this;
        let search = query.obj.title.replace(SeasonRegex, '');
        search += (query.obj.year != -1) ? " " + query.obj.year : "";
        search += (query.obj.type) ? " " + query.obj.type : "";

        const APIKey = "AIzaSyAc47xwOxJGizAIh3psApNFzb5R8O6yRNc";
        const engineID = "010310575180467592004:ye4wgqlhtto";
        let url = "https://www.googleapis.com/customsearch/v1?key=" + APIKey + "&cx=" + engineID;
        url += "&q=" + escape(search.replace(/ /g, '+'));

        $.getJSON(url, function (data)
        {
            console.log(url);
            console.log(data);
            if (data && data.items && data.items.length > 0)
            {
                const imdbID = data.items[0].link.replace(/.*?(tt\d+?)\//, "$1");
                manager.SearchIDOMDBAPI(query, imdbID);
            }
            else
            {
                manager.ProcessOMDBApiQuery({"Response":"False","Error":"Movie not found!"}, query);
            }
        });
    }

    SearchIDOMDBAPI(query, id)
    {
        const manager = this;
        let url = "http://www.omdbapi.com/?i=" + id;
        url += "&plot=full";

        console.log(url);
        $.getJSON(url, (data) => manager.ProcessOMDBApiQuery(data, query));
    }

    SearchTitleOMDBAPI(query, title)
    {
        const manager = this;
        let url = "http://www.omdbapi.com/?t=" + escape(title);
        url += (query.obj.year != -1) ? "&y=" + query.obj.year : "";
        url += (query.obj.type) ? "&type=" + query.obj.type : "";

        console.log(url);
        $.getJSON(url, (data) => manager.ProcessOMDBApiQuery(data, query));
    }

    ProcessOMDBApiQuery(data, query)
    {
        const manager = this;
        console.log(data);
        query.obj.infos = data;

        if (data.Response && data.Response == "True")
        {
            query.obj.isUnknown = false;

            let path_to_img = UserPosterFolder + query.obj.id + ".jpg";
            let file = fs.createWriteStream(path_to_img);

            let downloader = (data.Poster.startsWith("https")) ? https : http;
            let request = downloader.get(data.Poster, function (response)
            {
                response.pipe(file);
                file.on('finish', function ()
                {
                    file.close();
                    query.obj.img = path_to_img;
                    manager.UpdateInfos(query.obj, query.html);
                });
            });
        }
        else
        {
            if (query.obj.isUnknown)
            {
                query.obj.img = AppPosterPath + "unknown.jpg";
                manager.UpdateInfos(query.obj, query.html);
            }
            else
            {
                query.obj.isUnknown = true;
                manager.SearchCustomGoogle(query);
            }
        }
    }
}

module.exports = MediaListManager;