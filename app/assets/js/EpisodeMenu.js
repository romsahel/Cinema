/**
 * Created by Romsahel on 01/04/2017.
 */
module.exports = function()
{
    const [ContextMenu, Helper] = require ("./ContextMenu");
    const menu = new ContextMenu();

    menu.add('Open Folder', () => shell.showItemInFolder(DetailHandler.GetCurrentEpisode().path));
    menu.add_separator();

    menu.add('Mark as seen', () => Helper.ChangeCurrentEpisodeTime(100));
    menu.add('Mark as seen until there', () => Helper.ChangeAllEpisodesTimeUntil(100));
    menu.add_separator();

    menu.add('Reset', () => Helper.ChangeCurrentEpisodeTime(0));
    menu.add('Reset until here', () => Helper.ChangeAllEpisodesTimeUntil(0));
    menu.add_separator();

    return menu.menu;
}();