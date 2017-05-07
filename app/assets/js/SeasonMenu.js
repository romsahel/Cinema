/**
 * Created by Romsahel on 01/04/2017.
 */

module.exports = function()
{
    const [ContextMenu, Helper] = require ("./ContextMenu");
    const menu = new ContextMenu();

    menu.add('Open Folder', () =>  shell.showItemInFolder(DetailHandler.GetCurrentSeason().path));
    menu.add_separator();

    menu.add('Mark as seen', () => Helper.ChangeCurrentSeasonTime(100));
    menu.add('Mark as seen until there', () => Helper.ChangeAllSeasonTimeUntil(100));
    menu.add_separator();

    menu.add('Reset', () => Helper.ChangeCurrentSeasonTime(0));
    menu.add('Reset until here', () => Helper.ChangeAllSeasonTimeUntil(0));
    menu.add_separator();

    return menu.menu;
}();