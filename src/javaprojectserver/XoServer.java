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
import java.util.HashSet;
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
    private static volatile HashMap<String, String> terminate = new HashMap<>();
    private static boolean runing = true;

    public XoServer() {
        db.init();
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
                        new Thread(new clientHandler(ss)).start();
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
                                if (!userIn.containsKey(currentUser)) {
                                    out.println("true");
                                    out.flush();
                                    break;
                                } else {
                                    out.println("false acouunt is singed in");
                                    out.flush();
                                }

                            } else {
                                out.println("false you are not singup");
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
                        } else if (req.equals("forget")) {
                            if (db.updatePassword(currentUser, password)) {
                                out.println("true");
                                out.flush();
                                break;
                            } else {
                                out.println("false");
                                out.flush();
                            }

                        }
                    } else {
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
                    String page = in.readLine();
                    if (page == null || page.equals("exit")) {
                        userOut.remove(currentUser).close();
                        userIn.remove(currentUser).close();
                        db.updateUserState(currentUser, false);
                        db.updateUserAvailabelty(currentUser, false);
                        return;
                    }
                    StringBuilder sb=new StringBuilder();
                    sb.append("(online-list) ");
                    for (String st : db.getOnlineUsers()) {
                        if (!st.equals(currentUser)) {
                            if (!db.isAvailable(st)) {
                                sb.append(st).append(",(In-Game)");
                            } else {
                                sb.append(st).append(" ");
                            }
                        }
                    }
                    out.println(sb.toString());
                    out.flush();
                    try {
                        Thread.sleep(3000L);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(XoServer.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    if (in.ready()) {
                        rule = in.readLine();
                        System.out.println(rule);
                        if (rule == null) {
                            userOut.remove(currentUser).close();
                            userIn.remove(currentUser).close();
                            db.updateUserState(currentUser, false);
                            db.updateUserAvailabelty(currentUser, false);
                            return;
                        }
                        if (rule.equals("ok")) {
                            while (true) {
                                if (terminate.containsKey(currentUser)) {
                                    if (terminate.remove(currentUser).equals("break")) {
                                        break;
                                    } else {
                                        userOut.remove(currentUser).close();
                                        userIn.remove(currentUser).close();
                                        db.updateUserState(currentUser, false);
                                        db.updateUserAvailabelty(currentUser, false);
                                        return;
                                    }
                                }
                            }
                        } else if (rule.equals("exit")) {
                            userOut.remove(currentUser).close();
                            userIn.remove(currentUser).close();
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
                                otherOut.flush();
                                rule = otherIN.readLine();
                                if (rule.equals("ok")) {
                                    userOut.remove(otherUser);
                                    userIn.remove(otherUser);
                                    userOut.remove(currentUser);
                                    userIn.remove(currentUser);
                                    db.updateUserAvailabelty(otherUser, false);
                                    db.updateUserAvailabelty(currentUser, false);

                                    out.println("x");
                                    otherOut.println("o");
                                    out.flush();
                                    otherOut.flush();
                                    String userOption;
                                    while (runing) {
                                        userOption = in.readLine();
                                        if (userOption == null) {
                                            db.updateScore(db.getScore(otherUser) + 10, otherUser);
                                            terminate.put(otherUser, "break");
                                            otherOut.println("other player exit");
                                            otherOut.flush();
                                            return;
                                        } else if (userOption.contains("win")) {
                                            otherOut.println(userOption.replace("win", ""));
                                            otherOut.flush();
                                            db.updateScore(db.getScore(currentUser) + 10, currentUser);
                                            terminate.put(otherUser, "break");
                                            break;
                                        } else if (userOption.equals("exit")) {
                                            db.updateScore(db.getScore(otherUser) + 10, otherUser);
                                            terminate.put(otherUser, "break");
                                            otherOut.println("other player exit");
                                            otherOut.flush();
                                            break;
                                        } else if (userOption.equals("draw")) {
                                            terminate.put(otherUser, "break");
                                            break;
                                        } else {
                                            otherOut.println(userOption);
                                            otherOut.flush();
                                        }

                                        userOption = otherIN.readLine();
                                        if (userOption == null) {
                                            terminate.put(otherUser, "return");
                                            break;
                                        } else if (userOption.contains("win")) {
                                            out.println(userOption.replace("win", ""));
                                            out.flush();
                                            db.updateScore(db.getScore(otherUser) + 10, otherUser);
                                            terminate.put(otherUser, "break");
                                            break;
                                        } else if (userOption.equals("exit")) {
                                            db.updateScore(db.getScore(currentUser) + 10, currentUser);
                                            terminate.put(otherUser, "break");
                                            break;
                                        } else if (userOption.equals("draw")) {
                                            terminate.put(otherUser, "break");
                                            break;
                                        } else {
                                            out.println(userOption);
                                            out.flush();
                                        }

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
                if (iface.isLoopback() || !iface.isUp()) {
                    continue;
                }
                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();

                    if (iface.getDisplayName().contains("Wireless-AC")) {
                        System.out.println(iface.getDisplayName() + " " + ip);
                        ip = addr.getHostAddress();
                        break;
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

        for (String s : userIn.keySet()) {
            userOut.remove(s).close();
            try {
                userIn.remove(s).close();
            } catch (IOException ex) {
                Logger.getLogger(XoServer.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
        for (String s : db.getOnlineUsers()) {
            db.updateUserAvailabelty(s, false);
            db.updateUserAvailabelty(s, false);
            db.updateUserState(s, false);
            db.updateUserState(s, false);
        }
    }
}
