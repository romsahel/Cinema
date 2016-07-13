/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vlcinterface;

import files.Settings;
import gnu.trove.map.hash.THashMap;
import main.MainController;
import org.json.simple.JSONObject;
import utils.Utils;
import videomanagerjava.CWebEngine;
import videomanagerjava.Episode;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Romsahel
 */
public class VLCController {

    private static final MainController controller = MainController.getInstance();
    //    private static TimerTaskImpl timerTask = null;
    private static Episode currentEpisode;
    private static boolean isReady = false;
    private static String currentTitle;
    private static long currentId = -1;
    private static Episode[] followingEpisodes;

    private static void init() {
        currentTitle = "";
        currentId = -1;
        currentEpisode = null;
        followingEpisodes = null;
        isReady = false;
    }

    public static void playAllFollowing(Episode[] array, boolean withSubtitles) {
        final Thread thread = new Thread(()
                -> {
            playEpisode(array[0], withSubtitles);
            followingEpisodes = array;

            for (int i = 1; i < array.length; i++) {
                controller.logLoadingScreen("Enqueuing media: " + array[i].getName());
                VLCRemote.get().write("enqueue " + array[i].getPath());
                if (withSubtitles) {
                    getSubtitles(array[i].getPath(), true);
                }
            }
        });

        controller.startLoading(thread, "Launching media");
        thread.start();
    }

    private static String getPath(final THashMap<String, String> properties) {
        String path = properties.get("path");
        if (utils.Utils.isWindows) {
            path = path.replace("/", "\\");
        }
        return path;
    }

    public static void play(final Episode episode, boolean withSubtitles) {
        final Thread thread = new Thread(()
                -> {
            playEpisode(episode, withSubtitles);
        });

        controller.startLoading(thread, "Launching media");
        thread.start();
    }

    private static void playEpisode(final Episode episode, boolean withSubtitles) {
        init();

        currentEpisode = episode;
        String path = currentEpisode.getPath();

        if (withSubtitles) {
            controller.logLoadingScreen("Downloading subtitles");
            getSubtitles(path, true);
        }

        isReady = false;
        controller.logLoadingScreen("Starting VLC");
        VLCRemote.get().init();

        controller.logLoadingScreen("Waiting for VLC");
        VLCRemote.get().write("add " + currentEpisode.getPath());

        controller.logLoadingScreen("Waiting for VLC to start media");
        String status;
        int limit = 0;
        do {
            Thread.yield();
            try {
                Thread.sleep(300);
            } catch (InterruptedException ex) {
                Logger.getLogger(VLCController.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (limit++ == 25)   {
                controller.logLoadingScreen("Failing to connect with VLC");
            }
            if (!VLCRemote.get().isConnected() || limit > 30) {
                controller.stopLoading();
                return;
            }
            status = VLCRemote.get().getLastStatus();
        } while (!status.contains(currentEpisode.getName()));

        VLCController.setCurrentTitle(VLCRemote.get().write("get_title"));

        final long time = currentEpisode.getTime();
        controller.logLoadingScreen("Seeking to previous position");
        if (time > 0) {
            VLCRemote.get().write("seek " + time);
        }

        currentEpisode.setSeen(true);

        TimerTaskImpl.startTimer();
        controller.stopLoading();
    }

    private static void log(String res) {
        Logger.getLogger(VLCController.class.getName()).log(Level.INFO, res);
    }

    private static void getSubtitles(String path, boolean absoluteMode) {
        String language = Settings.getInstance().getGeneral().get("language");
        if (language == null) {
            language = "en";
        } else {
            language = language.substring(0, 2).replace("G", "d").toLowerCase();
        }
        try {
            File file = new File(path);
            String program = "subliminal";
            final String name = '"' + file.getAbsolutePath() + '"';
            String[] cmd;
            if (absoluteMode) {
                cmd = new String[]{
                    program, "download", "-l", language, "-f", "--", name
                };
            } else {
                cmd = new String[]{
                    program, "download", "-l", language, "-f", "-d", file.getParent(), "--", file.getName()
                };
            }

            log(program + " -l " + language + " -f -- " + name);
            ProcessBuilder builder = new ProcessBuilder(cmd);
            builder.redirectErrorStream(true);
            Process process = builder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.replace("INFO: ", "");
                controller.logLoadingScreen(line);
                log(line);
                if (absoluteMode) {
                    if (line.equals("No subtitles downloaded")) {
                        getSubtitles(path, false);
                    }
                }
            }
            Utils.callFuncJS(CWebEngine.getWebEngine(), "updateIndicators");
        } catch (IOException ex) {
            Logger.getLogger(VLCController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void onInputHasChanged(URI uri) {
        if (VLCController.getFollowingEpisodes() != null) {
            TimerTaskImpl.cancelTimer();
            int index = 0;
            for (Episode episode : VLCController.getFollowingEpisodes()) {
                if (uri.getPath().endsWith(episode.getName())) {
                    VLCController.setCurrentEpisode(episode);
                    do {
                        try {
                            Thread.sleep(400);
                        } catch (InterruptedException e) {
                        }

                        if (Thread.currentThread().isInterrupted()
                                || !VLCRemote.get().isConnected()) {
                            return;
                        }

                        VLCController.setCurrentTitle(VLCRemote.get().write("get_title"));
                        System.out.println('"' + VLCController.getCurrentTitle() + '"');
                    } while (VLCController.getCurrentTitle().isEmpty()
                            || VLCController.getCurrentTitle().contains("new input: "));


                    episode.setSeen(true);
                    Utils.callFuncJS(CWebEngine.getWebEngine(), "seenNextEpisode", episode.getName(), String.valueOf(index - 1));
                    isReady = true;
                    TimerTaskImpl.startTimer();
                    return;
                }
                index++;
            }
        } 
//        else {
//            if (uri.getPath().endsWith(currentEpisode.getName())) {
//                isReady = true;
//                return;
//            }
//        }

        // if failed
        TimerTaskImpl.cancelTimer();
    }
    
    static String getCurrentTitle() {
        return currentTitle;
    }

    static void setCurrentTitle(String currentTitle) {
        VLCController.currentTitle = currentTitle;
    }

    public static Episode getCurrentEpisode() {
        return currentEpisode;
    }

    public static void setCurrentEpisode(Episode currentEpisode) {
        VLCController.currentEpisode = currentEpisode;
    }

    public static Episode[] getFollowingEpisodes() {
        return followingEpisodes;
    }
}
