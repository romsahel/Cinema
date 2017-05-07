const [ContextMenu, Helper] = require ("./ContextMenu");
const EpisodeMenu = require("./EpisodeMenu");
const SeasonMenu = require("./SeasonMenu");
const LocationMenuModule = require("./LocationMenu");
const LocationMenu = LocationMenuModule.Build();


window.addEventListener('contextmenu', (e) =>
{
    e.preventDefault()
    // e.target is the underlying HTML element,
    // you can enable/disable non-OS items based on this before displaying
    // (i.e) example of menu item enabled for links only
    //linkMenuItem.enabled = (e.target.localName === 'a')

    let target = $(e.target)
    let shouldClick = true;
    let currentMenu = null;

    if (target.is('button'))
    {
        if (target.parent().hasClass('episode'))
        {
            currentMenu = EpisodeMenu;
        }
        if (target.parent().hasClass('season'))
        {
            currentMenu = SeasonMenu;
        }
    }
    else if (target.hasClass("media"))
    {
    }
    else if (target.is('li') && target.parent().is('#locationsList'))
    {
        currentMenu = LocationMenu;
        shouldClick = false;
        e.preventDefault();
    }

    if (shouldClick)
    {
        target.click();
    }

    if (currentMenu != null)
    {
        ipcRenderer.send('context-menu', currentMenu);
    }
    else
    {
        console.log(target);
    }

    //let contextMenu = Menu.buildFromTemplate(currentMenu);
    //contextMenu.popup(electron.remote.getCurrentWindow());

}, false);

ipcRenderer.on('context-menu-response', function(event, data)
{
    eval(data);
});