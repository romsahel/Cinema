/**
 * Created by Romsahel on 15/03/2017.
 */
let mediaListManager = null;

class Logger {
    constructor(cons)
    {
        this.console = cons;
    }

    send(channel, msg)
    {
        this.console[channel](msg);
        if (typeof msg == "object")
        {
            msg = JSON.stringify(msg);
        }
        ipcRenderer.send('__ELECTRON_LOG__', [channel, msg]);
    }

    log(msg) { this.send("log", msg); }

    error(msg) { this.send("error", msg); }

    debug(msg) { this.send("debug", msg); }

    warn(msg) { this.send("warn", msg); }

    info(msg) { this.send("info", msg); }
}


const __console__ = console;
console = new Logger(__console__);

$(function ()
{
    SearchBar.Init();
    SplitBar.Init();
    InputHandler.Init();
    DropdownListHandler.Init();
    MediaFinder.Init();
    DetailHandler.Init();
    Configuration.Init();
    Sorter.Init();

    mediaListManager = Configuration.Load(db_file, mediaListManager);
    Configuration.LoadSettings(settings_file);

    window.onbeforeunload = (e) =>
    {
        Configuration.Save(db_file, mediaListManager);
        Configuration.SaveSettings(settings_file);
    };

    window.onfocus = function ()
    {
        if (VLCAdapter.FirstStatus)
        {
            console.log("Start monitoring...");
            VLCAdapter.StatusUpdateCallback(VLCAdapter.FirstStatus);
        }
    }

    $(".help-btn").click(() => ipcRenderer.send('open-info-window'));
    $(".close-btn").click(() => window.close());
    $(".minimize-btn").click(() => remote.BrowserWindow.getFocusedWindow().minimize());
    $(".maximize-btn").click((event) =>
    {
        let win  = remote.BrowserWindow.getFocusedWindow();
        if (win.isMaximized())
        {
            $(event.target).addClass("maximize-btn");
            $(event.target).removeClass("restore-btn");
            win.restore();
        }
        else
        {
            $(event.target).removeClass("maximize-btn");
            $(event.target).addClass("restore-btn");
            win.maximize();
        }
    });
});

