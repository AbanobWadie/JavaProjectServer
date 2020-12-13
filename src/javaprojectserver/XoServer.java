/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javaprojectserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ahmed
 */
public class XoServer {

    private static ServerSocket server;
    public volatile static DatabaseProcess db = new DatabaseProcess();
    private static volatile HashMap<String, PrintWriter> userOut = new HashMap<>();
    private static volatile HashMap<String, BufferedReader> userIn = new HashMap<>();
    private static volatile HashMap<Integer, Thread> threadMap = new HashMap<>();
    private volatile Thread th;
    private static boolean runing = true;

    public XoServer() {
        db.init();
        int PORT = 5005;
        try {
            server = new ServerSocket(5005);

        } catch (IOException ex) {
            Logger.getLogger(XoServer.class.getName()).log(Level.SEVERE, null, ex);
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (runing) {
                    try {
                        Socket ss = server.accept();
                        th = new Thread(new clientHandler(ss));
                        threadMap.put(ss.getPort(), th);
                        th.start();
                        System.out.println(ss.getPort());

                    } catch (IOException e) {
                        Logger.getLogger(XoServer.class.getName()).log(Level.SEVERE, null, e);
                    }
                }
            }
        }).start();

    }

    class clientHandler implements Runnable {

        Socket socket;

        public clientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {

            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        socket.getInputStream()));
                PrintWriter out = new PrintWriter(
                        socket.getOutputStream());
                String req;
                String currentUser = null;
                String password;
                String rule;
                while (runing) {

                    String request = in.readLine();
                    StringTokenizer st = new StringTokenizer(request);
                    req = st.nextToken();
                    if (st.hasMoreTokens()) {
                        currentUser = st.nextToken();
                        password = st.nextToken();
                        System.out.println(request);
                        if (req.equals("singin")) {
                            if (db.SignIn(currentUser, password)) {
                                out.println("true");
                                out.flush();
                                break;
                            } else {
                                out.println("false");
                                out.flush();
                            }
                        } else if (req.equals("singup")) {
                            if (db.SignUp(currentUser, password)) {
                                out.println("true");
                                out.flush();
                                break;
                            } else {
                                out.println("false");
                                out.flush();
                            }
                        } else if (req.equals("singup")) {
                            if (db.SignUp(currentUser, password)) {
                                out.println("true");
                                out.flush();
                                break;
                            } else {
                                out.println("false");
                                out.flush();
                            }

                        }
                    } else {

                        threadMap.remove(socket.getPort());
                        return;
                    }

                }

                userOut.put(currentUser, out);
                userIn.put(currentUser, in);
                db.updateUserAvailabelty(currentUser, true);
                db.updateUserState(currentUser, true);
                PrintWriter otherOut;
                BufferedReader otherIN;
                while (runing) {
                    for (String st : db.getOnlineUsers()) {
                        if (!st.equals(currentUser)) {
                            if(db.isAvailable(st))
                                out.println(st + "  (In-Game)");
                            else
                                out.println(st);
                        }
                    }
                    out.println("end");
                    out.flush();
                    try {
                        Thread.sleep(3000L);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(XoServer.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    if (in.ready()) {
                        rule = in.readLine();
                        System.out.println(rule);
                        if (rule.equals("exit")) {
                            userOut.remove(currentUser);
                            userIn.remove(currentUser);
                            threadMap.remove(socket.getPort());
                            
                            db.updateUserState(currentUser, false);
                            db.updateUserAvailabelty(currentUser, false);
                            return;
                        } else if (rule.contains("play")) {
                            String st[] = rule.split(" ");
                            String otherUser = st[1];
                            System.out.println(otherUser);
                            if (userOut.containsKey(otherUser)) {
                                System.out.println(otherUser);
                                otherOut = userOut.get(otherUser);
                                otherIN = userIn.get(otherUser);
                                otherOut.println("play request from " + currentUser);
                                System.out.println("hi");
                                out.flush();

                                if (otherIN.readLine().equals("ok")) {
                                    userOut.remove(otherUser);
                                    userIn.remove(otherUser);
                                    userOut.remove(currentUser);
                                    userIn.remove(currentUser);
                                    db.updateUserAvailabelty(otherUser, false);
                                    db.updateUserAvailabelty(currentUser, false);
                                    out.println("ok");
                                    out.flush();
                                    out.println("x");
                                    otherOut.println("o");
                                    out.flush();
                                    otherOut.flush();
                                    String userOption;
                                    while (runing) {
                                        userOption = in.readLine();
                                        if (userOption.contains("win")) {
                                            otherOut.println(userOption.replace("win", ""));
                                            otherOut.flush();
                                            db.updateScore(db.getScore(currentUser) + 10, currentUser);
                                            break;
                                        }
                                        if (userOption.equals("exit")) {
                                            db.updateScore(db.getScore(otherUser) + 10, otherUser);
                                            threadMap.remove(socket.getPort());
                                            break;
                                        }
                                        otherOut.println(userOption);
                                        otherOut.flush();
                                        userOption = otherIN.readLine();
                                        if (userOption.contains("win")) {
                                            out.println(userOption.replace("win", ""));
                                            out.flush();
                                            db.updateScore(db.getScore(otherUser) + 10, otherUser);
                                            break;
                                        }
                                        if (userOption.equals("exit")) {
                                            db.updateScore(db.getScore(currentUser) + 10, currentUser);
                                            threadMap.remove(socket.getPort());
                                            break;
                                        }
                                        out.println(userOption);
                                        out.flush();
                                    }
                                    userOut.put(otherUser, otherOut);
                                    userIn.put(otherUser, otherIN);
                                    userOut.put(currentUser, out);
                                    userIn.put(currentUser, in);
                                    db.updateUserAvailabelty(otherUser, true);
                                    db.updateUserAvailabelty(currentUser, true);
                                } else {
                                    out.println("no");
                                    out.flush();
                                }
                            } else {
                                threadMap.remove(socket.getPort());
                                break;
                            }
                        }
                    }

                }

            } catch (IOException e) {
                Logger.getLogger(XoServer.class.getName()).log(Level.SEVERE, null, e);
            }

        }

    }

    public String getIP() {
        String ip = null;
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                // filters out 127.0.0.1 and inactive interfaces
                if (iface.isLoopback() || !iface.isUp()) {
                    continue;
                }
                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();

                    // System.out.println(iface.getDisplayName() + " " + ip);
                    if (iface.getDisplayName().contains("Wireless-AC")) {
                        System.out.println(iface.getDisplayName() + " " + ip);
                        ip = addr.getHostAddress();
                        break;
                    }
                    // EDIT
                    if (addr instanceof InetAddress) {
                        continue;
                    }

                }
            }
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
        System.out.println("server");

        return ip;
    }

    public static void closeServer() {

        runing = false;
        try {
            server.close();
        } catch (IOException ex) {
            Logger.getLogger(XoServer.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
