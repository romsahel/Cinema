/**
 * Created by Romsahel on 24/03/2017.
 */
const Sorter = {
    Init: function ()
    {
        this.MediaList = $("#media-list");
        this.CurrentSorting = "Name";

        $("#sortList > li").each(function ()
        {
            $(this).click(Sorter.SortMediaList)
        });
    },
    SortMediaList: function ()
    {
        const newSorting = $(this).html();

        const reverse = (newSorting === Sorter.CurrentSorting);
        let sorter = null;
        if (newSorting === "Name")
        {
            sorter = function(m1, m2) { return m1.title.localeCompare(m2.title); }
        }
        else if (newSorting === "Year")
        {
            sorter = function(m1, m2) { return parseInt(m2.year) - parseInt(m1.year); }
        }
        else if (newSorting === "Rating")
        {
            sorter = function(m1, m2) { return parseFloat(m2.infos.imdbRating) - parseFloat(m1.infos.imdbRating); }
        }

        if (sorter != null)
        {
            $("#media-list .media").sort(function(a, b)
            {
                var m1 = mediaListManager.Medias[$(a).attr('id')];
                var m2 = mediaListManager.Medias[$(b).attr('id')];

                return sorter((reverse) ? m2 : m1, (reverse) ? m1 : m2);
            })
            .appendTo($("#media-list"));
        }

        Sorter.CurrentSorting = newSorting;
    }
};

module.exports = Sorter;