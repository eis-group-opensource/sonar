package com.eisgroup;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.IExecutionDataVisitor;
import org.jacoco.core.data.ISessionInfoVisitor;
import org.jacoco.core.data.SessionInfo;
import org.jacoco.core.runtime.RemoteControlReader;
import org.jacoco.core.runtime.RemoteControlWriter;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class App {

    private static final String DESTFILE = "data/jacoco-server.exec";
    private static String ADDRESS = null;

    static {
        try {
            ADDRESS = InetAddress.getLocalHost().getCanonicalHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    private static final int PORT = 6300;
    private static final String PORT_SERVICE = System.getProperty("PORT_SERVICE");

    static final List<Handler> listHandler = new ArrayList<Handler>();

    public static void main(String[] args) throws Exception {

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");

        Server jettyServer = new Server(Integer.parseInt(PORT_SERVICE));
        jettyServer.setHandler(context);

        ServletHolder jerseyServlet = context.addServlet(
                org.glassfish.jersey.servlet.ServletContainer.class, "/*");
        jerseyServlet.setInitOrder(0);

        // Tells the Jersey Servlet which REST service/class to load.
        jerseyServlet.setInitParameter(
                "jersey.config.server.provider.classnames",
                RestApi.class.getCanonicalName());

        try {
            jettyServer.start();

            System.out.println("Output file is: "
                    + new File(DESTFILE).getAbsolutePath());
            final RemoteControlWriter fileWriter = new RemoteControlWriter(new FileOutputStream(DESTFILE));
            System.out.println("Starting TCP server on: "
                    + ADDRESS + ":" + PORT);
            final ServerSocket server = new ServerSocket(PORT,
                    0,
                    InetAddress.getByName(ADDRESS));
            Runnable runDumpTimer = new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        Iterator<Handler> it = listHandler.iterator();
                        while (it.hasNext()) {
                            App.Handler handler = (App.Handler) it.next();
                            boolean toRemove = false;
                            String id = handler.getId();
                            try {
                                if (!handler.isAlive()) {
                                    System.out.println("Socket closed, removing handler: "
                                            + id);
                                    toRemove = true;
                                } else {
                                    //handler.captureDump1(true, true);
                                    //System.out.println("All fine, we can dump");
                                }
                            } finally {
                                if (toRemove) {
                                    it.remove();
                                }
                            }
                        }
                    }
                }
            };
            Thread threadDumpTimer = new Thread(runDumpTimer, "threadDumpTimer");
            threadDumpTimer.start();

            while (true) {
                Socket socket = server.accept();
                System.out.println("Remote connection detected, opening socket on local port: "
                        + socket.getLocalPort());
                final Handler handler = new Handler(socket, fileWriter);
                listHandler.add(handler);
                new Thread(handler).start();
            }

            //jettyServer.join();

        } finally {

            System.out.println("Warning, not valid connection detected");
            jettyServer.destroy();
        }


    }
    public static class Handler
            implements
            Runnable,
            ISessionInfoVisitor,
            IExecutionDataVisitor {
        //private static IRemoteCommandVisitor remoteWriter;
        private final Socket socket;
        private final RemoteControlReader reader;
        private final RemoteControlWriter fileWriter;
        private final RemoteControlWriter remoteWriter;
        private static String id;
        private static String sessionID = "Retrieving data from sessions:";

        public static String getId() {
            return id;
        }
        Handler(final Socket socket, final RemoteControlWriter fileWriter) throws IOException {
            this.socket = socket;
            this.fileWriter = fileWriter;
            // Just send a valid header:
            remoteWriter = new RemoteControlWriter(socket.getOutputStream());
            reader = new RemoteControlReader(socket.getInputStream());
            reader.setSessionInfoVisitor(this);
            reader.setExecutionDataVisitor(this);
        }


        public void run() {
            try {
                while (reader.read()) {
                }
                socket.close();
                synchronized (fileWriter) {
                    fileWriter.flush();
                }
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
        public void visitSessionInfo(final SessionInfo info) {
            id = info.getId();
            System.out.printf("Retrieving execution Data for session: %s%n",
                    info.getId());
            sessionID=sessionID + "\n" + info.getId();
            synchronized (fileWriter) {
                fileWriter.visitSessionInfo(info);
            }
        }

        public static String resetID() {
            sessionID = "Retrieving data from sessions:";
            try {
                FileChannel.open(Paths.get(DESTFILE), StandardOpenOption.TRUNCATE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return "ID RESET DONE";
        }

        public void visitClassExecution(final ExecutionData data) {
            synchronized (fileWriter) {
                fileWriter.visitClassExecution(data);
            }
        }
        public static void captureDump(boolean dump, boolean reset) throws IOException {
            listHandler.forEach(handler-> {
                try {
                    handler.remoteWriter.visitDumpCommand(dump, reset);
                } catch (IOException e) {
                }
            });
        }

        public static String getLogs() {
            System.out.println(sessionID);
            return sessionID;
        }


        public boolean isAlive() {
            if (socket != null
                    && socket.isConnected()) {
                return true;
            }
            return false;
        }
    }

}
