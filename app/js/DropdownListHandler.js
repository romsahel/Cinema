/**
 * Created by Romsahel on 22/03/2017.
 */

const DropdownListHandler = {
    Init: function ()
    {
        $('html').click(this.HideSelectOptions);
        $('html').click(() => $("#subs-button").siblings("#subs-languages").slideUp(125));

        $("header .select").each(function ()
        {
            const holder = $('#' + $(this).attr('id') + "List");
            $(this).click(() => DropdownListHandler.DropDownClick($(this), holder));
            holder.children().each(function()
            {
                $(this).click(() => DropdownListHandler.OptionClick($(this)));
            });
        });
    },
    HideSelectOptions: function ()
    {
        $("#listsContainer > *").fadeOut(150);
        $("#watchList").fadeOut(150);
    },
    DropDownClick: function (elt, list)
    {
        if (list.is(":visible"))
            return;
        this.HideSelectOptions();

        const pos = elt.position();
        const width = elt.outerWidth();
        list.css({
            'left': pos.left,
            'top': pos.top,
            'min-width': width,
            'position': "fixed"
        });
        list.fadeToggle(200);
        event.stopPropagation();
    },
    OptionClick: function (elt)
    {
        let optionId = elt.parent().attr('id');
        optionId = optionId.substring(0, optionId.indexOf("List")) + "Option";
        $('#' + optionId).html(elt.html());
    }
};

module.exports = DropdownListHandler;