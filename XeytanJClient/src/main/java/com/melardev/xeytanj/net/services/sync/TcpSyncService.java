package com.melardev.xeytanj.net.services.sync;

import com.melardev.xeytanj.XeytanJClient;
import com.melardev.xeytanj.config.IConfigService;
import com.melardev.xeytanj.logger.ConsoleLogger;
import com.melardev.xeytanj.net.packets.Packet;
import com.melardev.xeytanj.net.packets.PacketLogin;
import com.melardev.xeytanj.net.services.NetClientService;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

public class TcpSyncService implements NetClientService {
    private final XeytanJClient app;
    private final ConsoleLogger logger;
    private final String host;
    private final int port;
    private final IConfigService config;
    private ObjectInputStream sockIn;
    private ObjectOutputStream sockOut;
    private Socket socket;
    private boolean running;

    public TcpSyncService(XeytanJClient xeytanJClient, String host, int port, ConsoleLogger logger, IConfigService config) {
        this.app = xeytanJClient;
        this.logger = logger;
        this.host = host;
        this.port = port;
        this.config = config;
    }

    @Override
    public void interact() {

        while (true) {
            initNetwork();
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
        }
    }

    private void initNetwork() {
        logger.traceCurrentMethodName();
        running = true;
        try {
            socket = new Socket(this.host, this.port);
            socket.setReuseAddress(true);
            sockOut = new ObjectOutputStream(socket.getOutputStream());
            sockIn = new ObjectInputStream(socket.getInputStream());

            Packet packet;
            PacketLogin packetLogin = new PacketLogin(config.getLoginKey(), PacketLogin.LoginType.LOGIN_REQUEST);
            packetLogin.setOs(System.getProperty("os.name"));
            packetLogin.setLocalIp(socket.getLocalAddress().getHostAddress());
            packetLogin.setPcName(InetAddress.getLocalHost().getHostName());
            sockOut.writeObject(packetLogin);

            while (running) {
                if (socket.isClosed()) {
                    initNetwork();
                    return;
                }
                try {
                    packet = (Packet) sockIn.readObject();
                    app.handlePacket(packet);
                } catch (ClassNotFoundException ex) {
                    ex.printStackTrace();
                } catch (IOException e) {
                    System.err.println("Master controller has closed the connection");
                    return;
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (socket != null)
                    socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void sendPacket(Packet packet) {
        logger.traceCurrentMethodName();
        try {
            sockOut.writeObject(packet);
            sockOut.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void closeConnection() {
        try {
            running = false;
            sockIn.close();
            sockOut.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
