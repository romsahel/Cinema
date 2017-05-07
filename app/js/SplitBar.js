/**
 * Created by Romsahel on 17/03/2017.
 */

const SplitBar = {
    Init: function()
    {
        this.split = $("#split");
        this.leftPanel = $("#media-list");
        this.rightPanel = $("#detail");

        this.text = $("#detail-text");
        this.poster = $("#detail-poster");
        this.buttons = $(".button-holder");

        this.split
        .mousedown(function ()
        {
            $("html").mousemove(function (event)
            {
                SplitBar.MoveSplitPane(event.pageX);
            })
            .mouseup(function ()
            {
                $(this).off("mousemove");
                $(this).off("mouseup");
            });
        })
        $(window).resize(() => this.MoveSplitPane(-1, true));

        this.MoveSplitPane($(window).width() / 3, true);
    },
    down: function ()
    {
        document.onmousemove = moveSplitbar;
    },
    MoveSplitPane: function (pageX, force)
    {
        pageX = (pageX == -1) ? this.leftPanel.width() : pageX;
        let x = (pageX <= 192) ? 192 : pageX;
        this.split.css("left", x);
        this.leftPanel.width(x);
        this.rightPanel.css("margin-left", x);

        let posterWidth = this.poster.width();

        let width = $(window).width() - x;
        if (width <= 600 || force)
        {
            let factor = (width / 700);

            let posterHeight = 0;
            const newPosterWidth = 195 * factor;
            if (newPosterWidth <= 200)
            {
                posterWidth = newPosterWidth;
                posterHeight = 289 * factor;

                this.poster.width(posterWidth);
                this.poster.height(posterHeight);
            }
            else
            {
                posterHeight = this.poster.height();
            }

            this.text.height(posterHeight - 50);
            this.buttons.css("margin-left", posterWidth + 5)

            $("#detail-files").css("margin-left", posterWidth - $("#detail-seasons").width());
            $("#detail-episodes").height(this.leftPanel.height() - posterHeight - 80);
            $("#detail-seasons").height(this.leftPanel.height() - posterHeight - 80);
        }

        $("#detail-episodes").width(width - posterWidth - 50);
        this.text.width(width - posterWidth - 50);
    }
}

module.exports = SplitBar;