package vlcinterface;

import files.Database;
import java.net.URI;
import java.net.URISyntaxException;

import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import videomanagerjava.Episode;

/**
 * Class created by c_moi on 10/04/2016.
 */
public class TimerTaskImpl extends TimerTask {

    public static final int PERIOD = 5 * 1000;
    private static Timer timer;

    public TimerTaskImpl() {
    }

    public static void startTimer() {
        if (timer != null) {
            cancelTimer();
        }
        timer = new Timer();
        timer.schedule(new TimerTaskImpl(), PERIOD, PERIOD);
    }

    public static void cancelTimer() {
        timer.cancel();
        timer = null;
        Database.getInstance().writeDatabase();
    }

    @Override
    public void run() {
        if (!VLCRemote.get().isConnected()) {
            VLCRemote.get().closeConnection();
            TimerTaskImpl.cancelTimer();
            return;
        }

        String strTime = VLCRemote.get().write("get_time");
        Long time;
        final Episode currentEpisode = VLCController.getCurrentEpisode();
        try {
            time = Long.valueOf(strTime);
        } catch (NumberFormatException e) {
            time = currentEpisode.getTime();
            time += (PERIOD / 1000);
        }
        currentEpisode.setTime(time);

        final String status = VLCRemote.get().getLastStatus();
        if (status == null) {
            return;
        }

        final int start = status.indexOf("file:///");
        final int end = status.indexOf(" )");

        if (start == -1 || end == -1) {
            return;
        }

        final String file = status.substring(start, end);
        if (!file.contains(currentEpisode.getName())) {
            try {
                VLCController.onInputHasChanged(new URI(utils.Utils.URLEncode(file)));
            } catch (URISyntaxException ex) {
                Logger.getLogger(TimerTaskImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
