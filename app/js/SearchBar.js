/**
 * Created by Romsahel on 15/03/2017.
 */
const SearchBar = {
    Init: function ()
    {
        this.input = $("#search-bar input");
        this.input[0].oninput = function () { SearchBar.UpdateSearch(this.value.toLowerCase()) };
        this.input.focus(() => SearchBar.HandleSearchFocus(true));
        this.input.blur(() => SearchBar.HandleSearchFocus(false));
    },

    ClickFirstVisible: function()
    {
        $(".media:visible").first().click();
    },

    UpdateSearch: function (str)
    {
        const medias = $(".media");
        for (let i = 0; i < medias.length; i++)
        {
            const media = $(medias[i]);

            let isVisible = media.is(":visible");
            let callback = (str.length > 0) ? SearchBar.ClickFirstVisible : null;
            let title = media.children("h4").text().toLowerCase();

            let funShowHide = (fun) =>
            {
                if (fun == "show" && !isVisible
                   || fun == "hide" && isVisible)
                    media[fun](400, callback);
            };

            if (title.startsWith(str))
            {
                funShowHide("show");
            }
            else
            {
                funShowHide("hide");
            }
        }
    },
    HandleSearchFocus: function (isFocused)
    {
    }
};

module.exports = SearchBar;