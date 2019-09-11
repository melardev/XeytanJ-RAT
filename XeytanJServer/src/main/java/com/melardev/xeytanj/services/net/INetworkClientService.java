package com.melardev.xeytanj.services.net;

import com.melardev.xeytanj.enums.MediaState;
import com.melardev.xeytanj.enums.NetworkProtocol;
import com.melardev.xeytanj.enums.ProcessInfoDetails;
import com.melardev.xeytanj.models.Client;
import com.melardev.xeytanj.models.FileInfoStructure;
import com.melardev.xeytanj.services.config.IConfigService;
import com.melardev.xeytanj.services.logger.ILogger;

import java.io.IOException;

public interface INetworkClientService {

    boolean isDesktopServiceActive(Client id);

    void interactAsync() throws IOException;

    String getRemoteIp();

    void startRdpSession();

    boolean isStreamingDesktop();

    void pauseRemoteDesktop();

    public void playRemoteDesktop(String displayName, int delay, int scaleX, int scaleY);

    void stopRemoteDesktop();

    MediaState getDesktopMediaState();

    void setConfig(IConfigService config);

    void startFileManagerSession();

    void onChatClosed();

    void onProcessSessionClosed();

    void startCameraSession(NetworkProtocol protocol);

    public void playRemoteCamera(NetworkProtocol displayName, int cameraId, boolean muteAudio, int delay);

    void stopCameraSession();

    MediaState getCameraMediaState();

    boolean isStreamingCamera();

    void pauseStreamingCamera();

    void onFilesystemRootInfoReceived(FileInfoStructure[] fileInfoStructures);

    void getFileSystemView(String fileSystemPath);

    void onFileSystemInfoUpdate(FileInfoStructure[] fileInfoStructures);

    void onKeylogDataReceived(String logs);

    void onKeylogSessionClosed();

    void startListProcess(ProcessInfoDetails level);

    void killProcess(int pid);

    boolean isListProcessActive();

    void stopProcessMonitor();

    void startShellSessionService();

    void sendShellCommand(String command);

    void pauseCameraStreaming();

    boolean isCameraServiceActive(Client client);

    void startChatSession();

    void sendChatMessage(String text);

    void onChatMessageReceived(String message);

    void setLogger(ILogger logger);

    void stopShellSession();

    void getSystemInfo();

    void closeConnection();

    void uninstallServer();
}
