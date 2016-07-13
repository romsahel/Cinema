package vlcinterface;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class created by c_moi on 09/04/2016.
 */
public class VLCRemote {

    private static VLCRemote ourInstance = new VLCRemote();
    private final StringHolder lastRequestHolder;
    private BufferedWriter writer;
    private BufferedReader reader;
    private Socket socket;
    private Thread outputMonitorThread;
    private OutputMonitor outputMonitor;
    private Process process;
    private String lastStatus;

    private class StringHolder {

        private String lastRequest = "";

        public synchronized String getLastRequest() {
            final String request = this.lastRequest;
            if (request.length() > 0) {
                System.out.println(request);
            }
            lastRequest = "";
            return request;
        }

        public synchronized void setLastRequest(String lastRequest) {
            this.lastRequest = lastRequest;
        }

        public synchronized void addToLastRequest(String lastRequest) {
            if (this.lastRequest.length() > 0) {
                this.lastRequest += '\n' + lastRequest;
            } else {
                this.lastRequest += lastRequest;
            }
        }
    }

    private VLCRemote() {
        lastRequestHolder = new StringHolder();
    }

    public void init() {
        lastStatus = null;  
        try {
            Runtime rt = Runtime.getRuntime();
            process = rt.exec("vlc --extraintf lua --one-instance --rc-host localhost:4242");
        } catch (IOException ex) {
            Logger.getLogger(VLCRemote.class.getName()).log(Level.SEVERE, null, ex);
        }
        while (reader == null) {
            connectSocket();
        }
    }

    public void closeConnection() {
        if (isSocketConnected()) {
            try {
                reader.close();
                socket.close();

                reader = null;
                writer = null;
                socket = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        outputMonitor.setReader(null);
        outputMonitorThread.interrupt();
    }

    private String doWrite(String command) throws IOException, InterruptedException {
        lastRequestHolder.getLastRequest();

        writer.write(command + "\n");
        writer.flush();

        String lastRequest = waitOutput(500).replaceAll("> ", "");
        if (lastRequest.indexOf('/') == 0) {
            return lastRequest.substring(1);
        } else {
            return lastRequest;
        }
    }

    public static VLCRemote get() {
        return ourInstance;
    }

    private boolean connectSocket() {
        try {
            socket = new Socket("localhost", 4242);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (IOException ex) {
            return false;
        }

        outputMonitor = new OutputMonitor(reader);
        outputMonitorThread = new Thread(outputMonitor);
        outputMonitorThread.start();
        return socket.isConnected();
    }

    public void logout() {
        if (isSocketConnected()) {
            write("logout");
            closeConnection();
        }
    }

    private String waitOutput(int timeout) throws InterruptedException {
        if (outputMonitorThread != null && outputMonitorThread.isAlive()) {
            synchronized (outputMonitorThread) {
                outputMonitorThread.wait(timeout);
                return lastRequestHolder.getLastRequest();
            }
        }
        return "";
    }

    public String write(String command) {
        try {
            return doWrite(command);
        } catch (InterruptedException | IOException e) {
//            e.printStackTrace();
        }
        return "";
    }

    public boolean isConnected() {
        if (!isSocketConnected()) {
            return false;
        }

        lastStatus = write("status");
//        return getLastStatus().contains("new input");
        return !lastStatus.isEmpty();
    }

    public boolean isSocketConnected() {
        if (writer == null || reader == null || socket == null) {
            return false;
        }
        if (socket.isClosed() || !socket.isConnected()) {
            return false;
        }
        return true;
    }

    public String getLastStatus() {
        return lastStatus;
    }

    private class OutputMonitor implements Runnable {

        private BufferedReader reader;

        public OutputMonitor(BufferedReader reader) {
            this.reader = reader;
        }

        @Override
        public void run() {
            String line = "";
            do {
                try {
                    line = reader.readLine();
                } catch (IOException e) {
                    break;
                }
                if (line != null) {
                    lastRequestHolder.addToLastRequest(line);
                }
            } while (reader != null);
        }

        /**
         * @param reader the reader to set
         */
        public void setReader(BufferedReader reader) {
            this.reader = reader;
        }
    }

}
