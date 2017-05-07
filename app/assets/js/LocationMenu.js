/**
 * Created by Romsahel on 01/04/2017.
 */
const LocationMenu = {
    GetSelectedLocation: function ()
    {
        return MediaFinder.Locations[$("#locationsList li:hover").index() - 1];
    },
    RemoveLocation: function ()
    {
        const loc = LocationMenu.GetSelectedLocation();
        for (var key in mediaListManager.Medias)
        {
            const media = mediaListManager.Medias[key];
            if (media.path.startsWith(MediaFinder.Locations[0].path))
            {
                console.log("Remove: " + media.title);
            }
        }
    },
    RenameLocation: function ()
    {
    },
    Build: function ()
    {
        const [ContextMenu, Helper] = require("./ContextMenu");
        const menu = new ContextMenu();

        //menu.add('Rename', LocationMenu.RenameLocation);
        menu.add('Remove', LocationMenu.RemoveLocation);

        return menu.menu;
    }
};

module.exports = LocationMenu;